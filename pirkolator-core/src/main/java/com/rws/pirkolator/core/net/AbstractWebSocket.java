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
package com.rws.pirkolator.core.net;

import static com.rws.utility.common.Preconditions.notNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import com.rws.pirkolator.core.engine.IPublisher;
import com.rws.pirkolator.core.engine.ISubscriber;
import com.rws.pirkolator.core.engine.Publication;
import com.rws.pirkolator.core.engine.Subscription;
import com.rws.pirkolator.core.engine.channel.IPubSubChannel;
import com.rws.pirkolator.model.AbstractSystemIdentifiable;
import com.rws.pirkolator.model.Message;
import com.rws.utility.common.UUIDs;

/**
 * This abstract implementation provides basic utility supporting an analytic
 * attached to the infrastructure. This class provides implementation for both
 * the {@link IPublisher} and {@link Subscription} interfaces.
 * 
 * @author jpirkey
 * @since 1.0.0
 */
public abstract class AbstractWebSocket extends AbstractSystemIdentifiable implements IPublisher, ISubscriber {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** pub/sub channel **/
    @Nullable
    private IPubSubChannel channel;

    /** define **/
    private final Publication publication;
    private final Subscription subscription;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    protected AbstractWebSocket (final UUID id) {

        this (id, "Web Socket");
    }

    protected AbstractWebSocket (final UUID id, final String name) {

        super (id, name);

        publication = new Publication (getName () + " Publication");
        subscription = new Subscription (getName () + " Subscription", UUIDs.toString (id));
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public IPubSubChannel getChannel () {

        return notNull (channel, "Pub/Sub Channel is undefined for analytic."
                + " This indicates that the analytic has not be properly prepared by Spring");
    }

    @Override
    public Publication getPublication () {

        return publication;
    }

    @Override
    public Subscription getSubscription () {

        return subscription;
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
    public void receive (final Message message) {

        // By default do nothing
    }

    public void publish (final Serializable object) {

        getChannel ().publish (object);
    }

    public void publish (final Serializable object, final boolean copy) {

        getChannel ().publish (object, copy);
    }

    public void publishAll (final Collection<Serializable> collection) {

        getChannel ().publishAll (collection);
    }

    // *************************************************************************
    // ** Life-cycle
    // *************************************************************************

    @PostConstruct
    public void postConstruct () {

        doConstruct (publication, subscription);
    }

    @Override
    public void prepare (final IPubSubChannel myChannel) {

        channel = myChannel;
    }

    public abstract void doConstruct (Publication pub, Subscription sub);
}
