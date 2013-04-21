package com.commafeed.backend.feeds;

import java.io.StringReader;
import java.util.List;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.dao.FeedCategoryDAO;
import com.commafeed.backend.model.FeedCategory;
import com.commafeed.backend.model.User;
import com.commafeed.backend.services.FeedSubscriptionService;
import com.sun.syndication.feed.opml.Opml;
import com.sun.syndication.feed.opml.Outline;
import com.sun.syndication.io.WireFeedInput;

@Stateless
public class OPMLImporter {

	private static Logger log = LoggerFactory.getLogger(OPMLImporter.class);

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@Inject
	FeedCategoryDAO feedCategoryDAO;

	@SuppressWarnings("unchecked")
	@Asynchronous
	public void importOpml(User user, String xml) {

		WireFeedInput input = new WireFeedInput();
		try {
			Opml feed = (Opml) input.build(new StringReader(xml));
			List<Outline> outlines = (List<Outline>) feed.getOutlines();
			for (Outline outline : outlines) {
				handleOutline(user, outline, null);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

	}

	@SuppressWarnings("unchecked")
	private void handleOutline(User user, Outline outline, FeedCategory parent) {

		if (StringUtils.isEmpty(outline.getType())) {
			FeedCategory category = feedCategoryDAO.findByName(user,
					outline.getText(), parent);
			if (category == null) {
				category = new FeedCategory();
				category.setName(outline.getText());
				category.setParent(parent);
				category.setUser(user);
				feedCategoryDAO.save(category);
			}

			List<Outline> children = outline.getChildren();
			for (Outline child : children) {
				handleOutline(user, child, category);
			}
		} else {

			feedSubscriptionService.subscribe(user, outline.getXmlUrl(),
					outline.getText(), parent);
		}
	}
}
