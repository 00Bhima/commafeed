package com.commafeed.frontend.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.RequiredArgsConstructor;

import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;

import com.commafeed.CommaFeedApplication;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.service.UserService;
import com.google.common.base.Optional;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

public class SecurityCheckProvider implements InjectableProvider<SecurityCheck, Parameter> {

	public static class SecurityCheckUserServiceProvider extends SingletonTypeInjectableProvider<Context, UserService> {

		public SecurityCheckUserServiceProvider(UserService userService) {
			super(UserService.class, userService);
		}
	}

	@RequiredArgsConstructor
	private static class SecurityCheckInjectable<T> extends AbstractHttpContextInjectable<User> {
		private static final String PREFIX = "Basic";

		private final HttpServletRequest request;
		private final UserService userService;
		private final Role role;
		private final boolean apiKeyAllowed;

		@Override
		public User getValue(HttpContext c) {
			Optional<User> user = cookieSessionLogin();
			if (!user.isPresent()) {
				user = basicAuthenticationLogin(c);
			}
			if (!user.isPresent()) {
				user = apiKeyLogin(c);
			}

			if (user.isPresent()) {
				return user.get();
			} else {
				throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED)
						.entity("Credentials are required to access this resource.").type(MediaType.TEXT_PLAIN_TYPE).build());
			}
		}

		private Optional<User> cookieSessionLogin() {
			HttpSession session = request.getSession(false);
			if (session != null) {
				User user = (User) session.getAttribute(CommaFeedApplication.SESSION_USER);
				if (user != null) {
					userService.afterLogin(user);
					return Optional.of(user);
				}
			}
			return Optional.absent();
		}

		private Optional<User> basicAuthenticationLogin(HttpContext c) {
			String header = c.getRequest().getHeaderValue(HttpHeaders.AUTHORIZATION);
			if (header != null) {
				int space = header.indexOf(' ');
				if (space > 0) {
					String method = header.substring(0, space);
					if (PREFIX.equalsIgnoreCase(method)) {
						String decoded = B64Code.decode(header.substring(space + 1), StringUtil.__ISO_8859_1);
						int i = decoded.indexOf(':');
						if (i > 0) {
							String username = decoded.substring(0, i);
							String password = decoded.substring(i + 1);
							Optional<User> user = userService.login(username, password);
							if (user.isPresent() && user.get().hasRole(role)) {
								return user;
							}
						}
					}
				}
			}
			return Optional.absent();
		}

		private Optional<User> apiKeyLogin(HttpContext c) {
			String apiKey = c.getUriInfo().getPathParameters().getFirst("apiKey");
			if (apiKey != null && apiKeyAllowed) {
				Optional<User> user = userService.login(apiKey);
				if (user.isPresent() && user.get().hasRole(role)) {
					return user;
				}
			}
			return Optional.absent();
		}
	}

	private HttpServletRequest request;
	private UserService userService;

	public SecurityCheckProvider(@Context HttpServletRequest request, @Context UserService userService) {
		this.request = request;
		this.userService = userService;
	}

	@Override
	public ComponentScope getScope() {
		return ComponentScope.PerRequest;
	}

	@Override
	public Injectable<?> getInjectable(ComponentContext ic, SecurityCheck sc, Parameter c) {
		return new SecurityCheckInjectable<>(request, userService, sc.value(), sc.apiKeyAllowed());
	}
}
