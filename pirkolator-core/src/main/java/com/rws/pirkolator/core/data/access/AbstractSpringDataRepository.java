package com.rws.pirkolator.core.data.access;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

import com.google.common.collect.ImmutableList;
import com.rws.pirkolator.core.data.access.exception.UnsupportedRepositoryException;
import com.rws.pirkolator.core.data.access.exception.UnsupportedRepositoryFunctionException;
import com.rws.pirkolator.model.AbstractSystemIdentifiable;

public abstract class AbstractSpringDataRepository<T, ID extends Serializable> extends AbstractSystemIdentifiable
        implements IRepository<T, T, ID> {

    private final Class<T> objectType;
    private final Class<ID> idType;

    private final Repository<T, ID> repo;

    private final boolean pageSortEnabled;
    private final boolean crudEnabled;

    public AbstractSpringDataRepository (final UUID id, final Repository<T, ID> repo, final Class<T> objectType,
            final Class<ID> idType) {

        this (id, objectType.getName () + " Spring Data Repository", repo, objectType, idType);
    }

    public AbstractSpringDataRepository (final UUID id, final String name, final Repository<T, ID> repo,
            final Class<T> objectType, final Class<ID> idType) {

        super (id, name);

        this.repo = repo;
        this.objectType = objectType;
        this.idType = idType;

        if (repo instanceof PagingAndSortingRepository) {
            pageSortEnabled = true;
        } else {
            pageSortEnabled = false;
        }

        if (repo instanceof CrudRepository) {
            crudEnabled = true;
        } else {
            crudEnabled = false;
        }
    }

    private CrudRepository<T, ID> getCrudRepo () {

        if (crudEnabled) {
            return (CrudRepository<T, ID>) repo;
        }

        throw new UnsupportedRepositoryFunctionException ("Spring Data Crud Repository function not supported");
    }

    private PagingAndSortingRepository<T, ID> getPageSortRepo () {

        if (pageSortEnabled) {
            return (PagingAndSortingRepository<T, ID>) repo;
        }

        throw new UnsupportedRepositoryFunctionException (
                "Spring Data Paging and Sorting Repository function not supported");
    }

    @Override
    public T save (final T entity) {

        final T obj = getCrudRepo ().save (entity);
        return checkNotNull (obj);
    }

    @Override
    public Iterable<? extends T> save (final Iterable<T> entities) {

        final Iterable<? extends T> obj = getCrudRepo ().save (entities);
        if (obj != null) {
            return obj;
        }

        return ImmutableList.of ();
    }

    @Override
    public @Nullable
    T findOne (final ID id) {

        return getCrudRepo ().findOne (id);
    }

    @Override
    public boolean exists (final ID id) {

        return getCrudRepo ().exists (id);
    }

    @Override
    public Iterable<? extends T> findAll () {

        final Iterable<? extends T> obj = getCrudRepo ().findAll ();
        if (obj != null) {
            return obj;
        }

        return ImmutableList.of ();
    }

    @Override
    public Iterable<? extends T> findAll (final Iterable<ID> ids) {

        final Iterable<? extends T> obj = getCrudRepo ().findAll (ids);
        if (obj != null) {
            return obj;
        }

        return ImmutableList.of ();
    }

    @Override
    public abstract Set<ID> findAllIDs ();
    
    @Override
    public abstract Iterable<? extends T> findByParameterMap (Map<String, Object> paramMap);

    @Override
    public long count () {

        return getCrudRepo ().count ();
    }

    @Override
    public void delete (final ID id) {

        getCrudRepo ().delete (id);
    }

    @Override
    public void delete (final T entity) {

        getCrudRepo ().delete (entity);
    }

    @Override
    public void delete (final Iterable<? extends T> entities) {

        getCrudRepo ().delete (entities);
    }

    @Override
    public void deleteAll () {

        getCrudRepo ().deleteAll ();
    }

    @Override
    public Iterable<T> findAll (final Sort sort) {

        final Iterable<T> obj = getPageSortRepo ().findAll (sort);
        if (obj != null) {
            return obj;
        }

        return ImmutableList.of ();
    }

    @Override
    public Page<? extends T> findAll (final Pageable pageable) {

        final Page<? extends T> obj = getPageSortRepo ().findAll (pageable);

        return checkNotNull (obj);
    }

    @Override
    public Object getImplementation () {

        return repo;
    }

    @Override
    public <R> R getImplementation (final Class<R> repoType) {

        if (repoType.isAssignableFrom (repo.getClass ())) {
            final R tmpRepo = repoType.cast (repo);
            return checkNotNull (tmpRepo);
        }

        throw new UnsupportedRepositoryException ("Repository type " + repoType.getName () + " not supported.");
    }

    @Override
    public Class<T> getObjectType () {

        return objectType;
    }

    @Override
    public Class<ID> getIdType () {

        return idType;
    }

}
