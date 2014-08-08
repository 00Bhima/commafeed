package com.commafeed.frontend.model.request;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@ApiModel("Mark Request")
@Data
public class MarkRequest implements Serializable {

	@ApiModelProperty(value = "entry id, category id, 'all' or 'starred'", required = true)
	private String id;

	@ApiModelProperty(value = "mark as read or unread")
	private boolean read;

	@ApiModelProperty(
			value = "only entries older than this, pass the timestamp you got from the entry list to prevent marking an entry that was not retrieved",
			required = false)
	private Long olderThan;

	@ApiModelProperty(value = "if marking a category or 'all', exclude those subscriptions from the marking", required = false)
	private List<Long> excludedSubscriptions;

}
