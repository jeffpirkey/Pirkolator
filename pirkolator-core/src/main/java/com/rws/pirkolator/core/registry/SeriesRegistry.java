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
package com.rws.pirkolator.core.registry;

import static com.rws.utility.common.Preconditions.notNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.pirkolator.core.engine.SystemResourceManager;
import com.rws.pirkolator.core.metric.PeriodicSeriesManager;
import com.rws.pirkolator.core.metric.SeriesViewSynchronizer;
import com.rws.pirkolator.core.store.SeriesViewStore;
import com.rws.pirkolator.model.metric.Series;
import com.rws.pirkolator.schema.metric.ISeries;
import com.rws.utility.common.Globals;

public final class SeriesRegistry {

    private final static Logger LOG = notNull (LoggerFactory.getLogger (SeriesRegistry.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Resource
    @Nullable
    private SystemResourceManager resourceManager;

    @Resource
    @Nullable
    private SeriesViewStore seriesViewStore;

    /** <p>Series map
     * <br/><b>Key = series id {@link UUID}</b> **/
    private final Map<UUID, ISeries<?, ?>> seriesMap = new ConcurrentHashMap<> ();

    /** <p>SeriesViewSynchronizer map
     * <br/><b>Key = series name {@link UUID}</b> **/
    private final Map<UUID, ISeries<?, ?>> seriesViewSynchronizerMap = new ConcurrentHashMap<> ();

    @Nullable
    private PeriodicSeriesManager seriesManager;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public SeriesRegistry () {

        super ();
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Nullable
    public ISeries<?, ?> getSeries (final String seriesId) {

        return seriesMap.get (seriesId);
    }

    PeriodicSeriesManager getSeriesManager () {

        return notNull (seriesManager, "The Periodic Series Manager has not been defined."
                + " Check your Spring configuration to ensure that a SeriesViewStore bean has been defined."
                + " This component is required by the Periodic Series Manager and must be set in Spring.");
    }

    SeriesViewStore getSeriesViewStore () {

        return notNull (seriesViewStore, "The Series View Store has not been defined."
                + " Check your Spring configuration to ensure that a SeriesViewStore bean has been defined.");
    }

    SystemResourceManager getSystemResourceManager () {

        return notNull (resourceManager, "The System Resource Manager instance is undefined. "
                + "This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public <P, V> ISeries<P, V> registerSeries (final ISeries<P, V> series) {

        // Does the series already exist?
        if (seriesViewSynchronizerMap.containsKey (series.getId ())) {
            final ISeries<P, V> tmp = series.getClass ().cast (seriesViewSynchronizerMap.get (series.getId ()));
            if (tmp != null) {
                return tmp;
            }
        }

        seriesMap.put (series.getId (), series);

        // Create synchronizer
        final ISeries<P, V> sync = new SeriesViewSynchronizer<> (series, getSeriesViewStore ());

        // Register series name for other distributions to use
        seriesViewSynchronizerMap.put (series.getId (), sync);

        return sync;
    }

    public AtomicInteger createPeriodicSeries (final String sourceId, final String seriesLabel, final String seriesType,
            final String periodLabel, final String periodType, final String valueLabel, final String valueType,
            final long periodInMillis) {

        final ISeries<Long, Integer> series = new Series<> (sourceId);
        series.getPropertyMap ().put ("seriesLabel", seriesLabel);
        series.getPropertyMap().put ("seriesType", seriesType);
        series.getPropertyMap ().put ("periodLabel", periodLabel);
        series.getPropertyMap ().put ("periodType", periodType);
        series.getPropertyMap ().put ("valueLabel", valueLabel);
        series.getPropertyMap ().put ("valueType", valueType);
        final ISeries<Long, Integer> opt = registerSeries (series);
        return getSeriesManager ().addPeriodicCounter (opt, periodInMillis);
    }

    // *************************************************************************
    // ** Life-cycle methods 
    // *************************************************************************

    @PostConstruct
    private final void postConstruct () {

        seriesManager = new PeriodicSeriesManager (getSystemResourceManager ());
    }

    @PreDestroy
    private final void preDestroy () {

        if (LOG.isDebugEnabled ()) {

            if (seriesViewSynchronizerMap.isEmpty ()) {
                LOG.debug ("-- No series registered");
            } else {
                final StringBuilder builder = new StringBuilder ("Series");
                builder.append (Globals.NEW_LINE);
                for (final ISeries<?, ?> sync : seriesViewSynchronizerMap.values ()) {
                    builder.append (sync.toString ()).append (Globals.NEW_LINE);
                }
                LOG.debug (builder.toString ());
            }
        }
    }
}
