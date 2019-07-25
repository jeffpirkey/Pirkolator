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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 
 * @author kwitten
 * @since 0.1.0
 */
public class SortUtils {

    public static List<Sort> getSortList (final String sortBy) {

        if (sortBy == null || sortBy.isEmpty ()) {
            return Collections.emptyList ();
        }

        final List<Sort> sortList = new ArrayList<> ();
        final String[] values = sortBy.replace (" ", "").split (",");
        for (final String value : values) {
            if (!value.isEmpty ()) {
                final Sort sort = createSort (value);
                sortList.add (sort);
            }
        }

        return sortList;
    }

    private static Sort createSort (final String value) {

        final SortOrder order;
        final String propertyName;
        if (value.startsWith ("-")) {
            order = SortOrder.DESCENDING;
            propertyName = value.substring (1);
        } else {
            order = SortOrder.ASCENDING;
            propertyName = value.toString ();
        }

        final Sort sort = new Sort ();
        sort.setPropertyName (propertyName);
        sort.setSortOrder (order);

        return sort;
    }
}
