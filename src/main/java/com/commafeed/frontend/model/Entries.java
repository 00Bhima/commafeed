package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@ApiClass("List of entries with some metadata")
@Data
public class Entries implements Serializable {

	@ApiProperty("name of the feed or the category requested")
	private String name;

	@ApiProperty("error or warning message")
	private String message;

	@ApiProperty("times the server tried to refresh the feed and failed")
	private int errorCount;

	@ApiProperty("URL of the website, extracted from the feed")
	private String feedLink;

	@ApiProperty("list generation timestamp")
	private long timestamp;

	@ApiProperty("if the query has more elements")
	private boolean hasMore;

	@ApiProperty("the requested offset")
	private int offset;

	@ApiProperty("the requested limit")
	private int limit;

	@ApiProperty("list of entries")
	private List<Entry> entries = Lists.newArrayList();

	@ApiProperty("true if the unread flag was ignored in the request, all entries are returned regardless of their read status")
	private boolean ignoredReadStatus;

}
