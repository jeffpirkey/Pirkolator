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

import org.junit.Test;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

import com.rws.utility.test.AbstractPirkolatorTest;

@DirtiesContext (classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration (locations = { "classpath:com/rws/pirkolator/core/engine/testMultiplePublishersMultipleSubscribers-context.xml" })
public class LocalPubSubMultiplePublisherDistributionTest extends AbstractPirkolatorTest {

    @Resource (name = "testSubscriber1")
    @Nullable
    MessageCountingSubscriber testSubscriber1;

    @Resource (name = "testSubscriber2")
    @Nullable
    MessageCountingSubscriber testSubscriber2;

    @Resource (name = "testSubscriber3")
    @Nullable
    MessageCountingSubscriber testSubscriber3;

    @Resource (name = "testPublisher1")
    @Nullable
    TestTypePublisher testPublisher1;

    @Resource (name = "testPublisher2")
    @Nullable
    TestTypePublisher testPublisher2;

    @Resource (name = "testPublisher3")
    @Nullable
    TestTypePublisher testPublisher3;

    @Test (timeout = 30000)
    public void testPublishByReference () throws InterruptedException {

        notNull (testSubscriber1).startCount (3);
        notNull (testSubscriber2).startCount (3);
        notNull (testSubscriber3).startCount (3);

        final TestData txData = new TestData ();
        notNull (testPublisher1).publish (txData, false);
        notNull (testPublisher2).publish (txData, false);
        notNull (testPublisher3).publish (txData, false);

        notNull (testSubscriber1).await ();
        notNull (testSubscriber2).await ();
        notNull (testSubscriber3).await ();
    }

}
