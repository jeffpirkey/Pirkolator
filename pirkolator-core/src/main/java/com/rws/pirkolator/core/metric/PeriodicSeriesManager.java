/*******************************************************************************
 * Copyright 2014 Reality Warp Software
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
package com.rws.pirkolator.core.metric;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.rws.pirkolator.core.engine.SystemResourceManager;
import com.rws.pirkolator.schema.metric.ISeries;

/**
 * This class provides the thread management for the periodic counters used by
 * the metric subscribers and publishers.
 * 
 * @author jpirkey
 *
 */
public class PeriodicSeriesManager {

    /** class **/
    private final ScheduledExecutorService executioner;

    public PeriodicSeriesManager (final SystemResourceManager resourceManager) {

        super ();

        executioner = resourceManager.getScheduledThreadExecutor ("periodic-count-runner", 4);
    }

    public AtomicInteger addPeriodicCounter (final ISeries<Long, Integer> series, final long periodInMillis) {

        final PeriodicCountRunner runner = new PeriodicCountRunner (series);
        executioner.scheduleAtFixedRate (runner, periodInMillis, periodInMillis, TimeUnit.MILLISECONDS);
        
        return runner;
    }
}
