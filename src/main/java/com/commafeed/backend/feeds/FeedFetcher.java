package com.commafeed.backend.feeds;

import java.io.IOException;

import javax.inject.Inject;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.commafeed.backend.HttpGetter;
import com.commafeed.backend.model.Feed;
import com.sun.syndication.io.FeedException;

public class FeedFetcher {

	private static Logger log = LoggerFactory.getLogger(FeedFetcher.class);

	@Inject
	FeedParser parser;

	@Inject
	HttpGetter getter;

	public Feed fetch(String feedUrl) throws FeedException,
			ClientProtocolException, IOException {
		log.debug("Fetching feed {}", feedUrl);
		Feed feed = null;

		byte[] content = getter.getBinary(feedUrl);
		String extractedUrl = extractFeedUrl(StringUtils.newStringUtf8(content));
		if (extractedUrl != null) {
			content = getter.getBinary(extractedUrl);
			feedUrl = extractedUrl;
		}
		feed = parser.parse(feedUrl, content);

		return feed;
	}

	private String extractFeedUrl(String html) {
		String foundUrl = null;

		Document doc = Jsoup.parse(html);
		String root = doc.children().get(0).tagName();
		if ("html".equals(root)) {
			Elements rss = doc.select("link[type=application/rss+xml]");
			Elements atom = doc.select("link[type=application/atom+xml]");
			if (!rss.isEmpty()) {
				foundUrl = rss.get(0).attr("abs:href").toString();
			} else if (!atom.isEmpty()) {
				foundUrl = atom.get(0).attr("abs:href").toString();
			}
		}
		return foundUrl;
	}
}
