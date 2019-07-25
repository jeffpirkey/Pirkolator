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

import java.util.UUID;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.rws.pirkolator.core.engine.listener.IShutdownListener;
import com.rws.pirkolator.core.engine.listener.IStartListener;
import com.rws.pirkolator.model.AbstractSystemIdentifiable;
import com.rws.pirkolator.model.Message;
import com.rws.utility.common.UUIDs;

/**
 * This abstract implementation provides basic utility supporting a publisher
 * attached to the infrastructure.
 * 
 * @author jpirkey
 * @since 1.0.0
 */
public abstract class AbstractSubscriber extends AbstractSystemIdentifiable implements ISubscriber, IStartListener,
        IShutdownListener {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Resource
    @Nullable
    LocalPubSub localPubSub;

    /** define **/
    private Subscription subscription;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    protected AbstractSubscriber (final UUID id) {

        this (id, "Subscriber");
    }

    protected AbstractSubscriber (final UUID id, final String name) {

        super (id, name);

        subscription = new Subscription (getName () + " Subscription", UUIDs.toString (id));
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public Subscription getSubscription () {

        return subscription;
    }

    public void setSubscription (final Subscription sub) {

        subscription = sub;
        notNull (localPubSub).registerSubscriber (this);
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    @Override
    public void receive (final Message message) {

        // Do nothing
    }

    // *************************************************************************
    // ** Life-cycle
    // *************************************************************************

    @PostConstruct
    private void postConstruct () {

        doConstruct (subscription);
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
     * This method provides the {@link Subscription} instance for
     * this implementation during the PostConstruct life-cycle phase.
     */
    public abstract void doConstruct (Subscription sub);
}
