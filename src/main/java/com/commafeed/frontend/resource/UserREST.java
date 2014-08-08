package com.commafeed.frontend.resource;

import io.dropwizard.auth.Auth;
import io.dropwizard.hibernate.UnitOfWork;

import java.util.Arrays;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.AllArgsConstructor;

import org.apache.commons.lang.StringUtils;

import com.commafeed.CommaFeedApplication;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.dao.UserRoleDAO;
import com.commafeed.backend.dao.UserSettingsDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings.ReadingMode;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.backend.model.UserSettings.ViewMode;
import com.commafeed.backend.service.PasswordEncryptionService;
import com.commafeed.backend.service.UserService;
import com.commafeed.frontend.model.Settings;
import com.commafeed.frontend.model.UserModel;
import com.commafeed.frontend.model.request.ProfileModificationRequest;
import com.commafeed.frontend.model.request.RegistrationRequest;
import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/user")
@Api(value = "/user", description = "Operations about the user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@AllArgsConstructor
public class UserREST {

	private final UserDAO userDAO;
	private final UserRoleDAO userRoleDAO;
	private final UserSettingsDAO userSettingsDAO;
	private final UserService userService;
	private final PasswordEncryptionService encryptionService;

	@Path("/settings")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Retrieve user settings", notes = "Retrieve user settings", response = Settings.class)
	public Response getSettings(@Auth User user) {
		Settings s = new Settings();
		UserSettings settings = userSettingsDAO.findByUser(user);
		if (settings != null) {
			s.setReadingMode(settings.getReadingMode().name());
			s.setReadingOrder(settings.getReadingOrder().name());
			s.setViewMode(settings.getViewMode().name());
			s.setShowRead(settings.isShowRead());

			s.setEmail(settings.isEmail());
			s.setGmail(settings.isGmail());
			s.setFacebook(settings.isFacebook());
			s.setTwitter(settings.isTwitter());
			s.setGoogleplus(settings.isGoogleplus());
			s.setTumblr(settings.isTumblr());
			s.setPocket(settings.isPocket());
			s.setInstapaper(settings.isInstapaper());
			s.setBuffer(settings.isBuffer());
			s.setReadability(settings.isReadability());

			s.setScrollMarks(settings.isScrollMarks());
			s.setTheme(settings.getTheme());
			s.setCustomCss(settings.getCustomCss());
			s.setLanguage(settings.getLanguage());
			s.setScrollSpeed(settings.getScrollSpeed());
		} else {
			s.setReadingMode(ReadingMode.unread.name());
			s.setReadingOrder(ReadingOrder.desc.name());
			s.setViewMode(ViewMode.title.name());
			s.setShowRead(true);
			s.setTheme("default");

			s.setEmail(true);
			s.setGmail(true);
			s.setFacebook(true);
			s.setTwitter(true);
			s.setGoogleplus(true);
			s.setTumblr(true);
			s.setPocket(true);
			s.setInstapaper(true);
			s.setBuffer(true);
			s.setReadability(true);

			s.setScrollMarks(true);
			s.setLanguage("en");
			s.setScrollSpeed(400);
		}
		return Response.ok(s).build();
	}

	@Path("/settings")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Save user settings", notes = "Save user settings")
	public Response saveSettings(@Auth User user, @ApiParam(required = true) Settings settings) {
		Preconditions.checkNotNull(settings);

		UserSettings s = userSettingsDAO.findByUser(user);
		if (s == null) {
			s = new UserSettings();
			s.setUser(user);
		}
		s.setReadingMode(ReadingMode.valueOf(settings.getReadingMode()));
		s.setReadingOrder(ReadingOrder.valueOf(settings.getReadingOrder()));
		s.setShowRead(settings.isShowRead());
		s.setViewMode(ViewMode.valueOf(settings.getViewMode()));
		s.setScrollMarks(settings.isScrollMarks());
		s.setTheme(settings.getTheme());
		s.setCustomCss(settings.getCustomCss());
		s.setLanguage(settings.getLanguage());
		s.setScrollSpeed(settings.getScrollSpeed());

		s.setEmail(settings.isEmail());
		s.setGmail(settings.isGmail());
		s.setFacebook(settings.isFacebook());
		s.setTwitter(settings.isTwitter());
		s.setGoogleplus(settings.isGoogleplus());
		s.setTumblr(settings.isTumblr());
		s.setPocket(settings.isPocket());
		s.setInstapaper(settings.isInstapaper());
		s.setBuffer(settings.isBuffer());
		s.setReadability(settings.isReadability());

		userSettingsDAO.saveOrUpdate(s);
		return Response.ok().build();

	}

	@Path("/profile")
	@GET
	@UnitOfWork
	@ApiOperation(value = "Retrieve user's profile", response = UserModel.class)
	public Response get(@Auth User user) {
		UserModel userModel = new UserModel();
		userModel.setId(user.getId());
		userModel.setName(user.getName());
		userModel.setEmail(user.getEmail());
		userModel.setEnabled(!user.isDisabled());
		userModel.setApiKey(user.getApiKey());
		for (UserRole role : userRoleDAO.findAll(user)) {
			if (role.getRole() == Role.ADMIN) {
				userModel.setAdmin(true);
			}
		}
		return Response.ok(userModel).build();
	}

	@Path("/profile")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Save user's profile")
	public Response save(@Auth User user, @ApiParam(required = true) ProfileModificationRequest request) {
		Preconditions.checkArgument(StringUtils.isBlank(request.getPassword()) || request.getPassword().length() >= 6);
		if (StringUtils.isNotBlank(request.getEmail())) {
			User u = userDAO.findByEmail(request.getEmail());
			Preconditions.checkArgument(u == null || user.getId().equals(u.getId()));
		}

		if (CommaFeedApplication.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}

		user.setEmail(StringUtils.trimToNull(request.getEmail()));
		if (StringUtils.isNotBlank(request.getPassword())) {
			byte[] password = encryptionService.getEncryptedPassword(request.getPassword(), user.getSalt());
			user.setPassword(password);
			user.setApiKey(userService.generateApiKey(user));
		}
		if (request.isNewApiKey()) {
			user.setApiKey(userService.generateApiKey(user));
		}
		userDAO.merge(user);
		return Response.ok().build();
	}

	@Path("/register")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Register a new account")
	public Response register(@ApiParam(required = true) RegistrationRequest req) {
		try {
			userService.register(req.getName(), req.getPassword(), req.getEmail(), Arrays.asList(Role.USER));
			return Response.ok().build();
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}

	}

	@Path("/profile/deleteAccount")
	@POST
	@UnitOfWork
	@ApiOperation(value = "Delete the user account")
	public Response delete(@Auth User user) {
		if (CommaFeedApplication.USERNAME_ADMIN.equals(user.getName()) || CommaFeedApplication.USERNAME_DEMO.equals(user.getName())) {
			return Response.status(Status.FORBIDDEN).build();
		}
		userService.unregister(user);
		return Response.ok().build();
	}
}
