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
package com.rws.utility.common.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author pirk
 */
public class TimeBoundPriorityQueue extends PriorityQueue<ITimestamp> {

    private static final long serialVersionUID = 1L;
    private static final TimeComparator COMPARATOR = new TimeComparator();

    long mPeriod;

    public TimeBoundPriorityQueue(final long periodInMinutes) {

        super(10, COMPARATOR);
        mPeriod = periodInMinutes * 60 * 1000;
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Cleaner(), 0, 5000,
                TimeUnit.MILLISECONDS);
    }

    class Cleaner implements Runnable {

        public Cleaner() {

            super();
        }

        @Override
        public void run() {

            if (isEmpty()) {
                return;
            }

            synchronized (TimeBoundPriorityQueue.this) {
                final Collection<ITimestamp> retain = new ArrayList<>();
                final long start = System.currentTimeMillis();
                for (final ITimestamp ts : TimeBoundPriorityQueue.this) {
                    if (start - ts.getTimestamp() < mPeriod) {
                        retain.add(ts);
                    }
                }

                retainAll(retain);
            }
        }
    }

    private static class TimeComparator implements Comparator<ITimestamp> {

        public TimeComparator() {

            super();
        }

        @Override
        public int compare(final ITimestamp o1, final ITimestamp o2) {

            if (o1.getTimestamp() < o2.getTimestamp()) {
                return -1;
            } else if (o1.getTimestamp() > o2.getTimestamp()) {
                return 1;
            }
            return 0;
        }

    }
}
