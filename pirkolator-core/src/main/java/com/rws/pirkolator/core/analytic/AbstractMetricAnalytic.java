/*******************************************************************************
 * Copyright 2013, Reality Warp Software
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
package com.rws.pirkolator.core.analytic;

import static com.rws.utility.common.Preconditions.notNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.rws.pirkolator.core.engine.IPublisher;
import com.rws.pirkolator.core.engine.Publication;
import com.rws.pirkolator.core.engine.Subscription;
import com.rws.pirkolator.core.engine.listener.IShutdownListener;
import com.rws.pirkolator.core.registry.SeriesRegistry;
import com.rws.pirkolator.model.Message;
import com.rws.utility.common.UUIDs;

/**
 * This abstract implementation provides basic utility supporting an analytic
 * attached to the infrastructure. This class provides implementation for both
 * the {@link IPublisher} and {@link Subscription}.
 * 
 * @author jpirkey
 * @since 1.0.0
 */
public abstract class AbstractMetricAnalytic extends AbstractAnalytic implements IShutdownListener {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** series **/
    @Resource
    @Nullable
    SeriesRegistry seriesRegistry;

    /** metrics **/
    @Nullable
    AtomicInteger receivedCounter;
    @Nullable
    AtomicInteger publishedCounter;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    protected AbstractMetricAnalytic (final UUID id) {

        this (id, "Metric Analytic");
    }

    protected AbstractMetricAnalytic (final UUID id, final String name) {

        super (id, name);
    }

    AtomicInteger getReceivedCounter () {

        return notNull (receivedCounter, "ReceivedCounter in analytic undefined. "
                + "This indicates that the analytic has not been "
                + "properly prepared during the Spring 'started' lifecycle.");
    }

    AtomicInteger getPublishedCounter () {

        return notNull (publishedCounter, "PublishedCounter in analytic undefined. "
                + "This indicates that the analytic has not been " + ""
                + "properly prepared during the Spring 'started' lifecycle.");
    }

    SeriesRegistry getSeriesRegistry () {

        return notNull (seriesRegistry, "Series Registry in analytic has not been defined. "
                + "This is an autowired @Resource and indicates that "
                + "the SeriesRegistry has not been defined in the Spring configuration.");
    }

    // *************************************************************************
    // ** Utility methods
    // *************************************************************************

    /**
     * By default, this method does nothing. This method should be overridden by
     * the implementing class to receive {@link Message} instances.
     * 
     */
    @Override
    public final void receive (final Message message) {

        getReceivedCounter ().incrementAndGet ();
        doReceive (message);
    }

    public void doReceive (@SuppressWarnings ("unused") final Message message) {

        // By default do nothing
    }

    private void defineSeries () {

        receivedCounter =
                getSeriesRegistry ().createPeriodicSeries (UUIDs.toString (getId ()), "Received Messages Count",
                        "receivedCounter", "Seconds", "date", "Count", "number", 15000);
        publishedCounter =
                getSeriesRegistry ().createPeriodicSeries (UUIDs.toString (getId ()), "Published Messages Count",
                        "publishedCounter", "Seconds", "date", "Count", "number", 15000);
    }

    @Override
    public void publish (final Serializable object) {

        super.publish (object);
        getPublishedCounter ().incrementAndGet ();
    }

    @Override
    public void publish (final Serializable object, final boolean copy) {

        super.publish (object, copy);
        getPublishedCounter ().incrementAndGet ();
    }

    @Override
    public void publishAll (final Collection<Serializable> collection) {

        super.publishAll (collection);
        getPublishedCounter ().addAndGet (collection.size ());
    }

    @Override
    public void transformPublish (final Serializable object) {

        transformPublish (object, false);
    }

    @Override
    public void transformPublish (final Serializable object, final boolean copy) {

        super.transformPublish (object, copy);
        getPublishedCounter ().incrementAndGet ();
    }

    @Override
    public void transformPublish (final Serializable object, final boolean copy, final boolean publishOriginal) {

        super.transformPublish (object, copy, publishOriginal);
        getPublishedCounter ().incrementAndGet ();
    }

    // *************************************************************************
    // ** Life-cycle
    // *************************************************************************

    @PostConstruct
    public void postConstruct () {

        defineSeries ();
    }

    @Override
    public void shutdown () {

        // By default don't need to do anything
    }

    @Override
    public abstract void doConstruct (Publication pub, Subscription sub);
}
