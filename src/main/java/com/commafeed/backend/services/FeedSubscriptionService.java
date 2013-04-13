package com.commafeed.backend.services;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryStatusDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.google.api.client.util.Lists;

@Stateless
public class FeedSubscriptionService {

	@Inject
	FeedDAO feedDAO;

	@Inject
	FeedEntryDAO feedEntryDAO;

	@Inject
	FeedEntryStatusDAO feedEntryStatusDAO;

	@Inject
	FeedSubscriptionDAO feedSubscriptionDAO;

	public void subscribe(User user, String url, String title,
			FeedCategory category) {

		Feed feed = feedDAO.findByUrl(url);
		if (feed == null) {
			feed = new Feed();
			feed.setUrl(url);
			feedDAO.save(feed);
		}

		FeedSubscription sub = feedSubscriptionDAO.findByFeed(user, feed);
		boolean newSubscription = false;
		if (sub == null) {
			sub = new FeedSubscription();
			sub.setFeed(feed);
			sub.setUser(user);
			newSubscription = true;
		}
		sub.setCategory(category);
		sub.setTitle(title);
		feedSubscriptionDAO.saveOrUpdate(sub);

		if (newSubscription) {
			List<FeedEntryStatus> statuses = Lists.newArrayList();
			List<FeedEntry> allEntries = feedEntryDAO.findByFeed(feed, 0, 10);
			for (FeedEntry entry : allEntries) {
				FeedEntryStatus status = new FeedEntryStatus();
				status.setEntry(entry);
				status.setRead(true);
				status.setSubscription(sub);
				statuses.add(status);
			}
			feedEntryStatusDAO.save(statuses);
		}
	}
}
