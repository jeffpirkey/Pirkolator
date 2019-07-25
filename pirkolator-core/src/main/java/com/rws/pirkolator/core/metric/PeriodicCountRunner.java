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
package com.rws.pirkolator.core.metric;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import com.rws.pirkolator.model.metric.Measurement;
import com.rws.pirkolator.model.metric.Series;
import com.rws.pirkolator.schema.metric.IMeasurement;
import com.rws.pirkolator.schema.metric.ISeries;

/**
 * <p>This class defines a task for a {@link Series} with a temporal period and a
 * count.  An {@link IMeasurement} instance will be create from the current count
 * and added to the series.  The count will then be reset.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public class PeriodicCountRunner extends AtomicInteger implements Runnable {

    private static final long serialVersionUID = 1L;

    public static final PeriodicCountComparator PERIODIC_COUNT_COMPARATOR = new PeriodicCountComparator ();

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** define **/
    private final ISeries<Long, Integer> series;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public PeriodicCountRunner (final ISeries<Long, Integer> series) {

        super ();

        this.series = series;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public ISeries<Long, Integer> getSeries () {

        return series;
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    @Override
    public void run () {

        final Long timestamp = System.currentTimeMillis ();
        final Integer value = this.getAndSet (0);
        final IMeasurement<Long, Integer> measurement =
                new Measurement<> (checkNotNull (timestamp), checkNotNull (value));
        series.add (measurement);
    }

    // *************************************************************************
    // ** Member classes 
    // *************************************************************************

    final static class PeriodicCountComparator implements Comparator<IMeasurement<Long, Integer>> {

        @Override
        public int compare (final @Nullable IMeasurement<Long, Integer> o1,
                final @Nullable IMeasurement<Long, Integer> o2) {

            checkNotNull (o1);
            checkNotNull (o2);
            
            if (o1.getPeriod () < o2.getPeriod ()) {
                return -1;
            } else if (o1.getPeriod () > o2.getPeriod ()) {
                return 1;
            }
            return 0;
        }
    }
}
