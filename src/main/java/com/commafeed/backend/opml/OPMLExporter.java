package com.commafeed.backend.opml;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.RequiredArgsConstructor;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.FeedSubscription;
import com.commafeed.backend.model.User;
import com.rometools.opml.feed.opml.Attribute;
import com.rometools.opml.feed.opml.Opml;
import com.rometools.opml.feed.opml.Outline;

@RequiredArgsConstructor(onConstructor = @__({ @Inject }))
@Singleton
public class OPMLExporter {

	private final FeedCategoryDAO feedCategoryDAO;
	private final FeedSubscriptionDAO feedSubscriptionDAO;

	public Opml export(User user) {
		Opml opml = new Opml();
		opml.setFeedType("opml_1.1");
		opml.setTitle(String.format("%s subscriptions in CommaFeed", user.getName()));
		opml.setCreated(new Date());

		List<FeedCategory> categories = feedCategoryDAO.findAll(user);
		List<FeedSubscription> subscriptions = feedSubscriptionDAO.findAll(user);

		// export root categories
		for (FeedCategory cat : categories) {
			if (cat.getParent() == null) {
				opml.getOutlines().add(buildCategoryOutline(cat, subscriptions));
			}
		}

		// export root subscriptions
		for (FeedSubscription sub : subscriptions) {
			if (sub.getCategory() == null) {
				opml.getOutlines().add(buildSubscriptionOutline(sub));
			}
		}

		return opml;

	}

	private Outline buildCategoryOutline(FeedCategory cat, List<FeedSubscription> subscriptions) {
		Outline outline = new Outline();
		outline.setText(cat.getName());
		outline.setTitle(cat.getName());

		for (FeedCategory child : cat.getChildren()) {
			outline.getChildren().add(buildCategoryOutline(child, subscriptions));
		}

		for (FeedSubscription sub : subscriptions) {
			if (sub.getCategory() != null && sub.getCategory().getId().equals(cat.getId())) {
				outline.getChildren().add(buildSubscriptionOutline(sub));
			}
		}
		return outline;
	}

	private Outline buildSubscriptionOutline(FeedSubscription sub) {
		Outline outline = new Outline();
		outline.setText(sub.getTitle());
		outline.setTitle(sub.getTitle());
		outline.setType("rss");
		outline.getAttributes().add(new Attribute("xmlUrl", sub.getFeed().getUrl()));
		if (sub.getFeed().getLink() != null) {
			outline.getAttributes().add(new Attribute("htmlUrl", sub.getFeed().getLink()));
		}
		return outline;
	}
}
