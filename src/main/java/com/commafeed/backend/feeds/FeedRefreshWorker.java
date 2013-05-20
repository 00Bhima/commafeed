package com.commafeed.backend.feeds;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter.NotModifiedException;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.sun.syndication.io.FeedException;

public class FeedRefreshWorker {

	private static Logger log = LoggerFactory
			.getLogger(FeedRefreshWorker.class);

	@Inject
	FeedRefreshUpdater feedRefreshUpdater;

	@Inject
	FeedFetcher fetcher;

	@Inject
	FeedRefreshTaskGiver taskGiver;

	public void start(MutableBoolean running, String threadName) {
		log.info("{} starting", threadName);
		while (running.isTrue()) {
			Feed feed = null;
			try {
				feed = getNextFeed();
				if (feed != null) {
					log.debug("refreshing " + feed.getUrl());
					update(feed);
				} else {
					log.debug("sleeping");
					Thread.sleep(15000);
				}
			} catch (InterruptedException e) {
				log.info(threadName + " interrupted");
				return;
			} catch (Exception e) {
				String feedUrl = "feed is null";
				if (feed != null) {
					feedUrl = feed.getUrl();
				}
				log.error(
						threadName + " (" + feedUrl + ") : " + e.getMessage(),
						e);
			}
		}
	}

	private void update(Feed feed) throws NotSupportedException,
			SystemException, SecurityException, IllegalStateException,
			RollbackException, HeuristicMixedException,
			HeuristicRollbackException, JMSException {

		FetchedFeed fetchedFeed = null;
		Collection<FeedEntry> entries = null;

		String message = null;
		int errorCount = 0;
		Date disabledUntil = null;

		try {
			fetchedFeed = fetcher.fetch(feed.getUrl(), false,
					feed.getLastModifiedHeader(), feed.getEtagHeader());

			// stops here if NotModifiedException is thrown
			entries = fetchedFeed.getEntries();
			disabledUntil = FeedUtils.calculateDisabledDate(fetchedFeed);

			feed.setLastUpdateSuccess(Calendar.getInstance().getTime());
			feed.setLink(fetchedFeed.getFeed().getLink());
			feed.setLastModifiedHeader(fetchedFeed.getFeed()
					.getLastModifiedHeader());
			feed.setEtagHeader(fetchedFeed.getFeed().getEtagHeader());

		} catch (NotModifiedException e) {
			log.debug("Feed not modified (304) : " + feed.getUrl());
			if (feed.getErrorCount() == 0) {
				// not modified and had no error before, do nothing
				return;
			}
		} catch (Exception e) {
			message = "Unable to refresh feed " + feed.getUrl() + " : "
					+ e.getMessage();
			if (e instanceof FeedException) {
				log.debug(e.getClass().getName() + " " + message);
			} else {
				log.debug(e.getClass().getName() + " " + message);
			}

			errorCount = feed.getErrorCount() + 1;
			disabledUntil = FeedUtils.calculateDisabledDate(errorCount);
		}

		feed.setErrorCount(errorCount);
		feed.setMessage(message);
		feed.setDisabledUntil(disabledUntil);
		log.info(feed.getUrl() + " disabledUntil " + disabledUntil);
		feedRefreshUpdater.updateEntries(feed, entries);

	}

	private Feed getNextFeed() {
		return taskGiver.take();
	}

}
