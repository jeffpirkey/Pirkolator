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
package com.rws.pirkolator.core.engine;

import static com.rws.utility.common.Preconditions.notNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.rws.pirkolator.core.engine.channel.IPubSubChannel;
import com.rws.pirkolator.core.engine.listener.IShutdownListener;
import com.rws.pirkolator.core.engine.listener.IStartListener;
import com.rws.pirkolator.model.AbstractSystemIdentifiable;

/**
 * This abstract implementation provides basic utility supporting a publisher
 * attached to the infrastructure.
 * 
 * @author jpirkey
 * @since 1.0.0
 */
public abstract class AbstractPublisher extends AbstractSystemIdentifiable implements IPublisher, IStartListener,
        IShutdownListener {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Resource
    @Nullable
    private LocalPubSub localPubSub;
    
    /** pub/sub channel **/
    @Nullable
    private IPubSubChannel channel;

    /** define **/
    private Publication publication;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    protected AbstractPublisher (final UUID id) {

        this (id, "Publisher");
    }

    protected AbstractPublisher (final UUID id, final String name) {

        super (id, name);

        publication = new Publication (getName () + " Publication");
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public IPubSubChannel getChannel () {

        return notNull (channel, "Pub/Sub Channel is undefined for publisher."
                + " This indicates that the publisher has not be properly prepared by Spring");
    }

    @Override
    public Publication getPublication () {

        return publication;
    }
    
    public void setPublication (final Publication pub) {
        
        publication = pub;
        
        notNull(localPubSub).registerPublisher (this);
    }

    // *************************************************************************
    // ** Utility methods
    // *************************************************************************

    public void publish (final Serializable object) {

        getChannel ().publish (object);
    }

    public void publish (final Serializable object, final boolean copy) {

        getChannel ().publish (object, copy);
    }

    public void publishAll (final Collection<? extends Serializable> collection) {

        getChannel ().publishAll (collection);
    }

    public void transformPublish (final Serializable object) {

        transformPublish (object, false);
    }

    public void transformPublish (final Serializable object, final boolean copy) {

        getChannel ().transformPublish (object, copy);
    }

    public void transformPublish (final Serializable object, final boolean copy, final boolean publishOriginal) {

        getChannel ().transformPublish (object, copy, publishOriginal);
    }

    // *************************************************************************
    // ** Life-cycle
    // *************************************************************************

    @PostConstruct
    private void postConstruct () {

        doConstruct (publication);
    }

    @Override
    public void prepare (final IPubSubChannel myChannel) {

        channel = myChannel;
    }

    @Override
    public void started () {

        // Override this to be notified with the Hub has started
    }

    @Override
    public void shutdown () {

        // Override this to be notified with the Hub has started
    }

    /**
     * This method provides the {@link Publication} instance for
     * this implementation during the PostConstruct life-cycle phase.
     * 
     * @param pub
     */
    public abstract void doConstruct (Publication pub);
}
