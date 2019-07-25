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

/**
 * This unit test checks publishing between many publishers and many subscribers.
 * 
 * @author jpirkey
 *
 */
@DirtiesContext (classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration (locations = { "classpath:com/rws/pirkolator/core/engine/localPubSubManyPubManySubTypeTest-context.xml" })
public class LocalPubSubManyPubManySubTypeTest extends AbstractPirkolatorTest {

    @Resource (name = "testSubscriberA")
    @Nullable
    TestTypeSubscriber testSubscriberA;

    @Resource (name = "testSubscriberB1")
    @Nullable
    TestTypeSubscriber testSubscriberB1;

    @Resource (name = "testSubscriberB2")
    @Nullable
    TestTypeSubscriber testSubscriberB2;

    @Resource (name = "testSubscriberC")
    @Nullable
    TestTypeSubscriber testSubscriberC;

    @Resource (name = "testPublisherA")
    @Nullable
    TestTypePublisher testPublisherA;

    @Resource (name = "testPublisherB1")
    @Nullable
    TestTypePublisher testPublisherB1;

    @Resource (name = "testPublisherB2")
    @Nullable
    TestTypePublisher testPublisherB2;

    @Resource (name = "testPublisherC")
    @Nullable
    TestTypePublisher testPublisherC;

    @Test (timeout = 3000000)
    public void testPublishByReference () {

        final TestDataA txDataA = new TestDataA ();
        final TestDataB txDataB = new TestDataB ();
        final TestDataC txDataC = new TestDataC ();

        /** Send objects through supported publisher **/
        notNull (testPublisherA).publish (txDataA, false);
        assertMessageReceived (notNull (testSubscriberA), txDataA);
        assertMessageNotReceived (notNull (testSubscriberB1));
        assertMessageNotReceived (notNull (testSubscriberB2));
        assertMessageNotReceived (notNull (testSubscriberC));

        resetSubscribers ();
        notNull (testPublisherB1).publish (txDataB, false);
        assertMessageNotReceived (notNull (testSubscriberA));
        assertMessageReceived (notNull (testSubscriberB1), txDataB);
        assertMessageReceived (notNull (testSubscriberB2), txDataB);
        assertMessageNotReceived (notNull (testSubscriberC));

        resetSubscribers ();
        notNull (testPublisherB2).publish (txDataB, false);
        assertMessageNotReceived (notNull (testSubscriberA));
        assertMessageReceived (notNull (testSubscriberB1), txDataB);
        assertMessageReceived (notNull (testSubscriberB2), txDataB);
        assertMessageNotReceived (notNull (testSubscriberC));

        resetSubscribers ();
        notNull (testPublisherC).publish (txDataC, false);
        assertMessageNotReceived (notNull (testSubscriberA));
        assertMessageNotReceived (notNull (testSubscriberB1));
        assertMessageNotReceived (notNull (testSubscriberB2));
        assertMessageReceived (notNull (testSubscriberC), txDataC);
    }

    @Test (timeout = 30000)
    public void testUnmatchedPublishByReference () {

        /** Send objects through unsupported publisher **/
        final TestDataB txDataB = new TestDataB ();

        notNull (testPublisherA).publish (txDataB, false);
        //FIXME jpirkey How do we fix the problem of delivering unsupported types that are published on a channel?
        assertMessageReceived (notNull (testSubscriberA), txDataB);
        assertMessageNotReceived (notNull (testSubscriberB1));
        assertMessageNotReceived (notNull (testSubscriberB2));
        assertMessageNotReceived (notNull (testSubscriberC));
    }

    void resetSubscribers () {

        notNull (testSubscriberA).reset ();
        notNull (testSubscriberB1).reset ();
        notNull (testSubscriberB2).reset ();
        notNull (testSubscriberC).reset ();
    }

    void assertMessageReceived (final TestTypeSubscriber sub, final IIdentifiable txData) {

        Assert.assertTrue ("Expected await to be true, but was false", sub.awaitMessage (1000));

        final Message msg = sub.getMessage ();

        Assert.assertNotNull ("Expected Message was null", msg);
        Assert.assertEquals ("Expected only 1 object in message", 1, msg.get ().size ());

        final IIdentifiable rxData = msg.iterate (IIdentifiable.class).next ();

        Assert.assertNotNull ("Test data is null", rxData);
        Assert.assertEquals ("UUIDs the same", txData.getId (), rxData.getId ());
    }

    void assertMessageNotReceived (final TestTypeSubscriber sub) {

        Assert.assertFalse ("Expected await to be false, but was true", sub.awaitMessage (1000));

        final Message msg = sub.getMessage ();

        Assert.assertNull ("Expected Message was not null", msg);
    }
}
