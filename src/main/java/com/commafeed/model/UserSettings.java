package com.commafeed.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "USERSETTINGS")
@SuppressWarnings("serial")
public class UserSettings extends AbstractModel {

	public enum ReadingMode {
		ALL, UNREAD
	}

	@Column(name = "user_id")
	private User user;

	@Enumerated(EnumType.STRING)
	private ReadingMode readingMode;

	public ReadingMode getReadingMode() {
		return readingMode;
	}

	public void setReadingMode(ReadingMode readingMode) {
		this.readingMode = readingMode;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
