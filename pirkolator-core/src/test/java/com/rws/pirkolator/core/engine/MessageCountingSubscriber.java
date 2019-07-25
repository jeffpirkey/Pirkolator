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

import static com.rws.utility.common.Preconditions.notNull;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.pirkolator.model.AbstractSystemIdentifiable;
import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.utility.common.UUIDs;

public class MessageCountingSubscriber extends AbstractSystemIdentifiable implements ISubscriber {

    private final static Logger LOG = notNull (LoggerFactory.getLogger (Hub.class));

    Subscription subscription;
    CountDownLatch latch;

    public MessageCountingSubscriber () {

        super (UUIDs.generateUUID (), "Test Subscriber");
        subscription = new Subscription ("Test Subscription", UUIDs.toString (getId()));
        subscription.addFilter (new TypeFilter (Serializable.class));
        latch = new CountDownLatch (1);
    }

    public MessageCountingSubscriber (final Class<?> type) {

        super (UUIDs.generateUUID (), "Test Subscriber");
        subscription = new Subscription ("Test Subscription", UUIDs.toString (getId()));
        subscription.addFilter (new TypeFilter (type));
        latch = new CountDownLatch (1);
    }

    @Override
    public Subscription getSubscription () {

        return subscription;
    }

    public void setSubscription (final Subscription sub) {

        subscription = sub;
    }

    public synchronized void startCount (final int max) {

        latch = new CountDownLatch (max);
    }

    @Override
    public void receive (final Message message) {

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("Received message {}", message);
        }

        latch.countDown ();
    }

    public synchronized void await () throws InterruptedException {

        latch.await ();
    }
}
