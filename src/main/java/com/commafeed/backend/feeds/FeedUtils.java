package com.commafeed.backend.feeds;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document.OutputSettings;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.safety.Whitelist;
import org.mozilla.universalchardet.UniversalDetector;

public class FeedUtils {

	public static String guessEncoding(byte[] bytes) {
		String DEFAULT_ENCODING = "UTF-8";
		UniversalDetector detector = new UniversalDetector(null);
		detector.handleData(bytes, 0, bytes.length);
		detector.dataEnd();
		String encoding = detector.getDetectedCharset();
		detector.reset();
		if (encoding == null) {
			encoding = DEFAULT_ENCODING;
		} else if (encoding.equalsIgnoreCase("ISO-8859-1")) {
			encoding = "windows-1252";
		}
		return encoding;
	}

	public static String handleContent(String content, String baseUri) {
		if (StringUtils.isNotBlank(content)) {
			baseUri = StringUtils.trimToEmpty(baseUri);
			Whitelist whitelist = Whitelist.relaxed();
			whitelist.addEnforcedAttribute("a", "target", "_blank");

			whitelist.addTags("iframe");
			whitelist.addAttributes("iframe", "src", "height", "width",
					"allowfullscreen", "frameborder");
			
			whitelist.addAttributes("table", "border", "bordercolor");
			whitelist.addAttributes("th", "border", "bordercolor");
			whitelist.addAttributes("td", "border", "bordercolor");

			content = Jsoup.clean(content, baseUri, whitelist,
					new OutputSettings().escapeMode(EscapeMode.base)
							.prettyPrint(false));
		}
		return content;
	}

	public static String trimInvalidXmlCharacters(String xml) {
		if (StringUtils.isBlank(xml)) {
			return null;
		}
		StringBuilder sb = new StringBuilder();

		boolean firstTagFound = false;
		for (int i = 0; i < xml.length(); i++) {
			char c = xml.charAt(i);

			if (!firstTagFound) {
				if (c == '<') {
					firstTagFound = true;
				} else {
					continue;
				}
			}

			if (c >= 32 || c == 9 || c == 10 || c == 13) {
				if (!Character.isHighSurrogate(c)
						&& !Character.isLowSurrogate(c)) {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}
}
