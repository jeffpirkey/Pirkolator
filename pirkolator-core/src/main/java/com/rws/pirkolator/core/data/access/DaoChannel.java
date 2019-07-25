package com.rws.pirkolator.core.data.access;

import java.io.Serializable;
import java.util.Set;

import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.rws.pirkolator.schema.IDaoSource;

public class DaoChannel<T, ID extends Serializable> implements IDaoChannel<T, ID> {

    IDaoSource source;
    IRepository<T, T, ID> repo;
    
    public DaoChannel (final IDaoSource source, final IRepository<T, T, ID> repo) {
        
        super ();
        
        this.source = source;
        this.repo = repo;
    }

    @Override
    public IDaoSource getSource () {

        return source;
    }

    @Override
    public T save (final T entity) {

        return repo.save (entity);
    }

    @Override
    public Iterable<? extends T> save (final Iterable<T> entities) {

        return repo.save (entities);
    }

    @Override
    public @Nullable T findOne (final ID id) {

        return repo.findOne (id); 
    }

    @Override
    public boolean exists (final ID id) {

        return repo.exists (id);
    }

    @Override
    public Iterable<? extends T> findAll () {

        return repo.findAll ();
    }

    @Override
    public Iterable<? extends T> findAll (final Iterable<ID> ids) {

        return repo.findAll(ids);
    }

    @Override
    public Set<ID> findAllIDs () {

        return repo.findAllIDs ();
    }

    @Override
    public long count () {

        return repo.count ();
    }

    @Override
    public void delete (final ID id) {

        repo.delete (id);
    }

    @Override
    public void delete (final T entity) {

        repo.delete (entity);
    }

    @Override
    public void delete (final Iterable<? extends T> entities) {

        repo.delete (entities);
    }

    @Override
    public void deleteAll () {

        repo.deleteAll ();
    }

    @Override
    public Iterable<? extends T> findAll (final Sort sort) {

        return repo.findAll (sort);
    }

    @Override
    public Page<? extends T> findAll (final Pageable pageable) {

        return repo.findAll (pageable);
    }
}
