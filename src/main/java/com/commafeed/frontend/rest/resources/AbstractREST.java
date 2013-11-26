package com.commafeed.frontend.rest.resources;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.crypt.Base64;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.backend.dao.UserDAO;
import com.commafeed.backend.model.User;
import com.commafeed.backend.model.UserRole.Role;
import com.commafeed.backend.services.FeedSubscriptionService;
import com.commafeed.frontend.CommaFeedApplication;
import com.commafeed.frontend.CommaFeedSession;
import com.commafeed.frontend.SecurityCheck;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityCheck(Role.USER)
@Slf4j
public abstract class AbstractREST {

	@Context
	private HttpServletRequest request;

	@Context
	private HttpServletResponse response;

	@Inject
	MetricRegistry metrics;

	@Inject
	private UserDAO userDAO;

	@Inject
	FeedSubscriptionService feedSubscriptionService;

	@PostConstruct
	public void init() {
		CommaFeedApplication app = CommaFeedApplication.get();
		ServletWebRequest swreq = new ServletWebRequest(request, "");
		ServletWebResponse swresp = new ServletWebResponse(swreq, response);
		RequestCycle cycle = app.createRequestCycle(swreq, swresp);
		ThreadContext.setRequestCycle(cycle);
		CommaFeedSession session = (CommaFeedSession) app.fetchCreateAndSetSession(cycle);

		if (session.getUser() == null) {
			cookieLogin(app, session);
		}
		if (session.getUser() == null) {
			basicHttpLogin(swreq, session);
		}
	}

	private void cookieLogin(CommaFeedApplication app, CommaFeedSession session) {
		IAuthenticationStrategy authenticationStrategy = app.getSecuritySettings().getAuthenticationStrategy();
		String[] data = authenticationStrategy.load();
		if (data != null && data.length > 1) {
			session.signIn(data[0], data[1]);
		}
	}

	private void basicHttpLogin(ServletWebRequest req, CommaFeedSession session) {
		String value = req.getHeader(HttpHeaders.AUTHORIZATION);
		if (value != null && value.startsWith("Basic ")) {
			value = value.substring(6);
			String decoded = new String(Base64.decodeBase64(value));
			String[] data = decoded.split(":");
			if (data != null && data.length > 1) {
				session.signIn(data[0], data[1]);
			}
		}
	}

	private void apiKeyLogin() {
		String apiKey = request.getParameter("apiKey");
		User user = userDAO.findByApiKey(apiKey);
		CommaFeedSession.get().setUser(user);
	}

	protected User getUser() {
		return CommaFeedSession.get().getUser();
	}

	@AroundInvoke
	public Object intercept(InvocationContext context) throws Exception {
		startFullRefresh(getUser());

		Method method = context.getMethod();

		// check security
		boolean allowed = true;
		User user = null;

		SecurityCheck check = method.isAnnotationPresent(SecurityCheck.class) ? method.getAnnotation(SecurityCheck.class) : method
				.getDeclaringClass().getAnnotation(SecurityCheck.class);

		if (check != null) {
			user = getUser();
			if (user == null && check.apiKeyAllowed()) {
				apiKeyLogin();
				user = getUser();
			}

			allowed = checkRole(check.value());
		}
		if (!allowed) {
			if (user == null) {
				return Response.status(Status.UNAUTHORIZED).entity("You are not authorized to do this.").build();
			} else {
				return Response.status(Status.FORBIDDEN).entity("You are not authorized to do this.").build();
			}

		}

		Object result = null;
		com.codahale.metrics.Timer.Context timer = metrics.timer(
				MetricRegistry.name(method.getDeclaringClass(), method.getName(), "responseTime")).time();
		try {
			result = context.proceed();
		} finally {
			timer.stop();
		}

		return result;
	}

	private void startFullRefresh(User user) {
		if (user == null)
			return;

		Date now = new Date();
		if (user.getLastFullRefresh() == null || (now.getTime() - user.getLastFullRefresh().getTime()) > TimeUnit.MINUTES.toMillis(30)) {
			log.info("Starting full refresh for {}", user.getName());
			user.setLastFullRefresh(now);
			userDAO.saveOrUpdate(user);
			feedSubscriptionService.refreshAll(user);
		}
	}

	private boolean checkRole(Role requiredRole) {
		if (requiredRole == Role.NONE) {
			return true;
		}

		Roles roles = CommaFeedSession.get().getRoles();
		boolean authorized = roles.hasAnyRole(new Roles(requiredRole.name()));
		return authorized;
	}

}
