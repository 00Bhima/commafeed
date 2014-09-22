package com.commafeed;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.SessionFactory;

import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration.CacheType;
import com.commafeed.backend.cache.CacheService;
import com.commafeed.backend.cache.NoopCacheService;
import com.commafeed.backend.cache.RedisCacheService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

@RequiredArgsConstructor
@Slf4j
public class CommaFeedModule extends AbstractModule {

	@Getter(onMethod = @__({ @Provides }))
	private final SessionFactory sessionFactory;

	@Getter(onMethod = @__({ @Provides }))
	private final CommaFeedConfiguration config;

	@Getter(onMethod = @__({ @Provides }))
	private final MetricRegistry metrics;

	@Override
	protected void configure() {
		CacheService cacheService = config.getApplicationSettings().getCache() == CacheType.NOOP ? new NoopCacheService()
				: new RedisCacheService(config.getRedisPoolFactory().build());
		log.info("using cache {}", cacheService.getClass());
		bind(CacheService.class).toInstance(cacheService);
	}
}
