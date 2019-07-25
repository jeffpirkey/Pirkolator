/*******************************************************************************
 * Copyright 2014 Reality Warp Software
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

import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.ITopic;
import com.hazelcast.core.MessageListener;
import com.rws.pirkolator.model.Message;
import com.rws.utility.common.UUIDs;

public class TopicMessageQueue extends MessageQueue implements MessageListener<Message> {

    private static final Logger LOG = notNull (LoggerFactory.getLogger (TopicMessageQueue.class));

    private final ITopic<Message> topic;
    
    private final String topicString;
    
    public TopicMessageQueue (final ITopic<Message> topic, final String queueName, final String queueDescriptor) {

        super (new LinkedBlockingQueue<Message> (), queueName, queueDescriptor);

        this.topic = topic;

        topicString = topic.addMessageListener (this);
    }

    public void stop () {

        topic.removeMessageListener (topicString);
    }

    @Override
    public boolean add (final @Nullable Message message) {

        if (message != null) {
            message.addHeader ("topic-publisher", UUIDs.toString (getId ()));
            topic.publish (message);

            return true;
        }

        return false;
    }

    @Override
    public void put (final @Nullable Message message) {

        if (message != null) {
            message.addHeader ("topic-publisher", UUIDs.toString (getId ()));
            topic.publish (message);
        }
    }

    @Override
    public void onMessage (final @Nullable com.hazelcast.core.Message<Message> message) {

        if (message != null) {
            final Message msg = message.getMessageObject ();
            final String publishingId = msg.getHeader ("topic-publisher");

            if (!getId ().toString ().equals (publishingId)) {
                try {
                    super.put (message.getMessageObject ());
                } catch (final InterruptedException ex) {
                    LOG.warn ("MessageQueue put interrupted");
                }
            }
        }
    }
}
