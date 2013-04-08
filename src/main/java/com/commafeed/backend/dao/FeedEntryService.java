package com.commafeed.backend.dao;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryStatus;
import com.commafeed.backend.model.FeedSubscription;
import com.google.common.collect.Lists;

@Stateless
@SuppressWarnings("serial")
public class FeedEntryService extends GenericDAO<FeedEntry> {

	@Inject
	FeedService feedService;

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	public void updateEntries(String url, Collection<FeedEntry> entries) {
		Feed feed = feedService.findByUrl(url);
		List<String> guids = Lists.newArrayList();
		for (FeedEntry entry : entries) {
			guids.add(entry.getGuid());
		}

		List<FeedEntry> existingEntries = getByGuids(guids);
		for (FeedEntry entry : entries) {
			FeedEntry foundEntry = null;
			for (FeedEntry existingEntry : existingEntries) {
				if (StringUtils
						.equals(entry.getGuid(), existingEntry.getGuid())) {
					foundEntry = existingEntry;
					break;
				}
			}
			if (foundEntry == null) {
				addFeedToEntry(entry, feed);
			} else {
				boolean foundFeed = false;
				for (Feed existingFeed : foundEntry.getFeeds()) {
					if (ObjectUtils.equals(existingFeed.getId(), feed.getId())) {
						foundFeed = true;
						break;
					}
				}

				if (!foundFeed) {
					addFeedToEntry(foundEntry, feed);
				}
			}
		}

		feed.setLastUpdated(Calendar.getInstance().getTime());
		feed.setMessage(null);
		feedService.update(feed);
	}

	private void addFeedToEntry(FeedEntry entry, Feed feed) {
		entry.getFeeds().add(feed);
		saveOrUpdate(entry);
		List<FeedSubscription> subscriptions = feedSubscriptionService
				.findByFeed(feed);
		for (FeedSubscription sub : subscriptions) {
			FeedEntryStatus status = new FeedEntryStatus();
			status.setEntry(entry);
			status.setSubscription(sub);
			em.persist(status);
		}

	}

	public List<FeedEntry> getByGuids(List<String> guids) {
		TypedQuery<FeedEntry> query = em.createNamedQuery("Entry.byGuids",
				FeedEntry.class);
		query.setParameter("guids", guids);
		return query.getResultList();
	}

}
