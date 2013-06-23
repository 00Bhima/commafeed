package com.commafeed.backend.dao;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import org.apache.commons.codec.digest.DigestUtils;

import com.commafeed.backend.model.Feed;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntry_;
import com.commafeed.backend.model.Feed_;
import com.google.common.collect.Iterables;

@Stateless
public class FeedEntryDAO extends GenericDAO<FeedEntry> {

	public static class EntryWithFeed {
		public FeedEntry entry;
		public Feed feed;

		public EntryWithFeed(FeedEntry entry, Feed feed) {
			this.entry = entry;
			this.feed = feed;
		}
	}

	public EntryWithFeed findExisting(String guid, String url, Long feedId) {

		TypedQuery<EntryWithFeed> q = em.createNamedQuery(
				"EntryStatus.existing", EntryWithFeed.class);
		q.setParameter("guidHash", DigestUtils.sha1Hex(guid));
		q.setParameter("url", url);
		q.setParameter("feedId", feedId);

		List<EntryWithFeed> resultList = q.getResultList();
		EntryWithFeed ewf = Iterables.getFirst(resultList, null);
		return ewf;
	}

	public List<FeedEntry> findByFeed(Feed feed, int offset, int limit) {
		CriteriaQuery<FeedEntry> query = builder.createQuery(getType());
		Root<FeedEntry> root = query.from(getType());
		SetJoin<FeedEntry, Feed> feedsJoin = root.join(FeedEntry_.feeds);

		query.where(builder.equal(feedsJoin.get(Feed_.id), feed.getId()));
		query.orderBy(builder.desc(root.get(FeedEntry_.updated)));
		TypedQuery<FeedEntry> q = em.createQuery(query);
		limit(q, offset, limit);
		return q.getResultList();
	}

	public int delete(Date olderThan, int max) {
		CriteriaQuery<FeedEntry> query = builder.createQuery(getType());
		Root<FeedEntry> root = query.from(getType());
		query.where(builder.lessThan(root.get(FeedEntry_.inserted), olderThan));

		TypedQuery<FeedEntry> q = em.createQuery(query);
		q.setMaxResults(max);
		List<FeedEntry> list = q.getResultList();

		int deleted = list.size();
		delete(list);
		return deleted;
	}
}
