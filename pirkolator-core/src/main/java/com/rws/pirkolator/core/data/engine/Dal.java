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
package com.rws.pirkolator.core.data.engine;

import static com.rws.utility.common.Preconditions.notNull;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.ImmutableSet;
import com.rws.pirkolator.core.data.access.DaoChannel;
import com.rws.pirkolator.core.data.access.IDao;
import com.rws.pirkolator.core.data.access.IDaoChannel;
import com.rws.pirkolator.core.data.access.IMultiDaoChannel;
import com.rws.pirkolator.core.data.access.IRepository;
import com.rws.pirkolator.core.data.access.MultiDaoChannel;
import com.rws.pirkolator.core.data.access.exception.UnsupportedDaoSourceException;
import com.rws.pirkolator.core.data.access.exception.UnsupportedRepositoryTypeException;
import com.rws.pirkolator.core.engine.SystemReady;
import com.rws.pirkolator.core.registry.SystemRegistry;
import com.rws.pirkolator.schema.IDaoSource;
import com.rws.utility.common.Globals;

public class Dal implements ApplicationContextAware {

    // TODO jpirkey Need to fix dynamic registry of DAOs

    private final static Logger LOG = notNull (LoggerFactory.getLogger (Dal.class));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** Spring **/
    @Nullable
    ApplicationContext applicationContext;

    @Resource
    @Nullable
    SystemRegistry systemRegistry;

    @Resource
    @Nullable
    SystemReady systemReady;

    /** define **/
    private final Map<UUID, IDao> daoMap = new ConcurrentHashMap<> ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Dal () {

        super ();
    }

    public Set<IDao> getDaoSet () {

        return ImmutableSet.copyOf (notNull (daoMap.values ()));
    }

    // *************************************************************************
    // ** Spring methods 
    // *************************************************************************

    @Override
    public void setApplicationContext (final @Nullable ApplicationContext appCtx) throws BeansException {

        applicationContext = appCtx;
    }

    ApplicationContext getApplicationContext () {

        return notNull (applicationContext, "Spring Application Context is undefined. "
                + "This indicates that Spring has not been initialized correctly.");
    }

    SystemRegistry getSystemRegistry () {

        return notNull (systemRegistry, "The System Registry is undefined. "
                + "This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    SystemReady getSystemReady () {

        return notNull (systemReady, "The System Ready instance is undefined. "
                + "This indicates that Spring has not been initialized correctly. Check the Spring configuration.");
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public <T, ID extends Serializable> IMultiDaoChannel<T, ID> getChannel (final Class<T> objType,
            final Class<ID> idType) {

        final MultiDaoChannel<T, ID> channel = new MultiDaoChannel<> ();

        boolean added = false;
        for (final IDao dao : daoMap.values ()) {
            if (dao.supports (objType, idType)) {
                final IRepository<T, T, ID> repo = dao.getRepository (objType, idType);
                channel.addRepository (dao.getSource (), repo);
                added = true;
            }
        }

        if (added) {
            return channel;
        }

        throw new UnsupportedRepositoryTypeException (objType.getName () + " not supported by the DAL.");
    }

    public <T, ID extends Serializable> IDaoChannel<T, ID> getChannel (final IDaoSource source, final Class<T> objType,
            final Class<ID> idType) {

        for (final IDao dao : daoMap.values ()) {
            if (source.equals (dao.getSource ()) && dao.supports (objType, idType)) {
                final IRepository<T, T, ID> repo = dao.getRepository (objType, idType);
                return new DaoChannel<> (source, repo);
            }
        }

        throw new UnsupportedDaoSourceException (source.getLabel () + " not supported by the DAL.");
    }

    public void registerDao (final IDao dao) {

        daoMap.put (dao.getId (), dao);
    }

    // *************************************************************************
    // ** Life-cycle
    // *************************************************************************

    @PostConstruct
    private void postConstruct () {

        if (applicationContext != null) {
            // IDao
            for (final IDao dao : getApplicationContext ().getBeansOfType (IDao.class).values ()) {
                if (dao != null) {
                    registerDao (dao);
                    getSystemRegistry ().register (dao);
                } else {
                    LOG.warn ("null IDao present in Spring context");
                }
            }
        }

        final StringBuilder builder = new StringBuilder ();
        builder.append ("DAL Information");
        builder.append (Globals.DASH_LINE).append (Globals.NEW_LINE);
        if (daoMap.isEmpty ()) {
            builder.append ("No registered DAOs").append (Globals.NEW_LINE);
        } else {
            for (final IDao dao : daoMap.values ()) {
                builder.append (dao.getName ()).append (Globals.NEW_LINE);
                builder.append ("- ").append (dao.getSource ().getLabel ()).append (" [")
                        .append (dao.getSource ().getId ()).append ("]").append (Globals.NEW_LINE);
                if (dao.getMetadata ().getMap ().isEmpty ()) {
                    builder.append ("- No metadata").append (Globals.NEW_LINE);
                } else {
                    for (final Entry<String, String> entry : dao.getMetadata ().getMap ().entrySet ()) {
                        builder.append ("- ").append (entry.getKey ()).append (" : ").append (entry.getValue ())
                                .append (Globals.NEW_LINE);
                    }
                }
            }
        }
        builder.append (Globals.DASH_LINE);
        LOG.info (builder.toString ());

        getSystemReady ().readyDal ();
    }

    @PreDestroy
    private void preDestroy () {

        for (final IDao dao : daoMap.values ()) {
            if (dao != null) {
                getSystemRegistry ().unregister (dao);
            } else {
                LOG.warn ("null IDao present in Spring context");
            }
        }
    }
}
