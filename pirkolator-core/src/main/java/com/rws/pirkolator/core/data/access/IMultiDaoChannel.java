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
package com.rws.pirkolator.core.data.access;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

import com.rws.pirkolator.schema.IDaoSource;

public interface IMultiDaoChannel<T, ID extends Serializable> extends IDataAccess<T, T, ID> {

    Map<IDaoSource, Repository<T, ID>> getRepositoryMap ();

    /**
     * 
     * @param id
     * @return M instance of the defined ID or null if an instance was not found
     */
    @Override
    @Nullable Map<IDaoSource, T> findOne (ID id);
    
    @Override
    boolean exists (ID id);

    @Override
    Map<IDaoSource, Iterable<? extends T>> findAll ();

    @Override
    Map<IDaoSource, Iterable<? extends T>> findAll (Iterable<ID> ids);
    
    @Override
    Set<ID> findAllIDs ();

    @Override
    long count ();

    @Override
    void delete (ID id);

    @Override
    void delete (T entity);

    @Override
    void delete (Iterable<? extends T> entities);

    @Override
    void deleteAll ();

    @Override
    Map<IDaoSource, Iterable<? extends T>> findAll (Sort sort);

    @Override
    Map<IDaoSource, Page<? extends T>> findAll (Pageable pageable);
}
