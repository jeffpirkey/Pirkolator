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

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.utility.common.UUIDs;

public class TestSubscriberGroup extends AbstractSubscriber {

    private final Class<?> type;
    @Nullable
    private Message message;
    private volatile CountDownLatch latch = new CountDownLatch (1);

    public TestSubscriberGroup () {

        this (Serializable.class);
    }

    public TestSubscriberGroup (final Class<?> type) {

        super (UUIDs.generateUUID (), "Test Subscriber");
        this.type = type;
    }

    @Override
    public void receive (final Message rxMessage) {

        message = rxMessage;
        latch.countDown ();
    }

    @Nullable
    public Message awaitMessage (final long time) throws InterruptedException {

        latch.await (time, TimeUnit.MILLISECONDS);

        // Reset the latch for the next run
        latch = new CountDownLatch (1);

        return message;
    }

    @Override
    public void doConstruct (final Subscription sub) {

        sub.addFilter (new TypeFilter (type));
        sub.setGroupName ("test");
    }
}
