package com.rws.pirkolator.core.data.access;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.Repository;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.rws.pirkolator.schema.IDaoSource;

public class MultiDaoChannel<T, ID extends Serializable> implements IMultiDaoChannel<T, ID> {

    Map<IDaoSource, IRepository<T, T, ID>> repoMap = new ConcurrentHashMap<> ();

    public void addRepository (final IDaoSource source, final IRepository<T, T, ID> repo) {

        repoMap.put (checkNotNull (source), checkNotNull (repo));
    }

    @Override
    public T save (final T entity) {

        for (final IRepository<T, T, ID> repo : repoMap.values ()) {
            repo.save (entity);
        }

        return entity;
    }

    @Override
    public Iterable<? extends T> save (final Iterable<T> entities) {

        for (final IRepository<T, T, ID> repo : repoMap.values ()) {
            repo.save (entities);
        }

        return entities;
    }

    @Override
    public Map<IDaoSource, T> findOne (final ID id) {

        final Map<IDaoSource, T> map = Maps.newConcurrentMap ();
        for (final Entry<IDaoSource, IRepository<T, T, ID>> entry : repoMap.entrySet ()) {
            final T one = entry.getValue ().findOne (id);
            if (one != null) {
                map.put (entry.getKey (), one);
            }
        }
        return map;
    }

    @Override
    public boolean exists (final ID id) {

        for (final IRepository<T, T, ID> repo : repoMap.values ()) {
            if (repo.exists (id)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public Map<IDaoSource, Iterable<? extends T>> findAll () {

        final Map<IDaoSource, Iterable<? extends T>> map = Maps.newConcurrentMap ();
        for (final Entry<IDaoSource, IRepository<T, T, ID>> entry : repoMap.entrySet ()) {
            final Iterable<? extends T> one = entry.getValue ().findAll ();
            map.put (entry.getKey (), one);
        }
        return map;
    }

    @Override
    public Map<IDaoSource, Iterable<? extends T>> findAll (final Iterable<ID> ids) {

        final Map<IDaoSource, Iterable<? extends T>> map = Maps.newConcurrentMap ();
        for (final Entry<IDaoSource, IRepository<T, T, ID>> entry : repoMap.entrySet ()) {
            final Iterable<? extends T> one = entry.getValue ().findAll (ids);
            map.put (entry.getKey (), one);
        }
        return map;
    }

    @Override
    public Set<ID> findAllIDs () {

        final Set<ID> set = Sets.newConcurrentHashSet ();
        for (final IRepository<T, T, ID> repo : repoMap.values()) {
            set.addAll(repo.findAllIDs ());
        }
        
        return set;
    }

    @Override
    public long count () {

        long count = 0;
        for (final IRepository<T, T, ID> repo : repoMap.values()) {
            count += repo.count ();
        }
        
        return count;
    }

    @Override
    public void delete (final ID id) {

        for (final IRepository<T, T, ID> repo : repoMap.values()) {
            repo.delete (id);
        }
    }

    @Override
    public void delete (final T entity) {

        for (final IRepository<T, T, ID> repo : repoMap.values()) {
            repo.delete (entity);
        }
    }

    @Override
    public void delete (final Iterable<? extends T> entities) {

        for (final IRepository<T, T, ID> repo : repoMap.values()) {
            repo.delete (entities);
        }
    }

    @Override
    public void deleteAll () {

        for (final IRepository<T, T, ID> repo : repoMap.values()) {
            repo.deleteAll ();
        }
    }

    @Override
    public Map<IDaoSource, Iterable<? extends T>> findAll (final Sort sort) {

        final Map<IDaoSource, Iterable<? extends T>> map = Maps.newConcurrentMap ();
        for (final Entry<IDaoSource, IRepository<T, T, ID>> entry : repoMap.entrySet ()) {
            final Iterable<? extends T> one = entry.getValue ().findAll (sort);
            map.put (entry.getKey (), one);
        }
        return map;
    }

    @Override
    public Map<IDaoSource, Page<? extends T>> findAll (final Pageable pageable) {

        final Map<IDaoSource, Page<? extends T>> map = Maps.newConcurrentMap ();
        for (final Entry<IDaoSource, IRepository<T, T, ID>> entry : repoMap.entrySet ()) {
            final Page<? extends T> one = entry.getValue ().findAll (pageable);
            map.put (entry.getKey (), one);
        }
        return map;
    }

    @Override
    public Map<IDaoSource, Repository<T, ID>> getRepositoryMap () {
        final Map<IDaoSource, Repository<T, ID>> map = Maps.newConcurrentMap ();
        for (final Entry<IDaoSource, IRepository<T, T, ID>> entry : repoMap.entrySet ()) {
            map.put (entry.getKey (), entry.getValue ().getImplementation (Repository.class));
        }
        
        return map;
    }
}
