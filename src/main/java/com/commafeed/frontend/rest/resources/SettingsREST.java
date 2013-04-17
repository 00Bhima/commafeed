package com.commafeed.frontend.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.commafeed.backend.model.UserSettings;
import com.commafeed.backend.model.UserSettings.ReadingMode;
import com.commafeed.backend.model.UserSettings.ReadingOrder;
import com.commafeed.frontend.model.Settings;
import com.google.common.base.Preconditions;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/settings")
@Api(value = "/settings", description = "Operations about user settings")
public class SettingsREST extends AbstractREST {

	@Path("/get")
	@GET
	@ApiOperation(value = "Retrieve user settings", notes = "Retrieve user settings", responseClass = "com.commafeed.frontend.model.Settings")
	public Settings get() {
		Settings s = new Settings();
		UserSettings settings = userSettingsDAO.findByUser(getUser());
		if (settings != null) {
			s.setReadingMode(settings.getReadingMode().name());
			s.setReadingOrder(settings.getReadingOrder().name());
			s.setShowRead(settings.isShowRead());
			s.setCustomCss(settings.getCustomCss());
		} else {
			s.setReadingMode(ReadingMode.unread.name());
			s.setReadingOrder(ReadingOrder.desc.name());
			s.setShowRead(true);
		}
		return s;
	}

	@Path("/save")
	@POST
	@ApiOperation(value = "Save user settings", notes = "Save user settings")
	public Response save(@ApiParam Settings settings) {
		Preconditions.checkNotNull(settings);

		UserSettings s = userSettingsDAO.findByUser(getUser());
		if (s == null) {
			s = new UserSettings();
			s.setUser(getUser());
		}
		s.setReadingMode(ReadingMode.valueOf(settings.getReadingMode()));
		s.setReadingOrder(ReadingOrder.valueOf(settings.getReadingOrder()));
		s.setShowRead(settings.isShowRead());
		s.setCustomCss(settings.getCustomCss());
		userSettingsDAO.saveOrUpdate(s);
		return Response.ok(Status.OK).build();

	}
}
