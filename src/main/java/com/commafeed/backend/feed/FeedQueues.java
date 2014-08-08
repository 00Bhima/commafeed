package com.commafeed.backend.feed;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.time.DateUtils;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.model.Feed;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

public class FeedQueues {

	private final FeedDAO feedDAO;
	private final CommaFeedConfiguration config;

	private Queue<FeedRefreshContext> addQueue = Queues.newConcurrentLinkedQueue();
	private Queue<FeedRefreshContext> takeQueue = Queues.newConcurrentLinkedQueue();
	private Queue<Feed> giveBackQueue = Queues.newConcurrentLinkedQueue();

	private Meter refill;

	@Inject
	public FeedQueues(FeedDAO feedDAO, CommaFeedConfiguration config, MetricRegistry metrics) {
		this.config = config;
		this.feedDAO = feedDAO;

		refill = metrics.meter(MetricRegistry.name(getClass(), "refill"));
		metrics.register(MetricRegistry.name(getClass(), "addQueue"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return addQueue.size();
			}
		});
		metrics.register(MetricRegistry.name(getClass(), "takeQueue"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return takeQueue.size();
			}
		});
		metrics.register(MetricRegistry.name(getClass(), "giveBackQueue"), new Gauge<Integer>() {
			@Override
			public Integer getValue() {
				return giveBackQueue.size();
			}
		});
	}

	/**
	 * take a feed from the refresh queue
	 */
	public synchronized FeedRefreshContext take() {
		FeedRefreshContext context = takeQueue.poll();

		if (context == null) {
			refill();
			context = takeQueue.poll();
		}
		return context;
	}

	/**
	 * add a feed to the refresh queue
	 */
	public void add(Feed feed, boolean urgent) {
		int refreshInterval = config.getApplicationSettings().getRefreshIntervalMinutes();
		if (feed.getLastUpdated() == null || feed.getLastUpdated().before(DateUtils.addMinutes(new Date(), -1 * refreshInterval))) {
			addQueue.add(new FeedRefreshContext(feed, urgent));
		}
	}

	/**
	 * refills the refresh queue and empties the giveBack queue while at it
	 */
	private void refill() {
		refill.mark();

		List<FeedRefreshContext> contexts = Lists.newArrayList();
		int batchSize = Math.min(100, 3 * config.getApplicationSettings().getBackgroundThreads());

		// add feeds we got from the add() method
		int addQueueSize = addQueue.size();
		for (int i = 0; i < Math.min(batchSize, addQueueSize); i++) {
			contexts.add(addQueue.poll());
		}

		// add feeds that are up to refresh from the database
		if (!config.getApplicationSettings().isCrawlingPaused()) {
			int count = batchSize - contexts.size();
			if (count > 0) {
				List<Feed> feeds = feedDAO.findNextUpdatable(count, getLastLoginThreshold());
				for (Feed feed : feeds) {
					contexts.add(new FeedRefreshContext(feed, false));
				}
			}
		}

		// set the disabledDate as we use it in feedDAO to decide what to refresh next. We also use a map to remove
		// duplicates.
		Map<Long, FeedRefreshContext> map = Maps.newLinkedHashMap();
		for (FeedRefreshContext context : contexts) {
			Feed feed = context.getFeed();
			feed.setDisabledUntil(DateUtils.addMinutes(new Date(), config.getApplicationSettings().getRefreshIntervalMinutes()));
			map.put(feed.getId(), context);
		}

		// refill the queue
		takeQueue.addAll(map.values());

		// add feeds from the giveBack queue to the map, overriding duplicates
		int giveBackQueueSize = giveBackQueue.size();
		for (int i = 0; i < giveBackQueueSize; i++) {
			Feed feed = giveBackQueue.poll();
			map.put(feed.getId(), new FeedRefreshContext(feed, false));
		}

		// update all feeds in the database
		List<Feed> feeds = Lists.newArrayList();
		for (FeedRefreshContext context : map.values()) {
			feeds.add(context.getFeed());
		}
		feedDAO.merge(feeds);
	}

	/**
	 * give a feed back, updating it to the database during the next refill()
	 */
	public void giveBack(Feed feed) {
		String normalized = FeedUtils.normalizeURL(feed.getUrl());
		feed.setNormalizedUrl(normalized);
		feed.setNormalizedUrlHash(DigestUtils.sha1Hex(normalized));
		feed.setLastUpdated(new Date());
		giveBackQueue.add(feed);
	}

	private Date getLastLoginThreshold() {
		if (config.getApplicationSettings().isHeavyLoad()) {
			return DateUtils.addDays(new Date(), -30);
		} else {
			return null;
		}
	}

}
