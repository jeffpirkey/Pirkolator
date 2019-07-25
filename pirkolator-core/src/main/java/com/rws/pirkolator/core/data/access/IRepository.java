package com.rws.pirkolator.core.data.access;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.rws.pirkolator.schema.ISystemIdentifiable;


public interface IRepository<M, T extends M, ID extends Serializable> extends IDataAccess <M, T, ID>, ISystemIdentifiable {

    Class<T> getObjectType ();
    
    Class<ID> getIdType ();
    
    Object getImplementation ();
    
    <R> R getImplementation (Class<R> repoType);
    
    /**
     * 
     * @param id
     * @return M instance of the defined ID or null if an instance was not found
     */
    @Override
    @Nullable M findOne (ID id);
    
    @Override
    boolean exists (ID id);

    @Override
    Iterable<? extends M> findAll ();

    @Override
    Iterable<? extends M> findAll (Iterable<ID> ids);
    
    @Override
    Set<ID> findAllIDs ();

    @Override
    long count ();

    @Override
    void delete (ID id);

    @Override
    void delete (M entity);

    @Override
    void delete (Iterable<? extends M> entities);

    @Override
    void deleteAll ();

    @Override
    Iterable<? extends M> findAll (Sort sort);

    /**
     * 
     * @param pageable
     * @return returns {@link Page} of entities
     */
    @Override
    Page<? extends M> findAll (Pageable pageable);
    
    Iterable<? extends M> findByParameterMap (Map<String, Object> paramMap);
}
