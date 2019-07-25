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
package com.rws.utility.common.sort;

/**
 * Created by IntelliJ IDEA.
 * User: kwitten
 * Date: 11/4/13
 * Time: 9:27 PM
 */
public class Sort {

    private SortOrder mSortOrder = SortOrder.ASCENDING;
    private String mPropertyName;

    public SortOrder getSortOrder() {

        return mSortOrder;
    }

    public void setSortOrder(final SortOrder sortOrder) {

        mSortOrder = sortOrder;
    }

    public String getPropertyName() {

        return mPropertyName;
    }

    public void setPropertyName(final String propertyName) {

        mPropertyName = propertyName;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("Sort{");
        sb.append("mSortOrder=").append(mSortOrder);
        sb.append(", mPropertyName='").append(mPropertyName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
