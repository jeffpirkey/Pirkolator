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
import static com.rws.utility.common.Preconditions.notNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.rws.pirkolator.core.store.SeriesViewStore;
import com.rws.pirkolator.schema.metric.IMeasurement;
import com.rws.pirkolator.schema.metric.ISeries;
import com.rws.pirkolator.view.model.MeasurementView;
import com.rws.pirkolator.view.model.SeriesView;
import com.rws.utility.common.UUIDs;

/**
 * This class listeners for updates to a {@link ISeries} and syncs the
 * changes with the representative views in the {@link SeriesViewStore}.
 * 
 * @author jpirkey
 *
 */
public final class SeriesViewSynchronizer<P, V> implements ISeries<P, V> {

    private final ISeries<P, V> series;
    private final SeriesViewStore seriesStore;

    public SeriesViewSynchronizer (final ISeries<P, V> series, final SeriesViewStore seriesStore) {

        super ();

        this.series = series;
        this.seriesStore = seriesStore;

        final String seriesLabel = notNull (series.getPropertyMap ().get ("seriesLabel"));
        final String periodLabel = notNull (series.getPropertyMap ().get ("periodLabel"));
        final String periodType = notNull (series.getPropertyMap ().get ("periodType"));
        final String valueLabel = notNull (series.getPropertyMap ().get ("valueLabel"));
        final String valueType = notNull (series.getPropertyMap ().get ("valueType"));
        final String seriesType = notNull (series.getPropertyMap ().get ("seriesType"));

        final SeriesView view =
                new SeriesView (UUIDs.uuidAsStringOrDefault (series.getId ()), series.getSourceId (), seriesLabel,
                        seriesType, periodLabel, periodType, valueLabel, valueType);

        for (final IMeasurement<P, V> measurement : series) {
            final String period = notNull (measurement.getPeriod ().toString ());
            final String value = notNull (measurement.getValue ().toString ());
            final MeasurementView measurementView = new MeasurementView (period, value);
            seriesStore.addMeasurementView (UUIDs.toString (series.getId ()), measurementView);
        }

        if (series.getPropertyMap ().containsKey ("category")) {
            final String catValue = series.getPropertyMap ().get ("category");
            if (catValue != null) {
                final Iterable<String> categories =
                        Splitter.on (",").trimResults ().omitEmptyStrings ().split (catValue);

                for (final String cat : categories) {
                    view.getCategorySet ().add (cat);
                }
            }
        }
        seriesStore.put (view);
    }

    @Override
    public int size () {

        return series.size ();
    }

    @Override
    public boolean isEmpty () {

        return series.isEmpty ();
    }

    @Override
    public boolean contains (final @Nullable Object o) {

        return series.contains (o);
    }

    @Override
    public Iterator<IMeasurement<P, V>> iterator () {

        final Iterator<IMeasurement<P, V>> tmp = series.iterator ();
        if (tmp != null) {
            return tmp;
        }
        return ImmutableSet.<IMeasurement<P, V>> of ().iterator ();
    }

    @Override
    public Object[] toArray () {

        final Object[] tmp = series.toArray ();
        return checkNotNull (tmp);
    }

    @Override
    public <T> T[] toArray (final @Nullable T[] a) {

        final T[] tmp = series.toArray (a);
        return checkNotNull (tmp);
    }

    @Override
    public boolean add (final @Nullable IMeasurement<P, V> e) {

        checkNotNull (e);

        final String period = notNull (e.getPeriod ().toString ());
        final String value = notNull (e.getValue ().toString ());
        final MeasurementView view = new MeasurementView (period, value);

        seriesStore.addMeasurementView (UUIDs.toString (series.getId ()), view);

        return series.add (e);
    }

    @Override
    public boolean remove (final @Nullable Object o) {

        if (o instanceof IMeasurement<?, ?>) {
            seriesStore.removeMeasurementView (UUIDs.toString (series.getId ()), ((IMeasurement<?, ?>) o).getPeriod ());
        }
        return series.remove (o);
    }

    @Override
    public boolean containsAll (final @Nullable Collection<?> c) {

        return series.containsAll (c);
    }

    @Override
    public boolean addAll (final @Nullable Collection<? extends IMeasurement<P, V>> c) {

        checkNotNull (c);

        for (final IMeasurement<P, V> e : c) {
            final String period = notNull (e.getPeriod ().toString ());
            final String value = notNull (e.getValue ().toString ());
            final MeasurementView view = new MeasurementView (period, value);
            seriesStore.addMeasurementView (UUIDs.toString (series.getId ()), view);
        }

        return series.addAll (c);
    }

    @Override
    public boolean addAll (final int index, final @Nullable Collection<? extends IMeasurement<P, V>> c) {

        checkNotNull (c);

        for (final IMeasurement<P, V> e : c) {
            final String period = notNull (e.getPeriod ().toString ());
            final String value = notNull (e.getValue ().toString ());
            final MeasurementView view = new MeasurementView (period, value);
            seriesStore.addMeasurementView (UUIDs.toString (series.getId ()), view);
        }

        return series.addAll (index, c);
    }

    @Override
    public boolean removeAll (final @Nullable Collection<?> c) {

        if (series != null) {
            return series.removeAll (c);
        }

        return false;
    }

    @Override
    public boolean retainAll (final @Nullable Collection<?> c) {

        if (series != null) {
            return series.retainAll (c);
        }

        return false;
    }

    @Override
    public void clear () {

        series.clear ();
    }

    @Override
    public @Nullable
    IMeasurement<P, V> get (final int index) {

        return series.get (index);
    }

    @Override
    public @Nullable
    IMeasurement<P, V> set (final int index, final @Nullable IMeasurement<P, V> element) {

        return series.set (index, element);
    }

    @Override
    public void add (final int index, final @Nullable IMeasurement<P, V> element) {

        series.add (index, element);
    }

    @Override
    public @Nullable
    IMeasurement<P, V> remove (final int index) {

        return series.remove (index);
    }

    @Override
    public int indexOf (final @Nullable Object o) {

        return series.indexOf (o);
    }

    @Override
    public int lastIndexOf (final @Nullable Object o) {

        return series.lastIndexOf (o);
    }

    @Override
    public ListIterator<IMeasurement<P, V>> listIterator () {

        final ListIterator<IMeasurement<P, V>> tmp = series.listIterator ();
        if (tmp != null) {
            return tmp;
        }

        return ImmutableList.<IMeasurement<P, V>> of ().listIterator ();
    }

    @Override
    public ListIterator<IMeasurement<P, V>> listIterator (final int index) {

        final ListIterator<IMeasurement<P, V>> tmp = series.listIterator (index);
        if (tmp != null) {
            return tmp;
        }

        return ImmutableList.<IMeasurement<P, V>> of ().listIterator ();

    }

    @Override
    public List<IMeasurement<P, V>> subList (final int fromIndex, final int toIndex) {

        final List<IMeasurement<P, V>> tmp = series.subList (fromIndex, toIndex);
        if (tmp != null) {
            return tmp;
        }

        return ImmutableList.of ();
    }

    @Override
    public UUID getId () {

        return series.getId ();
    }

    @Override
    public String getSourceId () {

        return series.getSourceId ();
    }

    @Override
    public Map<String, String> getPropertyMap () {

        return series.getPropertyMap ();
    }

    public ISeries<P, V> getSeries () {

        return series;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("series", series)
                .add ("seriesStore", seriesStore).toString ();
    }

}
