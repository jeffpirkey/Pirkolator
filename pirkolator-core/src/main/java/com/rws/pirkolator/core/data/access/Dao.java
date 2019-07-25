package com.rws.pirkolator.core.data.access;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.rws.pirkolator.core.data.access.exception.UnsupportedRepositoryException;
import com.rws.pirkolator.model.AbstractSystemIdentifiable;
import com.rws.pirkolator.model.Metadata;
import com.rws.pirkolator.schema.IDaoSource;

public class Dao extends AbstractSystemIdentifiable implements IDao {

    private final IDaoSource source;
    private final Set<IRepository<?, ?, ? extends Serializable>> repoSet = Sets.newConcurrentHashSet ();
    private final Metadata metadata = new Metadata ();

    public Dao (final UUID id, final IDaoSource source) {

        super (id, source.getLabel () + " Dao");

        this.source = source;
    }

    @Override
    public IDaoSource getSource () {

        return source;
    }

    @Override
    public Metadata getMetadata () {

        return metadata;
    }

    public void setMetadataMap (final @Nullable Map<String, String> mapToAdd) {

        metadata.getMap ().putAll (mapToAdd);
    }

    @Override
    public Set<IRepository<?, ?, ? extends Serializable>> getRepositorySet () {

        return ImmutableSet.copyOf (repoSet);
    }

    public void setRepositorySet (final Set<IRepository<?, ?, ? extends Serializable>> set) {

        repoSet.clear ();
        repoSet.addAll (set);
    }

    @SuppressWarnings ("unchecked")
    @Override
    public <M, T extends M, ID extends Serializable> IRepository<M, T, ID> getRepository (final Class<T> objectType,
            final Class<ID> idType) {

        for (final IRepository<?, ?, ? extends Serializable> repo : repoSet) {
            if (objectType.isAssignableFrom (repo.getObjectType ()) && idType.isAssignableFrom (repo.getIdType ())) {
                return (IRepository<M, T, ID>) repo;
            }
        }

        throw new UnsupportedRepositoryException ("No repositories found matching the constraints of type="
                + objectType.getName () + " id=" + idType.getName ());
    }

    @Override
    public <M, T extends M, ID extends Serializable> boolean supports (final Class<T> objectType, final Class<ID> idType) {

        for (final IRepository<?, ?, ? extends Serializable> repo : repoSet) {
            if (objectType.isAssignableFrom (repo.getObjectType ()) && idType.isAssignableFrom (repo.getIdType ())) {
                return true;
            }
        }

        return false;
    }
}
