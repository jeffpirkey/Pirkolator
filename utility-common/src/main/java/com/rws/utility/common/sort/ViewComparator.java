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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: kwitten
 * Date: 11/2/13
 * Time: 8:07 PM
 */
public class ViewComparator<T> implements Comparator<T> {

    private static final Logger LOG = LoggerFactory.getLogger (ViewComparator.class);
    private static final Pattern PATTERN = Pattern.compile ("(\\D*)(\\d*)");

    private final List<Sort> mCompareParams = new ArrayList<> ();

    public ViewComparator (final List<Sort> params) {

        mCompareParams.addAll (params);
    }

    @Override
    public int compare (final T o1, final T o2) {

        // TODO jpirkey Add number aware compare

        int val;
        int desc;

        // Compare the parameters in order until one is not equal and return that
        for (final Sort param : mCompareParams) {

            if (param.getSortOrder () == SortOrder.DESCENDING) {
                desc = -1;
            } else {
                desc = 1;
            }

            // Get the values for comparison
            final Comparable<Object> val1 = getValueForParameter (o1, param.getPropertyName ());
            final Comparable<Object> val2 = getValueForParameter (o2, param.getPropertyName ());

            if (val1 == null && val2 == null) {
                // If both values are null, so continue to next parameter
                continue;
            }

            // If first is null and second is not, return -1
            if (val1 == null) {
                return -1;
            }

            final Object tmp1 = val1;
            final Object tmp2 = val2;
            if (tmp1 instanceof String && tmp2 instanceof String) {
                return compare ((String) tmp1, (String) tmp2);
            }

            val = val1.compareTo (val2) * desc;
            if (val == 0) {
                // They are equal, so continue to next parameter
                continue;
            }

            // Just return the comparison value for this
            return val;
        }

        // All parameters were equal
        return 0;
    }

    public int compare (final String s1, final String s2) {

        final Matcher m1 = PATTERN.matcher (s1);
        final Matcher m2 = PATTERN.matcher (s2);

        // The only way find() could fail is at the end of a string
        while (m1.find () && m2.find ()) {
            // matcher.group(1) fetches any non-digits captured by the
            // first parentheses in PATTERN.
            final int nonDigitCompare = m1.group (1).compareTo (m2.group (1));
            if (0 != nonDigitCompare) {
                return nonDigitCompare;
            }

            // matcher.group(2) fetches any digits captured by the
            // second parentheses in PATTERN.
            if (m1.group (2).isEmpty ()) {
                return m2.group (2).isEmpty () ? 0 : -1;
            } else if (m2.group (2).isEmpty ()) {
                return +1;
            }

            final BigInteger n1 = new BigInteger (m1.group (2));
            final BigInteger n2 = new BigInteger (m2.group (2));
            final int numberCompare = n1.compareTo (n2);
            if (0 != numberCompare) {
                return numberCompare;
            }
        }

        // Handle if one string is a prefix of the other.
        // Nothing comes before something.
        return m1.hitEnd () && m2.hitEnd () ? 0 : m1.hitEnd () ? -1 : +1;
    }

    // Use reflection to call getter for a parameter
    private Comparable<Object> getValueForParameter (final Object obj, final String param) {

        try {
            final String methodName = Character.toUpperCase (param.charAt (0)) + param.substring (1);
            final Method method = obj.getClass ().getMethod ("get" + methodName);

            return (Comparable<Object>) method.invoke (obj);

        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LOG.error ("Error evaluating comparator parameter", e);
        }

        return null;
    }
}
