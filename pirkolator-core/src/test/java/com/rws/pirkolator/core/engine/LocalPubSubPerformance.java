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

import com.rws.utility.common.Globals;
import com.rws.utility.test.AbstractPirkolatorTest;

@DirtiesContext (classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration (locations = { "classpath:com/rws/pirkolator/core/engine/messageCountingSubscriber-context.xml",
        "classpath:com/rws/pirkolator/core/engine/testPublisher-context.xml" })
public class LocalPubSubPerformance extends AbstractPirkolatorTest {

    @Resource
    @Nullable
    LocalPubSub localPubSub;

    @Resource
    @Nullable
    MessageCountingSubscriber testSubscriber;

    @Resource
    @Nullable
    TestTypePublisher testPublisher;

    @Test
    public void testPublishByReference () throws InterruptedException {

        final double count = 100000;

        final StringData txData = new StringData (10240);

        notNull (testSubscriber).startCount ((int) count);

        final double startByReference = System.currentTimeMillis ();

        for (int i = 0; i < count; i++) {
            //LOG.info ("TX = " + Integer.toString (i));
            notNull (testPublisher).publish (txData, false);
        }

        notNull (testSubscriber).await ();

        final double stopByReference = System.currentTimeMillis ();

        final double avgPerSec = count / ((stopByReference - startByReference) / 1000);

        final StringBuilder tmp = new StringBuilder ("Performance by reference of ");
        tmp.append (count);
        tmp.append (" string messages = ");
        tmp.append (stopByReference - startByReference);
        tmp.append (" ms");
        tmp.append (Globals.NEW_LINE).append ("average = ").append (avgPerSec).append (" per second");
        LOG.info (tmp.toString ());
    }

    @Test
    public void testPublishByReference_twoSubscribers () throws InterruptedException {

        final MessageCountingSubscriber subscriber2 = new MessageCountingSubscriber ();

        notNull (localPubSub).addSubscriber (subscriber2);

        final double count = 100000;

        final StringData txData = new StringData (10240);

        notNull (testSubscriber).startCount ((int) count);
        subscriber2.startCount ((int) count);

        final double startByReference = System.currentTimeMillis ();

        for (int i = 0; i < count; i++) {
            //LOG.info ("TX = " + Integer.toString (i));
            notNull (testPublisher).publish (txData, false);
        }

        notNull (testSubscriber).await ();
        subscriber2.await ();

        final double stopByReference = System.currentTimeMillis ();

        final double avgPerSec = count / ((stopByReference - startByReference) / 1000);

        final StringBuilder tmp = new StringBuilder ("Performance by reference of ");
        tmp.append (count);
        tmp.append (" string messages = ");
        tmp.append (stopByReference - startByReference);
        tmp.append (" ms");
        tmp.append (Globals.NEW_LINE).append ("average = ").append (avgPerSec).append (" per second");
        LOG.info (tmp.toString ());
    }

    @Test
    public void testPublishByCopy () throws InterruptedException {

        final double count = 100000;

        final StringData txData = new StringData (10240);

        notNull (testSubscriber).startCount ((int) count);

        final double startByReference = System.currentTimeMillis ();

        for (int i = 0; i < count; i++) {
            //LOG.info ("TX = " + Integer.toString (i));
            notNull (testPublisher).publish (txData, true);
        }

        notNull (testSubscriber).await ();

        final double stopByReference = System.currentTimeMillis ();

        final double avgPerSec = count / ((stopByReference - startByReference) / 1000);

        final StringBuilder tmp = new StringBuilder ("Performance by copy of ");
        tmp.append (count);
        tmp.append (" string messages = ");
        tmp.append (stopByReference - startByReference);
        tmp.append (" ms");
        tmp.append (Globals.NEW_LINE).append ("average = ").append (avgPerSec).append (" per second");
        LOG.info (tmp.toString ());
    }

    @Test
    public void testPublishByCopy_twoSubscribers () throws InterruptedException {

        final MessageCountingSubscriber subscriber2 = new MessageCountingSubscriber ();

        notNull (localPubSub).addSubscriber (subscriber2);

        final double count = 100000;

        final StringData txData = new StringData (10240);

        notNull (testSubscriber).startCount ((int) count);
        subscriber2.startCount ((int) count);

        final double startByReference = System.currentTimeMillis ();

        for (int i = 0; i < count; i++) {
            //LOG.info ("TX = " + Integer.toString (i));
            notNull (testPublisher).publish (txData, true);
        }

        notNull (testSubscriber).await ();

        final double stopByReference = System.currentTimeMillis ();

        final double avgPerSec = count / ((stopByReference - startByReference) / 1000);

        final StringBuilder tmp = new StringBuilder ("Performance by copy of ");
        tmp.append (count);
        tmp.append (" string messages = ");
        tmp.append (stopByReference - startByReference);
        tmp.append (" ms");
        tmp.append (Globals.NEW_LINE).append ("average = ").append (avgPerSec).append (" per second");
        LOG.info (tmp.toString ());
    }
}
