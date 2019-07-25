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
package com.rws.pirkolator.view.model;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.model.AbstractIdentifiable;

public class SeriesView extends AbstractIdentifiable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private final String sourceId;
    private final String seriesLabel;
    private final String seriesType;
    private final String periodLabel;
    private final String periodType;
    private final String valueLabel;
    private final String valueType;
    private final Set<String> categorySet = new HashSet<> ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public SeriesView (final String id, final String sourceId, final String seriesLabel, final String seriesType,
            final String periodLabel, final String periodType, final String valueLabel, final String valueType) {

        super (id);

        this.sourceId = sourceId;
        this.seriesLabel = seriesLabel;
        this.seriesType = seriesType;
        this.periodLabel = periodLabel;
        this.periodType = periodType;
        this.valueLabel = valueLabel;
        this.valueType = valueType;
    }

    public SeriesView (final String seriesId, final String sourceId, final String seriesLabel, final String seriesType,
            final String periodLabel, final String periodType, final String valueLabel, final String valueType,
            final Set<String> categorySet) {

        this (seriesId, sourceId, seriesLabel, seriesType, periodLabel, periodType, valueLabel, valueType);

        categorySet.addAll (categorySet);
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getSourceId () {

        return sourceId;
    }

    public String getSeriesLabel () {

        return seriesLabel;
    }

    public String getSeriesType () {

        return seriesType;
    }

    public String getPeriodLabel () {

        return periodLabel;
    }

    public String getPeriodType () {

        return periodType;
    }

    public String getValueLabel () {

        return valueLabel;
    }

    public String getValueType () {

        return valueType;
    }

    public Set<String> getCategorySet () {

        return categorySet;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), sourceId, seriesLabel, seriesType, periodLabel, periodType,
                valueLabel, valueType, categorySet);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof SeriesView) {
            if (!super.equals (object))
                return false;
            final SeriesView that = (SeriesView) object;
            return Objects.equal (sourceId, that.sourceId) && Objects.equal (seriesLabel, that.seriesLabel)
                    && Objects.equal (seriesType, that.seriesType)
                    && Objects.equal (periodLabel, that.periodLabel)
                    && Objects.equal (periodType, that.periodType)
                    && Objects.equal (valueLabel, that.valueLabel)
                    && Objects.equal (valueType, that.valueType)
                    && Objects.equal (categorySet, that.categorySet);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("sourceId", sourceId)
                .add ("seriesLabel", seriesLabel).add ("seriesType", seriesType).add ("periodLabel", periodLabel)
                .add ("periodType", periodType).add ("valueLabel", valueLabel).add ("valueType", valueType)
                .add ("categorySet", categorySet).toString ();
    }
}
