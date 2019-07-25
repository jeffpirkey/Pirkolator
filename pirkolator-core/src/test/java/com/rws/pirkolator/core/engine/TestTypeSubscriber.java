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
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.utility.common.UUIDs;

public class TestTypeSubscriber extends AbstractSubscriber {

    private static final Logger LOG = notNull (LoggerFactory.getLogger (TestTypeSubscriber.class));

    private final Class<?> type;
    @Nullable
    private Message message;
    private volatile CountDownLatch mLatch = new CountDownLatch (1);

    public TestTypeSubscriber () {

        this (Serializable.class);
    }

    public TestTypeSubscriber (final Class<?> type) {

        super (UUIDs.generateUUID (), "Test Subscriber");
        this.type = type;
    }

    @Override
    public void receive (final Message rxMessage) {

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("Received message on " + getName () + " " + rxMessage.toString ());
        }

        message = rxMessage;
        mLatch.countDown ();
    }

    @Nullable
    public Message getMessage () {

        return message;
    }

    public void reset () {

        message = null;
        mLatch = new CountDownLatch (1);
    }

    public boolean awaitMessage (final long time) {

        try {
            return mLatch.await (time, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ex) {
            LOG.warn ("Awaiting message interrupted");
        }

        return false;
    }

    @Override
    public void doConstruct (final Subscription sub) {

        sub.addFilter (new TypeFilter (type));
    }
}
