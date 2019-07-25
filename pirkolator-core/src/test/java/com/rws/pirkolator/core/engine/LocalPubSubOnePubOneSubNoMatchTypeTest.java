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

import javax.annotation.Nullable;
import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.schema.IIdentifiable;
import com.rws.utility.test.AbstractPirkolatorTest;

@DirtiesContext (classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration (locations = { "classpath:com/rws/pirkolator/core/engine/localPubSubOnePubOneSubNoMatchTypeTest-context.xml" })
public class LocalPubSubOnePubOneSubNoMatchTypeTest extends AbstractPirkolatorTest {

    @Resource (name = "testSubscriberA")
    @Nullable
    TestTypeSubscriber testSubscriberA;

    @Resource (name = "testPublisherB")
    @Nullable
    TestTypePublisher testPublisherB;

    @Test (timeout = 30000)
    public void testPublishByDefault () {

        final TestDataA txDataA = new TestDataA ();
        final TestDataB txDataB = new TestDataB ();

        // Should not get message because test publisher not registered to send it?
        notNull (testPublisherB).publish (txDataA);
        assertMessageNotReceived (notNull (testSubscriberA));

        // Should not get message because subscriber not register for TestDataA
        notNull (testPublisherB).publish (txDataB);
        assertMessageNotReceived (notNull (testSubscriberA));
    }

    @Test (timeout = 30000)
    public void testPublishByCopy () {

        final TestDataA txDataA = new TestDataA ();
        final TestDataB txDataB = new TestDataB ();

        // Should not get message because test publisher not registered to send it?
        notNull (testPublisherB).publish (txDataA, true);
        assertMessageNotReceived (notNull (testSubscriberA));

        // Should not get message because subscriber not register for TestDataA
        notNull (testPublisherB).publish (txDataB, true);
        assertMessageNotReceived (notNull (testSubscriberA));
    }

    @Test (timeout = 30000)
    public void testPublishByReference () {

        final TestDataA txDataA = new TestDataA ();
        final TestDataB txDataB = new TestDataB ();

        // Should not get message because test publisher not registered to send it?
        notNull (testPublisherB).publish (txDataA, false);
        assertMessageNotReceived (notNull (testSubscriberA));

        // Should not get message because subscriber not register for TestDataA
        notNull (testPublisherB).publish (txDataB, false);
        assertMessageNotReceived (notNull (testSubscriberA));
    }

    void assertMessageReceived (final TestTypeSubscriber sub, final IIdentifiable txData) {

        Assert.assertTrue ("Expected subscriber to have received Message", sub.awaitMessage (1000));
        final Message msg = sub.getMessage ();

        Assert.assertNotNull ("Expected Message was null", msg);
        Assert.assertEquals ("Expected only 1 object in message", 1, msg.get ().size ());

        final IIdentifiable rxData = msg.iterate (IIdentifiable.class).next ();

        Assert.assertNotNull ("Test data is null", rxData);
        Assert.assertEquals ("UUIDs the same", txData.getId (), rxData.getId ());
    }

    void assertMessageNotReceived (final TestTypeSubscriber sub) {

        Assert.assertFalse ("Expected subscriber to have not received Message", sub.awaitMessage (1000));

        final Message msg = sub.getMessage ();
        Assert.assertNull ("Expected Message was not null", msg);
    }
}
