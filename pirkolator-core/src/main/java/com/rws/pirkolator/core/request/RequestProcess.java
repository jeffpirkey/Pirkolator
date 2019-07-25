package com.rws.pirkolator.core.request;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.rws.pirkolator.model.request.Request;
import com.rws.pirkolator.model.request.RequestState;
import com.rws.pirkolator.model.request.RequestStats;
import com.rws.utility.common.Externalizables;
import com.rws.utility.common.UUIDs;

public class RequestProcess implements Externalizable {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private Request request;
    private RequestStats requestStats = new RequestStats ();
    private RequestState requestState = RequestState.UNKNOWN;
    private String coordinatorId;

    private final Set<UUID> fulfillerSet = Sets.newConcurrentHashSet ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public RequestProcess () {

        super ();

        request = new Request ();
        coordinatorId = UUIDs.defaultUUIDAsString ();
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public Request getRequest () {

        return request;
    }

    public void setRequest (final Request request) {

        requestStats.setId (request.getId ());

        this.request = request;
    }

    public RequestStats getStats () {

        return requestStats;
    }

    public void setStats (final RequestStats stats) {

        requestStats = stats;
    }

    public Set<UUID> getFulfillerSet () {

        return ImmutableSet.copyOf (fulfillerSet);
    }

    public void setFulfillerSet (final Collection<UUID> values) {

        fulfillerSet.clear ();
        fulfillerSet.addAll (values);
    }

    public RequestState getState () {

        return requestState;
    }

    public void setState (final RequestState state) {

        requestState = state;
    }

    public String getCoordinatorId () {

        return coordinatorId;
    }

    public void setCoordinatorId (final String id) {

        coordinatorId = id;
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void addFulfiller (final UUID id) {

        fulfillerSet.add (id);
    }

    public boolean removeFulfiller (final UUID id) {

        return fulfillerSet.remove (id);
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public void writeExternal (final @Nullable ObjectOutput out) throws IOException {

        checkNotNull (out);

        out.writeInt (fulfillerSet.size ());
        for (final UUID id : fulfillerSet) {
            out.writeUTF (id.toString ());
        }

        out.writeObject (request);
        out.writeUTF (requestState.toString ());
        requestStats.writeExternal (out);
        out.writeUTF (coordinatorId);
    }

    @Override
    public void readExternal (final @Nullable ObjectInput in) throws IOException, ClassNotFoundException {

        checkNotNull (in);

        fulfillerSet.clear ();
        final int size = in.readInt ();
        for (int i = 0; i < size; i++) {
            fulfillerSet.add (UUID.fromString (in.readUTF ()));
        }
        
        request = Externalizables.readObjectAs (in, Request.class);
        requestState = RequestState.valueOf (Externalizables.readUTF (in));
        requestStats = new RequestStats ();
        requestStats.readExternal (in);
        coordinatorId = Externalizables.readUTF (in);
    }

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), request, requestStats, requestState, coordinatorId, fulfillerSet);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof RequestProcess) {
            if (!super.equals (object))
                return false;
            final RequestProcess that = (RequestProcess) object;
            return Objects.equal (request, that.request) && Objects.equal (requestStats, that.requestStats)
                    && Objects.equal (requestState, that.requestState)
                    && Objects.equal (coordinatorId, that.coordinatorId)
                    && Objects.equal (fulfillerSet, that.fulfillerSet);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("request", request)
                .add ("requestStats", requestStats).add ("requestState", requestState)
                .add ("coordinatorId", coordinatorId).add ("fulfillerSet", fulfillerSet).toString ();
    }

}