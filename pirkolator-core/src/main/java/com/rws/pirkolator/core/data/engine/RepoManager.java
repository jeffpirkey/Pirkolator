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

import java.util.UUID;
import java.util.concurrent.locks.Lock;

import javax.annotation.Nullable;
import javax.annotation.Resource;

import com.hazelcast.core.MultiMap;
import com.rws.pirkolator.core.data.access.IDao;
import com.rws.pirkolator.core.grid.HazelcastGrid;
import com.rws.pirkolator.core.grid.exception.AcquireLockException;

/**
 * This class provides federated management of repositories to provide
 * locking and access control for cases where you only want one
 * SystemInfo instance to access a repo.
 * 
 * @author jpirkey
 *
 */
public class RepoManager {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** Spring **/
    @Resource
    @Nullable
    HazelcastGrid grid;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public RepoManager () {

        super ();
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    HazelcastGrid getGrid () {

        return notNull (grid, "HazelcastGrid is undefined in the RepoManager. "
                + "This is an autowired @Resource and indicates that Spring "
                + "has not been configured or initialized correctly. "
                + "Check your Spring configuration to ensure that the HazelcastGrid is included correctly");
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void addDao (final IDao dao) {

        final String sourceUniqueIdentifier = dao.getSource ().getId ();
        final MultiMap<String, UUID> map = getGrid ().getInstance ().getMultiMap ("dao-registry");
        map.put (sourceUniqueIdentifier, dao.getId ());
    }

    public void removeDao (final IDao dao) {

        final String sourceUniqueIdentifier = dao.getSource ().getId ();
        final MultiMap<String, UUID> map = getGrid ().getInstance ().getMultiMap ("dao-registry");
        map.remove (sourceUniqueIdentifier, dao.getId ());
    }

    public Lock acquireLock (final IDao dao) {

        final String sourceUniqueIdentifier = dao.getSource ().getId ();
        final Lock lock = getGrid ().getInstance ().getLock (sourceUniqueIdentifier);
        if (lock != null) {
            return lock;
        }

        throw new AcquireLockException ("Unable to acquire Lock from HazelcastGrid.");
    }

}
