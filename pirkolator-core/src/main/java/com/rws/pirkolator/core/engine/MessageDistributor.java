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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.pirkolator.model.Message;
import com.rws.utility.common.Globals;

// TODO jpirkey Add better locking for message sending

/**
 * <p>This class provides an implementation to either copy a message or pass by 
 * reference to the queues.
 * 
 * <p>This implementation supports Kryo serialization by setting the KryoEnabled
 * flag to true.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public class MessageDistributor {

    public static String RECEIVE_ACK = "RECEIVE_ACK";

    static Logger LOG = notNull (LoggerFactory.getLogger (MessageDistributor.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** class **/
    final PublicationManager publicationManager;
    final BlockingQueue<Message> sendingQueue = new LinkedBlockingQueue<> ();
    private final Future<?> task;
    final String systemName;
    private final IMessageCopier copier;
    private final AtomicInteger messageCounter = new AtomicInteger ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public MessageDistributor (final IMessageCopier copier, final SystemResourceManager resourceManager,
            final PublicationManager handler, final String systemName) {

        this.copier = copier;
        publicationManager = handler;
        final Future<?> tmp = resourceManager.getCachedThreadExecutor ().submit (new PutTask ());
        checkNotNull (tmp);
        task = tmp;
        this.systemName = systemName;
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    /**
     * Default publish by copy
     */
    public void publish (final Message message) {

        publish (message, true);
    }

    public synchronized void publish (final Message msg, final boolean copy) {

        final Message messageCopy;
        if (!copy) {
            messageCopy = msg;
            messageCopy.addHeader ("copy", "false");
        } else {
            try {
                messageCopy = copier.copy (msg);
                notNull (messageCopy).addHeader ("copy", "true");
            } catch (final Exception ex) {
                LOG.error ("Message serialization problem  {}", ex.getLocalizedMessage ());
                return;
            }
        }

        final String count = Integer.toString (messageCounter.get ());
        messageCopy.addHeader ("count", notNull (count));

        // TODO jpirkey Add more system information to message header
        try {
            sendingQueue.put (messageCopy);
        } catch (final InterruptedException ex) {
            LOG.warn ("Message Distribution send() interrupted");
        }
    }

    // *************************************************************************
    // ** Life-cycle methods 
    // *************************************************************************

    void destroy () {

        if (!task.isCancelled ()) {
            task.cancel (true);
        }
    }

    // *************************************************************************
    // ** Member classes 
    // *************************************************************************

    private class PutTask implements Runnable {

        AtomicBoolean mRunning = new AtomicBoolean ();

        public PutTask () {

            super ();
        }

        @Override
        public void run () {

            // FIXME jpirkey Exception stops distribution
            mRunning.set (true);
            while (mRunning.get ()) {
                try {
                    final Message message = sendingQueue.take ();

                    if (message != null) {

                        /***** Distributed Queues *****/
                        final Set<MessageQueue> distributedQueues = publicationManager.getDistributedQueueSet ();

                        if (distributedQueues.isEmpty ()) {
                            if (LOG.isTraceEnabled ()) {
                                LOG.warn ("No distributed queues on {} for message {}", systemName, message.toString ());
                            }
                        } else {
                            if (LOG.isTraceEnabled ()) {
                                LOG.trace ("Publishing to distributed queues on {}...", systemName);
                            }
                            sendToDistributedQueues (distributedQueues, message);
                            if (LOG.isTraceEnabled ()) {
                                LOG.trace ("Published to {} distributed queues on {}", distributedQueues.size (),
                                        systemName);
                            }
                        }

                        /***** Local Queues *****/
                        final Collection<IQueueGroupManager> localQueues =
                                publicationManager.getReceivingQueueGroupSet ();

                        if (localQueues.isEmpty ()) {
                            if (LOG.isTraceEnabled ()) {
                                LOG.warn ("No local queues on {} for message {}", systemName, message.toString ());
                            }
                        } else {
                            if (LOG.isTraceEnabled ()) {
                                LOG.trace ("Publishing to local queues on {}...", systemName);
                            }
                            sendToQueues (localQueues, message);
                            if (LOG.isTraceEnabled ()) {
                                LOG.trace ("Published to {} local queues on {}", localQueues.size (), systemName);
                            }
                        }
                    }

                    Thread.yield ();
                } catch (final InterruptedException ex) {
                    LOG.warn ("Message Distribution interrupted, exiting");
                    mRunning.set (false);
                } catch (final Throwable ex) {
                    LOG.warn ("Message Distribution problem", ex);
                    mRunning.set (false);
                }
            }
        }

        private void sendToQueues (final Collection<IQueueGroupManager> queueGroupManagerSet, final Message message) {

            for (final IQueueGroupManager queueGroup : queueGroupManagerSet) {

                final Set<MessageQueue> queueSet = queueGroup.nextQueueSet ();
                for (final MessageQueue queue : queueSet) {
                    if (LOG.isTraceEnabled ()) {
                        LOG.trace ("Putting message on {} queue [name={}; descriptor={}; queue size={}]{}{}",
                                systemName, queue.getQueueName (), queue.getQueueDescriptor (), queue.size (),
                                Globals.NEW_LINE, message);
                    }

                    try {
                        queue.put (message);
                    } catch (final InterruptedException ex) {
                        LOG.warn ("MessageDistributor send interrupted");
                    }

                    if (LOG.isTraceEnabled ()) {
                        LOG.trace ("Put message on {} queue [name={}; descriptor={}; queue size={}]", systemName,
                                queue.getQueueName (), queue.getQueueDescriptor (), queue.size ());
                    }
                }
            }
        }

        private void sendToDistributedQueues (final Collection<MessageQueue> queueSet, final Message message) {

            for (final MessageQueue queue : queueSet) {

                if (LOG.isTraceEnabled ()) {
                    LOG.trace ("Putting message on {} queue [name={}; descriptor={}; queue size={}]{}{}", systemName,
                            queue.getQueueName (), queue.getQueueDescriptor (), queue.size (), Globals.NEW_LINE,
                            message);
                }

                try {
                    queue.put (message);
                } catch (final InterruptedException ex) {
                    LOG.warn ("MessageDistributor send interrupted");
                }

                if (LOG.isTraceEnabled ()) {
                    LOG.trace ("Put message on {} queue [name={}; descriptor={}; queue size={}]", systemName,
                            queue.getQueueName (), queue.getQueueDescriptor (), queue.size ());
                }
            }
        }
    }
}
