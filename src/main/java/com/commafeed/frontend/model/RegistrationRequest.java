package com.commafeed.frontend.model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class RegistrationRequest implements Serializable {

	private String name;
	private String password;
	private String email;
	private boolean googleImport = true;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isGoogleImport() {
		return googleImport;
	}

	public void setGoogleImport(boolean googleImport) {
		this.googleImport = googleImport;
	}

}
