package com.commafeed.backend.feed;

import io.dropwizard.lifecycle.Managed;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.SessionFactory;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.dao.FeedDAO;
import com.commafeed.backend.dao.UnitOfWork;

/**
 * Infinite loop fetching feeds from @FeedQueues and queuing them to the {@link FeedRefreshWorker} pool.
 * 
 */
@Slf4j
public class FeedRefreshTaskGiver implements Managed {

	private final SessionFactory sessionFactory;
	private final FeedQueues queues;
	private final FeedRefreshWorker worker;

	private ExecutorService executor;

	private Meter feedRefreshed;
	private Meter threadWaited;

	@Inject
	public FeedRefreshTaskGiver(SessionFactory sessionFactory, FeedQueues queues, FeedDAO feedDAO, FeedRefreshWorker worker,
			CommaFeedConfiguration config, MetricRegistry metrics) {
		this.sessionFactory = sessionFactory;
		this.queues = queues;
		this.worker = worker;

		executor = Executors.newFixedThreadPool(1);
		feedRefreshed = metrics.meter(MetricRegistry.name(getClass(), "feedRefreshed"));
		threadWaited = metrics.meter(MetricRegistry.name(getClass(), "threadWaited"));
	}

	@Override
	public void stop() {
		executor.shutdownNow();
		while (!executor.isTerminated()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("interrupted while waiting for threads to finish.");
			}
		}
	}

	@Override
	public void start() {
		log.info("starting feed refresh task giver");
		executor.execute(new Runnable() {
			@Override
			public void run() {
				while (!executor.isShutdown()) {
					try {
						FeedRefreshContext context = new UnitOfWork<FeedRefreshContext>(sessionFactory) {
							@Override
							protected FeedRefreshContext runInSession() throws Exception {
								FeedRefreshContext context = queues.take();
								if (context != null) {
									feedRefreshed.mark();
									worker.updateFeed(context);
								}
								return context;
							}
						}.run();
						if (context == null) {
							log.debug("nothing to do, sleeping for 15s");
							threadWaited.mark();
							try {
								Thread.sleep(15000);
							} catch (InterruptedException e) {
								log.error("interrupted while sleeping");
							}
						}
					} catch (Exception e) {
						log.error(e.getMessage(), e);
					}
				}
			}
		});
	}
}
