package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiClass;
import com.wordnik.swagger.annotations.ApiProperty;

@SuppressWarnings("serial")
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@ApiClass("List of entries with some metadata")
public class Entries implements Serializable {

	@ApiProperty("Name of the feed or the category requested")
	private String name;

	@ApiProperty("Error or warning message")
	private String message;

	@ApiProperty("TImes the server tried to refresh the feed and failed")
	private int errorCount;

	@ApiProperty("List generation Timestamp")
	private long timestamp;

	@ApiProperty("List of entries")
	private List<Entry> entries = Lists.newArrayList();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getErrorCount() {
		return errorCount;
	}

	public void setErrorCount(int errorCount) {
		this.errorCount = errorCount;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
