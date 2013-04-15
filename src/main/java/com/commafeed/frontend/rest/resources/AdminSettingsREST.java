package com.commafeed.frontend.rest.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.codehaus.enunciate.doc.DocumentationGroup;

import com.commafeed.backend.model.ApplicationSettings;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.frontend.SecurityCheck;

@SecurityCheck(Role.ADMIN)
@Path("admin/settings")
@DocumentationGroup("Application Settings")
public class AdminSettingsREST {

	@Inject
	ApplicationSettingsService applicationSettingsService;

	@Path("get")
	@GET
	public ApplicationSettings get() {
		return applicationSettingsService.get();
	}

	@Path("save")
	@POST
	public void save(ApplicationSettings settings) {
		applicationSettingsService.save(settings);
	}
}
