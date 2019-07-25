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
import java.util.TreeSet;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author pirk
 */
public class TimeBoundSortedSet<T extends ITimestamp> extends TreeSet<T> {

    private static final long serialVersionUID = 1L;
    private static final TimeComparator COMPARATOR = new TimeComparator();

    public static interface IListener {

        void notifyOnChange();
    }

    long mPeriod;
    IListener mListener;

    public TimeBoundSortedSet(final long periodInMinutes) {
        super(COMPARATOR);
        
        mPeriod = periodInMinutes * 60 * 1000;
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new Cleaner(), 0, mPeriod,
                TimeUnit.MILLISECONDS);
    }

    public TimeBoundSortedSet(final long periodInMinutes, final IListener listener) {

        this(periodInMinutes);
        mListener = listener;
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

            synchronized (TimeBoundSortedSet.this) {
                final Collection<ITimestamp> retain = new ArrayList<>();
                final long start = System.currentTimeMillis();
                for (final ITimestamp ts : TimeBoundSortedSet.this) {
                    if (start - ts.getTimestamp() < mPeriod) {
                        retain.add(ts);
                    }
                }

                retainAll(retain);
            }

            if (mListener != null) {
                mListener.notifyOnChange();
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
