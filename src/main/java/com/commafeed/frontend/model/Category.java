package com.commafeed.frontend.model;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

@SuppressWarnings("serial")
@ApiModel("Entry details")
@Data
public class Category implements Serializable {

	@ApiModelProperty("category id")
	private String id;

	@ApiModelProperty("parent category id")
	private String parentId;

	@ApiModelProperty("category id")
	private String name;

	@ApiModelProperty("category children categories")
	private List<Category> children = Lists.newArrayList();

	@ApiModelProperty("category feeds")
	private List<Subscription> feeds = Lists.newArrayList();

	@ApiModelProperty("wether the category is expanded or collapsed")
	private boolean expanded;

	@ApiModelProperty("position of the category in the list")
	private Integer position;
}