package com.commafeed.backend;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedEntryDAO;

public class DatabaseCleaner {

	private static Logger log = LoggerFactory.getLogger(DatabaseCleaner.class);

	@Inject
	FeedEntryDAO feedEntryDAO;

	public long cleanOlderThan(long value, TimeUnit unit) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1 * (int) unit.toMinutes(value));

		long total = 0;
		int deleted = -1;
		do {
			deleted = feedEntryDAO.delete(cal.getTime(), 100);
			total += deleted;
			log.info("removed {} entries", total);
		} while (deleted != 0);
		log.info("cleanup done: {} entries deleted", total);
		return total;
	}
}
