package com.rws.pirkolator.core.data.access;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface IDataAccess<M, T extends M, ID extends Serializable> {

    M save (M entity);

    Iterable<? extends M> save (Iterable<M> entities);

    /**
     * 
     * @param id
     * @return M instance of the defined ID or null if an instance was not found
     */
    @Nullable Object findOne (ID id);

    boolean exists (ID id);

    Object findAll ();

    Object findAll (Iterable<ID> ids);
    
    Object findAllIDs ();

    long count ();

    void delete (ID id);

    void delete (M entity);

    void delete (Iterable<? extends M> entities);

    void deleteAll ();

    Object findAll (Sort sort);

    Object findAll (Pageable pageable);
}
