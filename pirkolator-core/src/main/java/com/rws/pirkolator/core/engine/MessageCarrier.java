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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.filter.IContentFilter;
import com.rws.utility.common.UUIDs;

/**
 * This class handles delivery of messages from the subscirber's receiving
 * queue to the subscriber's receive() method.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public class MessageCarrier implements Runnable {

    final transient static Logger LOG = notNull (LoggerFactory.getLogger (MessageCarrier.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** define **/
    final ISubscriber subscriber;

    /** class **/
    final MessageQueue subscriberReceivingQueue;

    Multimap<Class<?>, MethodFilteredContent> methodAnnotationMap = HashMultimap
            .<Class<?>, MethodFilteredContent> create ();

    /**life-cycle **/
    private final AtomicBoolean running = new AtomicBoolean (false);
    private final AtomicBoolean shutdown = new AtomicBoolean (false);

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public MessageCarrier (final ISubscriber subscriber) {

        super ();

        this.subscriber = subscriber;
        subscriberReceivingQueue =
                new MessageQueue (new LinkedBlockingQueue<Message> (), UUIDs.toString (subscriber.getId ()), "queue-"
                        + subscriber.getName () + "-" + UUIDs.toString (subscriber.getId ()));
        initAnnotation ();
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public MessageQueue getSubscriberReceivingQueue () {

        return subscriberReceivingQueue;
    }

    public ISubscriber getSubscriber () {

        return subscriber;
    }

    public boolean isRunning () {

        return running.get ();
    }

    public boolean isShutdown () {

        return shutdown.get ();
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    private void initAnnotation () {

        for (final Method method : subscriber.getClass ().getMethods ()) {
            if (method.isAnnotationPresent (FilteredContent.class)) {
                final FilteredContent ann = method.getAnnotation (FilteredContent.class);
                methodAnnotationMap.put (ann.contentType (),
                        new MethodFilteredContent (method, ann.contentType (), ann.propertyArray ()));
            }
        }
    }

    @Override
    public void run () {

        if (LOG.isDebugEnabled ()) {
            LOG.info ("Started MessageCarrier task for subscriber [name={}]", subscriber.getName ());
        }

        running.set (true);
        while (running.get ()) {

            // FIXME jpirkey exception handling shouldn't kill process
            try {

                if (LOG.isTraceEnabled ()) {
                    LOG.trace ("Listening for message on {} for {}", subscriberReceivingQueue.getQueueDescriptor (),
                            subscriber.getName ());
                }

                final Message message = subscriberReceivingQueue.take ();
                if (message != null) {
                    // Process annotated FilteredContent methods
                    if (!methodAnnotationMap.isEmpty () && subscriber.getSubscription ().isContentDefined ()) {
                        for (final IContentFilter filter : subscriber.getSubscription ().getContentFilterSet ()) {
                            // Filter message content
                            if (filter.supports (message)) {

                                // First iterate over the class types supported by the Filter
                                for (final Class<?> filterType : filter.getTypeSet ()) {
                                    checkNotNull (filterType);

                                    // Iterate over each method that supports the contentType defined by the Filter
                                    for (final MethodFilteredContent mfc : methodAnnotationMap.get (filterType)) {

                                        // Get each object in the message of the defined type
                                        final Collection<?> objs = message.get (filterType);
                                        for (final Object obj : objs) {
                                            checkNotNull (obj);

                                            if (filter.supportsObject (obj, mfc.mPropertyArray)) {
                                                mfc.mMethod.invoke (subscriber, obj);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    try {
                        subscriber.receive (message);
                    } catch (final Exception ex) {

                        if (LOG.isInfoEnabled ()) {
                            if (LOG.isDebugEnabled ()) {
                                LOG.warn (
                                        "Unable to complete delivery of message to subscriber " + subscriber.getName (),
                                        ex);
                            } else {
                                LOG.warn (
                                        "Unable to complete delivery of message to subscriber.  {} threw a problem - {}",
                                        subscriber.getName (), ex.getLocalizedMessage ());
                            }
                        }
                    }

                    if (LOG.isTraceEnabled ()) {
                        LOG.trace ("Submitted message to be delivered to subscriber {} {}", subscriber.getName (),
                                message.toString ());
                    }
                }

                Thread.yield ();
            } catch (final InterruptedException ie) {
                running.set (false);

                if (LOG.isDebugEnabled ()) {
                    LOG.info ("Message delivery to subscriber {} interrupted", subscriber.getName ());
                }
            } catch (final Throwable ex) {
                running.set (false);
                LOG.warn ("MessageCarrier problem:  " + ex.getLocalizedMessage (), ex);
            }

        }

        shutdown.set (true);

        if (LOG.isDebugEnabled ()) {
            LOG.info ("Exited MessageCarrier for subscriber ", subscriber.getName ());
        }
    }

    class MethodFilteredContent {

        public final Method mMethod;
        public final String[] mPropertyArray;
        public final Class<?> mContentType;

        public MethodFilteredContent (final Method method, final Class<?> contentType, final String[] array) {

            super ();

            mMethod = method;
            mPropertyArray = array;
            mContentType = contentType;
        }
    }
}
