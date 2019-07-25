/*******************************************************************************
 * Copyright 2013 Reality Warp Software
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.rws.pirkolator.core.engine;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rws.utility.common.Globals;

/**
 * This class provides centralized resource management as needed by threaded tasks.
 * This class helps to prevent to many threads being allocated on a JVM.
 * 
 * @author jpirkey
 *
 */
public class SystemResourceManager {

    private static final Logger LOG = notNull (LoggerFactory.getLogger (SystemResourceManager.class));

    public static final long TOTAL_MEMORY = Runtime.getRuntime ().totalMemory ();
    public static final long MAX_MEMORY = Runtime.getRuntime ().maxMemory ();
    public static final int QUEUE_AVERAGE_SIZE = 1024 * 8;
    public static final int QUEUE_CAPACITY = (int) (Runtime.getRuntime ().maxMemory () / QUEUE_AVERAGE_SIZE);
    public static final int CORES = Runtime.getRuntime ().availableProcessors ();

    static {

        LOG.info (new StringBuilder (Globals.DASH_LINE).append ("SystemInfo Resources:").append (Globals.NEW_LINE)
                .append ("- Total memory = ").append (TOTAL_MEMORY).append (Globals.NEW_LINE)
                .append ("- Max memory = ").append (MAX_MEMORY).append (Globals.NEW_LINE)
                .append ("- Allocated task size (bytes) = ").append (QUEUE_AVERAGE_SIZE).append (Globals.NEW_LINE)
                .append ("- Allocated task capacity (items) = ").append (QUEUE_CAPACITY).append (Globals.NEW_LINE)
                .append ("- Allocated cores = ").append (CORES).append (Globals.DASH_LINE).toString ());
    }

    public final Map<String, ExecutorService> sCachedPoolMap = new ConcurrentHashMap<> ();
    public final Map<String, ExecutorService> sFixedPoolMap = new ConcurrentHashMap<> ();
    public final Map<String, ScheduledExecutorService> sScheduledPoolMap = new ConcurrentHashMap<> ();
    public final ExecutorService sCachedThreadPool;

    private static ThreadFactory buildThreadFactory (final String serviceName) {

        ThreadFactoryBuilder builder = new ThreadFactoryBuilder ();
        builder = new ThreadFactoryBuilder ();
        builder.setNameFormat (serviceName + "-%d");

        return builder.build ();
    }

    public SystemResourceManager () {

        super ();

        ThreadFactoryBuilder builder = new ThreadFactoryBuilder ();
        builder = new ThreadFactoryBuilder ();
        builder.setNameFormat ("pirkolator-task-pool-%d");

        final ExecutorService service = Executors.newCachedThreadPool (builder.build ());
        sCachedThreadPool = checkNotNull (service);
    }

    public ExecutorService getSingleThreadExecutor (final String serviceName) {

        if (sCachedPoolMap.containsKey (serviceName)) {
            final ExecutorService service = sCachedPoolMap.get (serviceName);
            return checkNotNull (service);
        }

        final ExecutorService service = Executors.newSingleThreadExecutor (buildThreadFactory (serviceName));
        checkNotNull (service);
        sCachedPoolMap.put (serviceName, service);

        return service;
    }

    public ExecutorService getSingleThreadExecutor (final String serviceName, final BlockingQueue<Runnable> queue) {

        if (sCachedPoolMap.containsKey (serviceName)) {
            final ExecutorService service = sCachedPoolMap.get (serviceName);
            return checkNotNull (service);
        }

        final ExecutorService service =
                new ThreadPoolExecutor (1, 1, 60, TimeUnit.SECONDS, queue, buildThreadFactory (serviceName));
        sCachedPoolMap.put (serviceName, service);

        return service;
    }

    public ExecutorService getSingleThreadExecutor (final String serviceName, final int queueCapacity) {

        if (sCachedPoolMap.containsKey (serviceName)) {
            final ExecutorService service = sCachedPoolMap.get (serviceName);
            return checkNotNull (service);
        }

        final ExecutorService service =
                new ThreadPoolExecutor (1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable> (queueCapacity),
                        buildThreadFactory (serviceName));
        sCachedPoolMap.put (serviceName, service);

        return service;
    }

    public ExecutorService getCachedThreadExecutor () {

        return sCachedThreadPool;
    }

    public ScheduledExecutorService getScheduledThreadExecutor (final String serviceName, final int corePoolSize) {

        if (sScheduledPoolMap.containsKey (serviceName)) {
            final ScheduledExecutorService service = sScheduledPoolMap.get (serviceName);
            return checkNotNull (service);
        }

        final ScheduledExecutorService service =
                Executors.newScheduledThreadPool (corePoolSize, buildThreadFactory (serviceName));
        checkNotNull (service);
        sScheduledPoolMap.put (serviceName, service);

        return service;
    }

    public ExecutorService getFixedThreadExecutor (final String serviceName, final int corePoolSize) {

        if (sFixedPoolMap.containsKey (serviceName)) {
            final ExecutorService service = sFixedPoolMap.get (serviceName);
            return checkNotNull (service);
        }

        final ExecutorService service = Executors.newFixedThreadPool (corePoolSize, buildThreadFactory (serviceName));
        checkNotNull (service);
        sFixedPoolMap.put (serviceName, service);

        return service;
    }

    public ExecutorService getFixedThreadExecutor (final String serviceName, final int corePoolSize,
            final int queueCapacity) {

        if (sFixedPoolMap.containsKey (serviceName)) {
            final ExecutorService service = sFixedPoolMap.get (serviceName);
            return checkNotNull (service);
        }

        final ExecutorService service =
                new ThreadPoolExecutor (corePoolSize, corePoolSize, 60, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable> (queueCapacity), buildThreadFactory (serviceName));

        sFixedPoolMap.put (serviceName, service);

        return service;
    }

    @PreDestroy
    public void shutdownAll () {

        for (final Entry<String, ExecutorService> entry : sCachedPoolMap.entrySet ()) {
            if (LOG.isTraceEnabled ()) {
                LOG.info ("Waiting for " + entry.getKey () + " task to shutdown...");
            }

            entry.getValue ().shutdown ();
            if (!entry.getValue ().isShutdown ()) {
                //entry.getValue ().awaitTermination (60, TimeUnit.SECONDS);
            }

            if (LOG.isInfoEnabled ()) {
                if (entry.getValue ().isShutdown ()) {
                    LOG.info ("Shutdown task " + entry.getKey ());
                } else {
                    // TODO jpirkey - should we do more to try to shutdown timed out threads?
                    LOG.warn ("Unable to shutdown " + entry.getKey () + " task.");
                }
            }
        }

        for (final Entry<String, ExecutorService> entry : sFixedPoolMap.entrySet ()) {
            if (LOG.isTraceEnabled ()) {
                LOG.info ("Waiting for " + entry.getKey () + " task to shutdown...");
            }

            entry.getValue ().shutdown ();
            if (!entry.getValue ().isShutdown ()) {
                //entry.getValue ().awaitTermination (60, TimeUnit.SECONDS);
            }

            if (LOG.isInfoEnabled ()) {
                if (entry.getValue ().isShutdown ()) {
                    LOG.info ("Shutdown task " + entry.getKey ());
                } else {
                    // TODO jpirkey - should we do more to try to shutdown timed out threads?
                    LOG.warn ("Unable to shutdown " + entry.getKey () + " task.");
                }
            }
        }

        for (final Entry<String, ScheduledExecutorService> entry : sScheduledPoolMap.entrySet ()) {
            if (LOG.isTraceEnabled ()) {
                LOG.info ("Waiting for " + entry.getKey () + " task to shutdown...");
            }

            entry.getValue ().shutdown ();
            if (!entry.getValue ().isShutdown ()) {
                //entry.getValue ().awaitTermination (60, TimeUnit.SECONDS);
            }

            if (LOG.isInfoEnabled ()) {
                if (entry.getValue ().isShutdown ()) {
                    LOG.info ("Shutdown task " + entry.getKey ());
                } else {
                    // TODO jpirkey - should we do more to try to shutdown timed out threads?
                    LOG.warn ("Unable to shutdown " + entry.getKey () + " task.");
                }
            }
        }

        if (LOG.isTraceEnabled ()) {
            LOG.info ("Waiting for cached thread pool to shutdown...");
        }

        sCachedThreadPool.shutdown ();
        if (sCachedThreadPool.isShutdown ()) {
            //sCachedThreadPool.awaitTermination (60, TimeUnit.SECONDS);
        }

        if (LOG.isInfoEnabled ()) {
            if (sCachedThreadPool.isShutdown ()) {
                LOG.info ("Shutdown cached thread pool");
            } else {
                LOG.warn ("Unable to shutdown cached thread pool");
            }
        }
    }
}
