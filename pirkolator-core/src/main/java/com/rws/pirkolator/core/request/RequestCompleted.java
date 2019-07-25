package com.rws.pirkolator.core.request;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.annotation.Nullable;

import com.rws.utility.common.Externalizables;
import com.rws.utility.common.UUIDs;

public class RequestCompleted implements Externalizable {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private String requestId;
    private String coordinatorId;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public RequestCompleted () {

        super ();

        requestId = UUIDs.defaultUUIDAsString ();
        coordinatorId = UUIDs.defaultUUIDAsString ();
    }

    public RequestCompleted (final String requestId, final String coordinatorId) {

        super ();

        this.requestId = requestId;
        this.coordinatorId = coordinatorId;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getRequestId () {

        return requestId;
    }

    public void setRequestId (final String requestId) {

        this.requestId = requestId;
    }

    public String getCoordinatorId () {

        return coordinatorId;
    }

    public void setCoordinatorId (final String coordinatorId) {

        this.coordinatorId = coordinatorId;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public void writeExternal (final @Nullable ObjectOutput out) throws IOException {

        checkNotNull (out);

        out.writeUTF (checkNotNull (coordinatorId));
        out.writeUTF (checkNotNull (requestId));
    }

    @Override
    public void readExternal (final @Nullable ObjectInput in) throws IOException, ClassNotFoundException {

        checkNotNull (in);

        coordinatorId = Externalizables.readUTF (in);
        requestId = Externalizables.readUTF (in);
    }

}
