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

import java.util.Map;

import com.google.common.base.Optional;
import com.rws.pirkolator.core.grid.IGrid;

public interface IStore<T> {

    /** 
     * Supporting grid of the store
     * 
     * @return
     */
    IGrid getGrid ();

    Class<T> getType ();

    int size ();

    Iterable<? extends T> findAll ();

    Iterable<? extends T> findByParameterMap (Map<String, Object> map);
    
    Optional<T> findById (String id);

    Optional<T> put (T item);

    Optional<T> remove (T item);

    Optional<T> removeById (String id);

    void reset ();

    void registerListener (Object listener);

    void unregisterListener (Object listener);

    void postEvent (Object event);

    void removeAll ();
}
