package com.commafeed.backend.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.commafeed.backend.model.User;
import com.commafeed.backend.security.PasswordEncryptionService;
import com.commafeed.frontend.utils.ModelFactory.MF;
import com.google.common.collect.Iterables;

@Stateless
@SuppressWarnings("serial")
public class UserService extends GenericDAO<User, Long> {

	@Inject
	PasswordEncryptionService encryptionService;

	public User login(String name, String password) {
		List<User> users = findByField(MF.i(MF.p(User.class).getName()), name);
		User user = Iterables.getFirst(users, null);
		if (user != null && !user.isDisabled()) {
			boolean authenticated = encryptionService.authenticate(password,
					user.getPassword(), user.getSalt());
			if (authenticated) {
				return user;
			}
		}

		return null;
	}
}
