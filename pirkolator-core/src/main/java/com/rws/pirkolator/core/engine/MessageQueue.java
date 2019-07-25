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

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.rws.pirkolator.model.Message;
import com.rws.utility.common.UUIDs;

/**
 * This class provides an implementation of the {@link BlockingQueue} for {@link Message}
 * objects.  It is a wrap around the BlcokingQueue that is provided by the IPubSub
 * instances for each PubSub implementation (LocalPubSub, HazelcastPubSub, etc.)
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public class MessageQueue implements BlockingQueue<Message> {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private final UUID id;
    private final BlockingQueue<Message> queue;
    private final String queueDescriptor;
    private final String queueName;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public MessageQueue (final BlockingQueue<Message> queue, final String queueName, final String queueDescriptor) {

        super ();

        id = UUIDs.generateUUID ();

        this.queue = queue;
        this.queueName = queueName;
        this.queueDescriptor = queueDescriptor;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public UUID getId () {

        return id;
    }

    public String getQueueDescriptor () {

        return queueDescriptor;
    }

    public String getQueueName () {

        return queueName;
    }

    // *************************************************************************
    // ** Blocking Queue implementation
    // *************************************************************************

    @Override
    public boolean add (final @Nullable Message message) {

        checkNotNull (message);
        return queue.add (message);
    }

    @Override
    public boolean addAll (final @Nullable Collection<? extends Message> messageColl) {

        checkNotNull (messageColl);
        for (final Message message : messageColl) {
            add (message);
        }
        return false;
    }

    @Override
    public void clear () {

        queue.clear ();
    }

    @Override
    public boolean contains (final @Nullable Object obj) {

        return queue.contains (obj);
    }

    @Override
    public boolean containsAll (final @Nullable Collection<?> coll) {

        return queue.containsAll (coll);
    }

    @Override
    public int drainTo (final @Nullable Collection<? super Message> c) {

        checkNotNull (c);
        return queue.drainTo (c);
    }

    @Override
    public int drainTo (final @Nullable Collection<? super Message> c, final int maxElements) {

        checkNotNull (c);
        return queue.drainTo (c, maxElements);
    }

    @Override
    public @Nullable
    Message element () {

        return queue.element ();
    }

    @Override
    public boolean isEmpty () {

        return queue.isEmpty ();
    }

    @Override
    public Iterator<Message> iterator () {

        final Iterator<Message> tmp = queue.iterator ();
        if (tmp != null) {
            return Iterators.unmodifiableIterator (tmp);
        }

        return ImmutableSet.<Message> of ().iterator ();
    }

    @Override
    public boolean offer (final @Nullable Message message) {

        return queue.offer (message);
    }

    @Override
    public boolean offer (final @Nullable Message message, final long timeout, final @Nullable TimeUnit unit)
            throws InterruptedException {

        return queue.offer (message, timeout, unit);
    }

    @Override
    public @Nullable
    Message peek () {

        return queue.peek ();
    }

    @Override
    public @Nullable
    Message poll () {

        return queue.poll ();
    }

    @Override
    public @Nullable
    Message poll (final long timeout, final @Nullable TimeUnit unit) throws InterruptedException {

        return queue.poll (timeout, unit);
    }

    @Override
    public void put (final @Nullable Message message) throws InterruptedException {

        queue.put (message);
    }

    @Override
    public int remainingCapacity () {

        return queue.remainingCapacity ();
    }

    @Override
    public @Nullable
    Message remove () {

        return queue.remove ();
    }

    @Override
    public boolean remove (final @Nullable Object obj) {

        return queue.remove (obj);
    }

    @Override
    public boolean removeAll (final @Nullable Collection<?> coll) {

        return queue.removeAll (coll);
    }

    @Override
    public boolean retainAll (final @Nullable Collection<?> coll) {

        return queue.retainAll (coll);
    }

    @Override
    public int size () {

        return queue.size ();
    }

    @Override
    public @Nullable
    Message take () throws InterruptedException {

        return queue.take ();
    }

    @Override
    public @Nullable
    Object[] toArray () {

        return queue.toArray ();
    }

    @Override
    public @Nullable
    <T> T[] toArray (final @Nullable T[] theArray) {

        return queue.toArray (theArray);
    }
}
