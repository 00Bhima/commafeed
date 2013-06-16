package com.commafeed.backend;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.model.FeedEntry;

@Stateless
public class DatabaseCleaner {

	@Inject
	FeedEntryDAO feedEntryDAO;

	public void cleanOlderThan(long value, TimeUnit unit) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -1 * (int) unit.toMinutes(value));

		int deleted = -1;
		do {
			List<FeedEntry> entries = feedEntryDAO.findByInserted(
					cal.getTime(), 1000);
			deleted = entries.size();

			for (FeedEntry entry : entries) {
				feedEntryDAO.delete(entry.getStatuses());
			}
			feedEntryDAO.delete(entries);

		} while (deleted != 0);

	}
}
