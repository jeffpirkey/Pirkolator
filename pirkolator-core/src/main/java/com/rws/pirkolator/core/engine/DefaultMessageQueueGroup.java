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

import java.util.Iterator;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

/**
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public class DefaultMessageQueueGroup implements IQueueGroupManager {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private final Set<MessageQueue> allQueueSet = Sets.newConcurrentHashSet ();
    private final Set<MessageQueue> nextQueueSet = Sets.newConcurrentHashSet ();

    private final String queueName;

    private int currentIndex;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public DefaultMessageQueueGroup (final MessageQueue firstQueue, final String queueName) {

        super ();

        this.queueName = queueName;
        nextQueueSet.add (firstQueue);
        allQueueSet.add (firstQueue);
        currentIndex = 0;
    }

    @Override
    public String getQueueName () {

        return queueName;
    }

    @Override
    public void addQueue (final MessageQueue queue) {

        allQueueSet.add (queue);
    }

    public void removeQueue (final MessageQueue queue) {

        allQueueSet.remove (queue);
        nextQueueSet.remove (queue);
    }

    @Override
    public Set<MessageQueue> nextQueueSet () {

        return ImmutableSet.copyOf (nextQueueSet);
    }

    @Override
    public Set<MessageQueue> getAllQueueSet () {

        return ImmutableSet.copyOf (allQueueSet);
    }

    void nextQueue () {

        if (allQueueSet.size () > 1) {

            int index = currentIndex++;
            if (index >= allQueueSet.size ()) {
                index = 0;
            }
            final Iterator<MessageQueue> it = allQueueSet.iterator ();
            nextQueueSet.clear ();
            nextQueueSet.add (Iterators.get (checkNotNull (it), index));
        }
    }
}
