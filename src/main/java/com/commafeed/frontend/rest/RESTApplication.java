package com.commafeed.frontend.rest;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.google.common.collect.Sets;

@ApplicationPath("/rest")
public class RESTApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> set = Sets.newHashSet();
		set.add(JSONMessageBodyWriter.class);
		
		set.add(SubscriptionsREST.class);
		set.add(EntriesREST.class);
		return set;
	}
}
