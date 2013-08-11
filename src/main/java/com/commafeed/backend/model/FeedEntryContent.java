package com.commafeed.backend.model;

import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "FEEDENTRYCONTENTS")
@SuppressWarnings("serial")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
@Getter
@Setter
public class FeedEntryContent extends AbstractModel {

	@Column(length = 2048)
	private String title;

	@Column(length = 40)
	private String titleHash;

	@Lob
	@Column(length = Integer.MAX_VALUE)
	private String content;

	@Column(length = 40)
	private String contentHash;

	@Column(name = "author", length = 128)
	private String author;

	@Column(length = 2048)
	private String enclosureUrl;

	@Column(length = 255)
	private String enclosureType;

	@OneToMany(mappedBy = "content")
	private Set<FeedEntry> entries;

}
