package com.commafeed.backend.service;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryContentDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;

/**
 * Contains utility methods for cleaning the database
 * 
 */
@Slf4j
@RequiredArgsConstructor
public class DatabaseCleaningService {

	private static final int BATCH_SIZE = 100;

	private final FeedDAO feedDAO;
	private final FeedEntryDAO feedEntryDAO;
	private final FeedEntryContentDAO feedEntryContentDAO;
	private final FeedEntryStatusDAO feedEntryStatusDAO;
	private final FeedSubscriptionDAO feedSubscriptionDAO;

	public long cleanEntriesWithoutSubscriptions() {
		log.info("cleaning entries without subscriptions");
		long total = 0;
		int deleted = 0;
		do {
			List<FeedEntry> entries = feedEntryDAO.findWithoutSubscriptions(BATCH_SIZE);
			deleted = feedEntryDAO.delete(entries);
			total += deleted;
			log.info("removed {} entries without subscriptions", total);
		} while (deleted != 0);
		log.info("cleanup done: {} entries without subscriptions deleted", total);
		return total;
	}

	public long cleanFeedsWithoutSubscriptions() {
		log.info("cleaning feeds without subscriptions");
		long total = 0;
		int deleted = 0;
		do {
			List<Feed> feeds = feedDAO.findWithoutSubscriptions(BATCH_SIZE);
			deleted = feedDAO.delete(feeds);
			total += deleted;
			log.info("removed {} feeds without subscriptions", total);
		} while (deleted != 0);
		log.info("cleanup done: {} feeds without subscriptions deleted", total);
		return total;
	}

	public long cleanContentsWithoutEntries() {
		log.info("cleaning contents without entries");
		long total = 0;
		int deleted = 0;
		do {
			deleted = feedEntryContentDAO.deleteWithoutEntries(BATCH_SIZE);
			total += deleted;
			log.info("removed {} contents without entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} contents without entries deleted", total);
		return total;
	}

	public long cleanEntriesOlderThan(long value, TimeUnit unit) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1 * (int) unit.toMinutes(value));

		long total = 0;
		int deleted = 0;
		do {
			deleted = feedEntryDAO.delete(cal.getTime(), BATCH_SIZE);
			total += deleted;
			log.info("removed {} entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} entries deleted", total);
		return total;
	}

	public void mergeFeeds(Feed into, List<Feed> feeds) {
		for (Feed feed : feeds) {
			if (into.getId().equals(feed.getId())) {
				continue;
			}
			List<FeedSubscription> subs = feedSubscriptionDAO.findByFeed(feed);
			for (FeedSubscription sub : subs) {
				sub.setFeed(into);
			}
			feedSubscriptionDAO.saveOrUpdate(subs);
			feedDAO.delete(feed);
		}
		feedDAO.saveOrUpdate(into);
	}

	public long cleanStatusesOlderThan(Date olderThan) {
		log.info("cleaning old read statuses");
		long total = 0;
		List<FeedEntryStatus> list = Collections.emptyList();
		do {
			list = feedEntryStatusDAO.getOldStatuses(olderThan, BATCH_SIZE);
			if (!list.isEmpty()) {
				feedEntryStatusDAO.delete(list);
				total += list.size();
				log.info("cleaned {} old read statuses", total);
			}
		} while (!list.isEmpty());
		log.info("cleanup done: {} old read statuses deleted", total);
		return total;
	}
}
