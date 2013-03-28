package com.commafeed.backend.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.collect.Sets;

@Entity
@Table(name = "FEEDCATEGORIES")
@SuppressWarnings("serial")
public class FeedCategory extends AbstractModel {

	@Column(length = 128)
	private String name;

	@ManyToOne
	private User user;

	@ManyToOne
	private FeedCategory parent;

	@OneToMany(mappedBy = "parent")
	private Set<FeedCategory> children;

	@OneToMany(mappedBy = "category")
	private Set<FeedSubscription> subscriptions;

	private boolean collapsed;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public FeedCategory getParent() {
		return parent;
	}

	public void setParent(FeedCategory parent) {
		this.parent = parent;
	}

	public Set<FeedSubscription> getSubscriptions() {
		if (subscriptions == null) {
			return Sets.newHashSet();
		}
		return subscriptions;
	}

	public void setSubscriptions(Set<FeedSubscription> subscriptions) {
		this.subscriptions = subscriptions;
	}

	public Set<FeedCategory> getChildren() {
		return children;
	}

	public void setChildren(Set<FeedCategory> children) {
		this.children = children;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

}
