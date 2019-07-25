package com.rws.pirkolator.core.engine;

import static com.rws.utility.common.Preconditions.notNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.rws.pirkolator.core.grid.IGridQueueAdapter;
import com.rws.pirkolator.model.Message;
import com.rws.utility.common.Globals;

public class MessageRouter {

    static final Logger LOG = notNull (LoggerFactory.getLogger (MessageRouter.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** <p>Receiving queue for each subscriber. This is a single queue used to deliver
     * messages from all the supporting publishers to an individual subscriber in the group. 
     * <br/><b> : KEY = queue name {@link String}</b> **/
    final Map<String, IQueueGroupManager> subscriberReceivingQueueMap = new ConcurrentHashMap<> ();

    /** Publisher MessageQueues that are listened to by this router **/
    final Set<MessageQueue> pollingQueueSet = Sets.newConcurrentHashSet ();

    /** <p>ExecutorService used to polling a publisher queue. 
     * <br/><b>Key = Queue {@link UUID}</b> **/
    private final Map<UUID, ExecutorService> pollingServiceMap = new ConcurrentHashMap<> ();

    /** <p> Callback Grid used to get queues for return ACKs.
     * <br/><b>Key = Queue {@link UUID}</b> **/
    final Map<UUID, IGridQueueAdapter> callbackGridMap = new ConcurrentHashMap<> ();

    /** Registration management **/
    final ReentrantReadWriteLock deliveryLock = new ReentrantReadWriteLock (true);

    final String systemName;

    private final SystemResourceManager resourceManager;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public MessageRouter (final SystemResourceManager resourceManager, final String systemName) {

        super ();

        this.resourceManager = resourceManager;
        this.systemName = systemName;
    }

    // *************************************************************************
    // ** Subscriber methods
    // *************************************************************************

    public Set<IQueueGroupManager> getReceivingQueueGroupSet () {

        final Collection<IQueueGroupManager> tmp = subscriberReceivingQueueMap.values ();
        if (tmp != null) {
            return ImmutableSet.copyOf (tmp);
        }

        return ImmutableSet.of ();
    }

    public void addReceivingQueueGroup (final IQueueGroupManager receivingQueue) {

        deliveryLock.writeLock ().lock ();

        try {
            subscriberReceivingQueueMap.put (receivingQueue.getQueueName (), receivingQueue);
        } finally {
            deliveryLock.writeLock ().unlock ();
        }
    }

    public void removeReceivingQueue (final String queueName) {

        deliveryLock.writeLock ().lock ();

        try {
            subscriberReceivingQueueMap.remove (queueName);
        } finally {
            deliveryLock.writeLock ().unlock ();
        }
    }

    public void addPollingQueue (final MessageQueue queue) {

        deliveryLock.writeLock ().lock ();

        try {
            pollingQueueSet.add (queue);
            activatePollingTask (queue);
        } finally {
            deliveryLock.writeLock ().unlock ();
        }
    }

    public void addPollingQueueSet (final Set<MessageQueue> queueSet) {

        deliveryLock.writeLock ().lock ();

        try {
            for (final MessageQueue queue : queueSet) {
                if (queue != null) {
                    pollingQueueSet.add (queue);
                    activatePollingTask (queue);
                } else {
                    LOG.warn ("Attempted to add a null MessageQueue to the polling set.");
                }
            }
        } finally {
            deliveryLock.writeLock ().unlock ();
        }
    }

    public void removePollingQueue (final MessageQueue queue) {

        deliveryLock.writeLock ().lock ();

        try {
            if (pollingQueueSet.remove (queue)) {
                deactivatePollingTask (queue);
            }
        } finally {
            deliveryLock.writeLock ().unlock ();
        }
    }

    public void removePollingQueueSet (final Set<MessageQueue> queueSet) {

        for (final MessageQueue queue : queueSet) {
            if (queue != null) {
                removePollingQueue (queue);
            } else {
                LOG.warn ("Attempted to remove a null MessageQueue to the polling set.");
            }
        }
    }

    public Set<MessageQueue> getPollingQueueSet () {

        return pollingQueueSet;
    }

    public void destroy () {

        deliveryLock.writeLock ().lock ();

        try {
            for (final ExecutorService task : pollingServiceMap.values ()) {
                task.shutdown ();
            }
        } finally {
            deliveryLock.writeLock ().unlock ();
        }
    }

    private void activatePollingTask (final MessageQueue activeQueue) {

        // No active polling task
        if (!pollingServiceMap.containsKey (activeQueue.getId ())) {

            final ExecutorService service = resourceManager.getSingleThreadExecutor (activeQueue.getQueueName ());
            service.execute (new PollQueueTask (activeQueue));

            pollingServiceMap.put (activeQueue.getId (), service);

            if (LOG.isTraceEnabled ()) {
                LOG.trace ("Poll task service activated for queue {}", activeQueue.getQueueName ());
            }
        } else if (LOG.isTraceEnabled ()) {
            LOG.trace ("Polling task service already activated for queue {}", activeQueue.getQueueName ());
        }
    }

    private void deactivatePollingTask (final MessageQueue deactiveQueue) {

        // Remove polling task if active
        // No active polling task
        if (pollingServiceMap.containsKey (deactiveQueue.getId ())) {
            final ExecutorService service = pollingServiceMap.remove (deactiveQueue.getId ());
            if (service != null) {
                service.shutdown ();
            }

            if (LOG.isTraceEnabled ()) {
                LOG.trace ("Poll task service deactivated for queue {}", deactiveQueue.getQueueName ());
            }
        }
        if (LOG.isTraceEnabled ()) {
            LOG.trace ("Polling task service already deactivated for queue {}", deactiveQueue.getQueueName ());
        }
    }

    // *************************************************************************
    // ** Member classes 
    // *************************************************************************

    /**
     * Listens on an active Pub/Sub message queue and delivers any taken message
     * to each receiving queue of the supported Subscriptions.
     * 
     * @author jpirkey
     *
     */
    private class PollQueueTask implements Runnable {

        /** class **/
        private final MessageQueue pollingQueue;

        /** life-cycle */
        private final AtomicBoolean taskRunning = new AtomicBoolean (false);

        public PollQueueTask (final MessageQueue queue) {

            super ();

            pollingQueue = queue;
        }

        @Override
        public void run () {

            taskRunning.set (true);

            while (taskRunning.get ()) {
                try {
                    if (LOG.isTraceEnabled ()) {
                        LOG.trace ("Listening for message on {} queue {}, descriptor {}", systemName,
                                pollingQueue.getQueueName (), pollingQueue.getQueueDescriptor ());
                    }

                    final Message message = pollingQueue.take ();
                    if (message != null) {

                        // TODO jpirkey Copy message for multiple receiving queues?
                        if (subscriberReceivingQueueMap.isEmpty ()) {
                            if (LOG.isTraceEnabled ()) {
                                LOG.warn ("No local subscriber receiving queues on {}{}", systemName, message);
                            }
                        } else {
                            for (final IQueueGroupManager receivingQueue : subscriberReceivingQueueMap.values ()) {

                                final Set<MessageQueue> queueSet = receivingQueue.nextQueueSet ();
                                for (final MessageQueue queue : queueSet) {
                                    
                                    queue.put (message);
                                    
                                    if (LOG.isTraceEnabled ()) {
                                        LOG.trace ("Put message on {} receiving queue {}, descriptor {}", systemName,
                                                queue.getQueueName (), queue.getQueueDescriptor ());
                                    }
                                }

                                // FIXME jpirkey Use ACK identifier to only notify the requesting publisher
                                // ACK on receive
                                final IGridQueueAdapter callbackGrid = callbackGridMap.get (pollingQueue.getId ());
                                final String receiveAckQueue = message.getHeader (MessageDistributor.RECEIVE_ACK);
                                if (receiveAckQueue != null && !receiveAckQueue.isEmpty ()) {
                                    try {
                                        final BlockingQueue<UUID> returnQueue = callbackGrid.getQueue (receiveAckQueue);
                                        returnQueue.put (message.getOriginalId ());
                                    } catch (final InterruptedException e) {
                                        LOG.warn (e.getMessage (), e);
                                    }
                                }
                            }
                        }
                    }

                    Thread.yield ();
                } catch (final InterruptedException ie) {
                    taskRunning.set (false);

                    if (LOG.isDebugEnabled ()) {
                        LOG.info ("Polling task interrupted, exiting");
                    }
                } catch (final Throwable ex) {
                    taskRunning.set (false);
                    if (LOG.isDebugEnabled ()) {
                        LOG.warn ("Polling task problem - {}{}{}", ex.getLocalizedMessage (), Globals.NEW_LINE, ex);
                    } else {
                        LOG.warn ("Polling task problem - ", ex.getLocalizedMessage ());
                    }
                }
            }
        }
    }
}
