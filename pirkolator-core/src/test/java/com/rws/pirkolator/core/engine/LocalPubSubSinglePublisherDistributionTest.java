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
import com.rws.utility.test.AbstractPirkolatorTest;

@DirtiesContext (classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration (locations = { "classpath:com/rws/pirkolator/core/engine/testSinglePublisherMultipleSubscribers-context.xml" })
public class LocalPubSubSinglePublisherDistributionTest extends AbstractPirkolatorTest {

    @Resource (name = "testSubscriber1")
    @Nullable
    TestTypeSubscriber testSubscriber1;

    @Resource (name = "testSubscriber2")
    @Nullable
    TestTypeSubscriber testSubscriber2;

    @Resource (name = "testSubscriber3")
    @Nullable
    TestTypeSubscriber testSubscriber3;

    @Resource
    @Nullable
    TestTypePublisher testPublisher;

    @Test
    public void testPublishByReference () {

        final TestData txData = new TestData ();
        notNull (testPublisher).publish (txData, false);

        assertMessageReceived (notNull (testSubscriber1), txData);
        assertMessageReceived (notNull (testSubscriber2), txData);
        assertMessageReceived (notNull (testSubscriber3), txData);
    }

    void assertMessageReceived (final TestTypeSubscriber sub, final TestData txData) {

        Assert.assertTrue ("Expected subscriber to have received Message, but it did not", sub.awaitMessage (1000));
        final Message msg = sub.getMessage ();

        Assert.assertNotNull ("Expected Response was null", msg);
        Assert.assertEquals ("Expected only 1 object in message", 1, msg.get ().size ());
        Assert.assertEquals ("Not passed by reference", false, msg.isCopy ());
        
        final TestData rxData = msg.iterate (TestData.class).next ();

        Assert.assertNotNull ("Expected Test Data to not be null", rxData);
        
        Assert.assertEquals ("UUIDs not the same", txData.getId (), rxData.getId ());
    }
}
