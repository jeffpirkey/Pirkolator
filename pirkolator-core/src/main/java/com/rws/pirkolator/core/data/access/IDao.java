package com.rws.pirkolator.core.data.access;

import java.io.Serializable;
import java.util.Set;

import com.rws.pirkolator.model.Metadata;
import com.rws.pirkolator.schema.IDaoSource;
import com.rws.pirkolator.schema.ISystemIdentifiable;

public interface IDao extends ISystemIdentifiable {

    IDaoSource getSource ();

    Metadata getMetadata ();

    Set<IRepository<?, ?, ? extends Serializable>> getRepositorySet ();

    <M, T extends M, ID extends Serializable> IRepository<M, T, ID> getRepository (Class<T> objectType, Class<ID> idType);

    <M, T extends M, ID extends Serializable> boolean supports (Class<T> objectType, Class<ID> idType);
}
