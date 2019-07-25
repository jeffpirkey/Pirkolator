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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class provides a {@link ConcurrentHashMap} implementation that removes
 * the entries based on the amount of time since insertion. This class uses an
 * underlying {@link Map} of times and a scheduled cleaner instance that will check and
 * remove entries.  An {@link IListener} can be supplied which will pass the removed entry
 * to the listener.
 * 
 * @author pirk
 */
public class TimeBoundConcurrentMap<K, V> extends ConcurrentHashMap<K, V> {

    private static final long serialVersionUID = 1L;

    public static interface IListener {
        void notifyOnChange (List<Object> removedKeyList);
    }

    Map<Object, Long> mTimeMap = new HashMap<> ();
    long mPeriod;
    IListener mListener;

    public TimeBoundConcurrentMap (final long periodInMinutes) {
        super ();

        mPeriod = periodInMinutes * 60 * 1000;
        Executors.newSingleThreadScheduledExecutor ().scheduleWithFixedDelay (new Cleaner (), 0, mPeriod,
                TimeUnit.MILLISECONDS);
    }

    public TimeBoundConcurrentMap (final long periodInMinutes, final IListener listener) {

        this (periodInMinutes);
        mListener = listener;
    }

    class Cleaner implements Runnable {

        public Cleaner () {

            super ();
        }

        @Override
        public void run () {

            if (isEmpty ()) {
                return;
            }

            final List<Object> remove = new ArrayList<> ();
            synchronized (TimeBoundConcurrentMap.this) {

                final long start = System.currentTimeMillis ();
                for (final Entry<Object, Long> entry : mTimeMap.entrySet ()) {
                    if (start - entry.getValue () < mPeriod) {
                        remove.add (entry.getKey ());
                    }
                }

                for (final Object obj : remove) {
                    TimeBoundConcurrentMap.this.remove (obj);
                }
            }

            if (mListener != null) {
                mListener.notifyOnChange (remove);
            }
        }
    }
}
