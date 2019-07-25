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
package com.rws.pirkolator.core.store;

import java.util.ArrayList;
import java.util.List;

import com.rws.pirkolator.view.model.MeasurementView;
import com.rws.pirkolator.view.model.SeriesView;

public class SeriesViewStore extends AbstractHazelcastMapStore<SeriesView> {

    public static final String PROP_MAP_NAME = "seriesViewMap";

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public SeriesViewStore () {

        super (SeriesView.class);
    }

    @Override
    public String getMapName () {

        return PROP_MAP_NAME;
    }

    public void addMeasurementView (final String seriesId, final MeasurementView measurementView) {

        final List<MeasurementView> viewList = getGrid ().getList (seriesId);
        viewList.add (measurementView);
    }

    public void removeMeasurementView (final String seriesId, final Object period) {

        final List<MeasurementView> viewList = getGrid ().getList (seriesId);

        for (final MeasurementView view : viewList) {
            if (view.getPeriod ().equals (period)) {
                getGrid ().getExclusiveListLock (seriesId).lock ();
                try {
                    getGrid ().getList (seriesId).remove (view);
                } finally {
                    getGrid ().getExclusiveListLock (seriesId).unlock ();
                }

                return;
            }
        }
    }

    public List<SeriesView> findBySourceId (final String sourceId) {

        final List<SeriesView> set = new ArrayList<> ();
        for (final SeriesView view : getMap ().values ()) {
            if (sourceId.equals (view.getSourceId ())) {
                set.add (view);
            }
        }

        return set;
    }

    public List<SeriesView> findBySourceIdAndCategory (final String sourceId, final String category) {

        final List<SeriesView> set = new ArrayList<> ();
        for (final SeriesView view : getMap ().values ()) {
            if (sourceId.equals (view.getSourceId ()) && view.getCategorySet ().contains (category)) {
                set.add (view);
            }
        }

        return set;
    }

    public List<SeriesView> findByCategory (final String category) {

        final List<SeriesView> set = new ArrayList<> ();
        for (final SeriesView view : getMap ().values ()) {
            if (view.getCategorySet ().contains (category)) {
                set.add (view);
            }
        }

        return set;
    }

}
