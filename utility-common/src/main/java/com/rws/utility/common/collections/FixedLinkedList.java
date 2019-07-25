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

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author pirk
 */
public class FixedLinkedList<T> extends LinkedList<T> {

    private static final long serialVersionUID = 1L;

    int mMaxSize;

    public FixedLinkedList(final int maxSize) {

        super();
        mMaxSize = maxSize;
    }

    public FixedLinkedList(final Collection<? extends T> arg0) {

        super(arg0);
        mMaxSize = arg0.size();
    }

    @Override
    public void add(final int index, final T element) {
        // TODO Auto-generated method stub
        super.add(index, element);
    }

    @Override
    public boolean add(final T e) {
        // TODO Auto-generated method stub
        return super.add(e);
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        // TODO Auto-generated method stub
        return super.addAll(c);
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends T> c) {
        // TODO Auto-generated method stub
        return super.addAll(index, c);
    }

    @Override
    public void addFirst(final T e) {
        // TODO Auto-generated method stub
        super.addFirst(e);
    }

    @Override
    public void addLast(final T e) {
        // TODO Auto-generated method stub
        super.addLast(e);
    }

}
