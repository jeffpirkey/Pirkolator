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
package com.rws.pirkolator.core.registry;

import static com.rws.utility.common.Preconditions.notNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import com.rws.pirkolator.core.store.SystemStatusViewStore;
import com.rws.pirkolator.core.transform.StatusToViewFunction;
import com.rws.pirkolator.model.Status;
import com.rws.pirkolator.model.Status.State;
import com.rws.pirkolator.model.SystemInfo;
import com.rws.pirkolator.schema.ISystemIdentifiable;
import com.rws.pirkolator.view.model.StatusView;
import com.rws.utility.common.UUIDs;

public class StatusRegistry {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Resource
    @Nullable
    private SystemStatusViewStore systemStatusViewStore;

    @Resource (name = "statusToViewFunction")
    @Nullable
    private StatusToViewFunction statusToViewFunction;

    private final SystemInfo systemInfo;

    private Status currentSystemStatus;

    private final Map<UUID, Status> localStatusMap = new ConcurrentHashMap<> ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public StatusRegistry (final SystemInfo systemInfo) {

        super ();

        this.systemInfo = systemInfo;

        currentSystemStatus = new Status (systemInfo.getId ());
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public Status getCurrentSystemStatus () {

        return currentSystemStatus;
    }

    StatusToViewFunction getStatusToViewFunction () {

        return notNull (statusToViewFunction, "StatusToViewFunction has not been defined in the Status Registry."
                + " Check the Spring configuration to ensure the function has been included properly.");
    }

    SystemStatusViewStore getSystemStatusViewStore () {

        return notNull (systemStatusViewStore, "SystemStatusViewStore has not been defined in the Status Registry."
                + " Check the Spring configuration to ensure the store has been included properly.");
    }

    // *************************************************************************
    // ** Component Status methods
    // *************************************************************************

    public void setInitialized (final UUID id) {

        // Only allow status updates for locally controlled analytics
        if (localStatusMap.containsKey (id)) {
            final Status status = findLocalStatus (id);
            status.initialized ();
            updateStatus (status);
        }
    }

    public void setStarting (final UUID id) {

        // Only allow status updates for locally controlled analytics
        if (localStatusMap.containsKey (id)) {
            final Status status = findLocalStatus (id);
            status.starting ();
            updateStatus (status);
        }
    }

    public void setStarted (final UUID id) {

        // Only allow status updates for locally controlled analytics
        final Status status = findLocalStatus (id);
        status.started ();
        updateStatus (status);
    }

    public void setStopping (final UUID id) {

        // Only allow status updates for locally controlled analytics
        if (localStatusMap.containsKey (id)) {
            final Status status = findLocalStatus (id);
            status.stopping ();
            updateStatus (status);
        }
    }

    public void setStopped (final UUID id) {

        // Only allow status updates for locally controlled analytics
        if (localStatusMap.containsKey (id)) {
            final Status status = findLocalStatus (id);
            status.stopped ();
            updateStatus (status);
        }
    }

    public void setException (final UUID id) {

        // Only allow status updates for locally controlled analytics
        if (localStatusMap.containsKey (id)) {
            final Status status = findLocalStatus (id);
            status.exception ();
            updateStatus (status);
        }
    }

    public boolean isInitialized (final UUID id) {

        return State.INITIALIZED.equals (findLocalStatus (id).getState ());
    }

    public boolean isStarting (final UUID id) {

        return State.STARTING.equals (findLocalStatus (id).getState ());
    }

    public boolean isStarted (final UUID id) {

        return State.STARTED.equals (findLocalStatus (id).getState ());
    }

    public boolean isStopping (final UUID id) {

        return State.STOPPING.equals (findLocalStatus (id).getState ());
    }

    public boolean isStopped (final UUID id) {

        return State.STOPPED.equals (findLocalStatus (id).getState ());
    }

    public boolean isException (final UUID id) {

        return State.EXCEPTION.equals (findLocalStatus (id).getState ());
    }

    // *************************************************************************
    // ** System Status methods
    // *************************************************************************

    public void setSystemInitialized () {

        currentSystemStatus.initialized ();
    }

    public void setSystemStarting () {

        currentSystemStatus.initialized ();
    }

    public void setSystemStarted () {

        currentSystemStatus.initialized ();
    }

    public void setSystemStopping () {

        currentSystemStatus.initialized ();
    }

    public void setSystemStopped () {

        currentSystemStatus.initialized ();
    }

    public void setSystemException () {

        currentSystemStatus.initialized ();
    }

    public boolean isSystemInitialized () {

        return State.INITIALIZED.equals (currentSystemStatus.getState ());
    }

    public boolean isSystemStarting () {

        return State.STARTING.equals (currentSystemStatus.getState ());
    }

    public boolean isSystemStarted () {

        return State.STARTED.equals (currentSystemStatus.getState ());
    }

    public boolean isSystemStopping () {

        return State.STOPPING.equals (currentSystemStatus.getState ());
    }

    public boolean isSystemStopped () {

        return State.STOPPED.equals (currentSystemStatus.getState ());
    }

    public boolean isSystemException () {

        return State.EXCEPTION.equals (currentSystemStatus.getState ());
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public Status findLocalStatus (final UUID id) {

        final Status localStatus = localStatusMap.get (id);
        if (localStatus != null) {
            return localStatus;
        }

        final Status tmpStatus = new Status (id);
        tmpStatus.unknown ();

        return tmpStatus;
    }

    public Status register (final ISystemIdentifiable comp) {

        Status currentStatus = localStatusMap.get (comp.getId ());
        if (currentStatus == null) {
            currentStatus = new Status (comp.getId ());
            localStatusMap.put (comp.getId (), currentStatus);
        }

        final StatusView view = getStatusToViewFunction ().apply (currentStatus);
        getSystemStatusViewStore ().put (view);

        return currentStatus;
    }

    public void unregister (final ISystemIdentifiable comp) {

        localStatusMap.remove (comp.getId ());
        getSystemStatusViewStore ().removeById (UUIDs.uuidAsStringOrDefault (comp.getId ()));
    }

    // *************************************************************************
    // ** Life-cycle methods 
    // *************************************************************************

    @PostConstruct
    void postConstruct () {

        // Construct system status instance
        currentSystemStatus = new Status (systemInfo.getId ());
    }

    @PreDestroy
    void preDestroy () {

        for (final Status status : localStatusMap.values ()) {
            getSystemStatusViewStore ().removeById (UUIDs.uuidAsStringOrDefault (status.getComponentId ()));
        }
    }

    void updateStatus (final Status status) {

        localStatusMap.put (status.getComponentId (), status);
    }
}
