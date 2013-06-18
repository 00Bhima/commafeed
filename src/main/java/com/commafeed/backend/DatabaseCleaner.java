package com.commafeed.backend;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedEntryStatusDAO;

public class DatabaseCleaner {

	private static Logger log = LoggerFactory.getLogger(DatabaseCleaner.class);

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	public long cleanOlderThan(long value, TimeUnit unit) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1 * (int) unit.toMinutes(value));

		long total = 0;
		int deleted = -1;
		do {
			deleted = feedEntryStatusDAO.delete(cal.getTime(), 100);
			total += deleted;
			log.info("removed %d statuses", total);
		} while (deleted != 0);
		log.info("cleanup done: %d", total);
		return total;
	}
}
