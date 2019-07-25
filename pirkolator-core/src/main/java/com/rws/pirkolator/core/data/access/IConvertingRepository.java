package com.rws.pirkolator.core.data.access;

import java.io.Serializable;

public interface IConvertingRepository<M, T extends M, ID extends Serializable> extends IRepository<M, T, ID> {

    T convert (M model);

    Iterable<T> convert (Iterable<? extends M> modelList);

}
