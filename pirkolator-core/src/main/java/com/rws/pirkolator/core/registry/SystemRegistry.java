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
package com.rws.pirkolator.core.registry;

import static com.rws.utility.common.Preconditions.notNull;

import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstanceNotActiveException;
import com.rws.pirkolator.core.analytic.IAnalytic;
import com.rws.pirkolator.core.data.access.IDao;
import com.rws.pirkolator.core.engine.IPublisher;
import com.rws.pirkolator.core.engine.ISubscriber;
import com.rws.pirkolator.core.store.AnalyticViewStore;
import com.rws.pirkolator.core.store.DalViewStore;
import com.rws.pirkolator.core.store.PublisherViewStore;
import com.rws.pirkolator.core.store.SubscriberViewStore;
import com.rws.pirkolator.core.store.SystemViewStore;
import com.rws.pirkolator.core.transform.AnalyticToViewFunction;
import com.rws.pirkolator.core.transform.DalToViewFunction;
import com.rws.pirkolator.core.transform.PublisherToViewFunction;
import com.rws.pirkolator.core.transform.SubscriberToViewFunction;
import com.rws.pirkolator.model.SystemInfo;
import com.rws.pirkolator.schema.ISystemIdentifiable;
import com.rws.pirkolator.view.model.AnalyticView;
import com.rws.pirkolator.view.model.DalView;
import com.rws.pirkolator.view.model.PublisherView;
import com.rws.pirkolator.view.model.SubscriberView;
import com.rws.pirkolator.view.model.SystemView;
import com.rws.utility.common.UUIDs;

public class SystemRegistry {

    private static final Logger LOG = notNull (LoggerFactory.getLogger (SystemRegistry.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** define **/
    @Resource
    @Nullable
    private AnalyticViewStore analyticStore;
    private final Set<UUID> localAnalyticIdSet = Sets.newConcurrentHashSet ();

    @Resource
    @Nullable
    private DalViewStore dalStore;
    private final Set<UUID> localDalIdSet = Sets.newConcurrentHashSet ();

    @Resource
    @Nullable
    private PublisherViewStore publisherStore;
    private final Set<UUID> localPublisherIdSet = Sets.newConcurrentHashSet ();

    @Resource
    @Nullable
    private SubscriberViewStore subscriberStore;
    private final Set<UUID> localSubscriberIdSet = Sets.newConcurrentHashSet ();

    @Resource
    @Nullable
    private SystemViewStore systemStore;

    private final SystemInfo systemInfo;

    /** Transforms **/
    @Resource (name = "publisherToViewFunction")
    @Nullable
    PublisherToViewFunction publisherToView;

    @Resource (name = "subscriberToViewFunction")
    @Nullable
    SubscriberToViewFunction subscriberToView;

    @Resource (name = "analyticToViewFunction")
    @Nullable
    AnalyticToViewFunction analyticToView;

    @Resource (name = "dalToViewFunction")
    @Nullable
    DalToViewFunction dalToView;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public SystemRegistry (final SystemInfo systemInfo) {

        super ();

        this.systemInfo = systemInfo;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    /**
     * @return {@link SystemInfo} of this instance of the Pirkolator
     */
    public SystemInfo getSystemInfo () {

        return systemInfo;
    }

    AnalyticViewStore getAnalyticViewStore () {

        return notNull (analyticStore, "The Analytic View Store in the System Registry has not been defined."
                + " Check the Spring XML system files for proper configuration.");
    }

    AnalyticToViewFunction getAnalyticToViewFunction () {

        return notNull (analyticToView, "The Analytic to View transform function has not been defined."
                + " Chech the Spring XML system files for proper configuration.");
    }

    DalViewStore getDalViewStore () {

        return notNull (dalStore, "The DAL View Store in the System Registry has not been defined."
                + " Check the Spring XML system files for proper configuration.");
    }

    DalToViewFunction getDalToViewFunction () {

        return notNull (dalToView, "The DAL to View transform function has not been defined."
                + " Chech the Spring XML system files for proper configuration.");
    }

    PublisherViewStore getPublisherViewStore () {

        return notNull (publisherStore, "The Publisher View Store in the System Registry has not been defined."
                + " Check the Spring XML system files for proper configuration.");
    }

    PublisherToViewFunction getPublisherToViewFunction () {

        return notNull (publisherToView, "The Publisher to View transform function has not been defined."
                + " Chech the Spring XML system files for proper configuration.");
    }

    SystemViewStore getSystemViewStore () {

        return notNull (systemStore, "The System View Store in the System Registry has not been defined."
                + " Check the Spring XML system files for proper configuration.");
    }

    
    SubscriberViewStore getSubscriberViewStore () {

        return notNull (subscriberStore, "The Subscriber View Store in the System Registry has not been defined."
                + " Check the Spring XML system files for proper configuration.");
    }

    SubscriberToViewFunction getSubscriberToViewFunction () {

        return notNull (subscriberToView, "The Subscriber to View transform function has not been defined."
                + " Chech the Spring XML system files for proper configuration.");
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    /***** System Identifiable *****/
    public void register (final ISystemIdentifiable component) {

        if (component instanceof ISubscriber) {
            registerSubscriber ((ISubscriber) component);
        }

        if (component instanceof IPublisher) {
            registerPublisher ((IPublisher) component);
        }

        if (component instanceof IAnalytic) {
            registerAnalytic ((IAnalytic) component);
        }

        if (component instanceof IDao) {
            registerDao ((IDao) component);
        }
    }

    public void unregister (final ISystemIdentifiable component) {

        if (component instanceof ISubscriber) {
            unregisterSubscriber ((ISubscriber) component);
        }

        if (component instanceof IPublisher) {
            unregisterPublisher ((IPublisher) component);
        }

        if (component instanceof IAnalytic) {
            unregisterAnalytic ((IAnalytic) component);
        }

        if (component instanceof IDao) {
            unregisterDao ((IDao) component);
        }
    }

    /***** Publishers *****/

    void registerPublisher (final IPublisher publisher) {

        final PublisherView view = getPublisherToViewFunction ().apply (publisher);
        localPublisherIdSet.add (publisher.getId ());
        getPublisherViewStore ().put (view);
    }

    void unregisterPublisher (final IPublisher publisher) {

        localPublisherIdSet.remove (publisher.getId ());
        final String tmpId = publisher.getId ().toString ();
        if (tmpId != null && !tmpId.isEmpty ()) {
            getPublisherViewStore ().removeById (tmpId);
        } else {
            LOG.warn ("Cannot remove a null or empty String id from the publisher store.");
        }
    }

    /***** Subscribers *****/

    void registerSubscriber (final ISubscriber subscriber) {

        final SubscriberView view = getSubscriberToViewFunction ().apply (subscriber);
        localPublisherIdSet.add (subscriber.getId ());
        getSubscriberViewStore ().put (view);
    }

    void unregisterSubscriber (final ISubscriber subscriber) {

        localSubscriberIdSet.remove (subscriber.getId ());
        final String tmpId = subscriber.getId ().toString ();
        if (tmpId != null && !tmpId.isEmpty ()) {
            getSubscriberViewStore ().removeById (tmpId);
        } else {
            LOG.warn ("Cannot remove a null or empty String id from the subscriber store.");
        }
    }

    /***** Analytics *****/

    void registerAnalytic (final IAnalytic analytic) {

        final AnalyticView view = getAnalyticToViewFunction ().apply (analytic);
        localAnalyticIdSet.add (analytic.getId ());
        getAnalyticViewStore ().put (view);
    }

    void unregisterAnalytic (final IAnalytic analytic) {

        localAnalyticIdSet.remove (analytic.getId ());
        final String tmpId = analytic.getId ().toString ();
        if (tmpId != null && !tmpId.isEmpty ()) {
            getAnalyticViewStore ().removeById (tmpId);
        } else {
            LOG.warn ("Cannot remove a null or empty String id from the analytic store.");
        }
    }

    /***** DAOs *****/

    void registerDao (final IDao dao) {

        final DalView view = getDalToViewFunction ().apply (dao);
        localDalIdSet.add (dao.getId ());
        getDalViewStore ().put (view);
    }

    void unregisterDao (final IDao dao) {

        localDalIdSet.remove (dao.getId ());
        final String tmpId = dao.getId ().toString ();
        if (tmpId != null && !tmpId.isEmpty ()) {
            getDalViewStore ().removeById (tmpId);
        } else {
            LOG.warn ("Cannot remove a null or empty String id from the dal store.");
        }
    }

    // *************************************************************************
    // ** Life-cycle methods 
    // *************************************************************************

    @PostConstruct
    void postConstruct () {

        final SystemView view =
                new SystemView (UUIDs.uuidAsStringOrDefault (systemInfo.getId ()), systemInfo.getName (), systemInfo
                        .getMetadata ().getMap ());

        getSystemViewStore().put (view);
    }

    @PreDestroy
    void preDestroy () {

        final String tmpId = systemInfo.getId ().toString ();
        if (tmpId != null && !tmpId.isEmpty ()) {
            try {
            getSystemViewStore().removeById (tmpId);
            } catch (final HazelcastInstanceNotActiveException ex) {
                LOG.warn ("Hazelcast instance has already been shut down");
            }
        } else {
            LOG.warn ("Cannot remove a null or empty String id from the system store.");
        }
    }
}
