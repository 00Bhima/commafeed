package com.commafeed.frontend.pages.components;

import java.util.Arrays;

import javax.inject.Inject;

import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.EmailTextField;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.StringValidator;

import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.ApplicationSettingsService;
import com.commafeed.backend.services.UserService;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.model.RegistrationRequest;
import com.commafeed.frontend.pages.GoogleImportRedirectPage;
import com.commafeed.frontend.utils.ModelFactory.MF;

@SuppressWarnings("serial")
public class RegisterPanel extends Panel {

	@Inject
	UserDAO userDAO;

	@Inject
	UserService userService;

	@Inject
	ApplicationSettingsService applicationSettingsService;

	public RegisterPanel(String markupId) {
		super(markupId);

		IModel<RegistrationRequest> model = Model.of(new RegistrationRequest());

		Form<RegistrationRequest> form = new StatelessForm<RegistrationRequest>(
				"form", model) {
			@Override
			protected void onSubmit() {
				if (applicationSettingsService.get().isAllowRegistrations()) {
					RegistrationRequest req = getModelObject();
					userService.register(req.getName(), req.getPassword(),
							Arrays.asList(Role.USER));
					IAuthenticationStrategy strategy = getApplication()
							.getSecuritySettings().getAuthenticationStrategy();
					strategy.save(req.getName(), req.getPassword());
					CommaFeedSession.get().signIn(req.getName(),
							req.getPassword());

					if (req.isGoogleImport()) {
						setResponsePage(GoogleImportRedirectPage.class);
					} else {
						setResponsePage(getApplication().getHomePage());
					}
				} else {
					setResponsePage(getApplication().getHomePage());
				}
			}
		};
		add(form);
		add(new BootstrapFeedbackPanel("feedback",
				new ContainerFeedbackMessageFilter(form)));

		RegistrationRequest p = MF.p(RegistrationRequest.class);
		form.add(new RequiredTextField<String>("name", MF.m(model, p.getName()))
				.add(StringValidator.lengthBetween(3, 32)).add(
						new IValidator<String>() {
							@Override
							public void validate(
									IValidatable<String> validatable) {
								String name = validatable.getValue();
								User user = userDAO.findByName(name);
								if (user != null) {
									validatable.error(new ValidationError(
											"Name is already taken."));
								}
							}
						}));
		form.add(new PasswordTextField("password", MF.m(model, p.getPassword()))
				.setResetPassword(false).add(StringValidator.minimumLength(6)));
		form.add(new EmailTextField("email", MF.m(model, p.getEmail())));
		form.add(new CheckBox("import", MF.m(model, p.isGoogleImport())));

	}
}
