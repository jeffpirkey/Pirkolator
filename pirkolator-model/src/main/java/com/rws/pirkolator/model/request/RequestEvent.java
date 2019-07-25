package com.rws.pirkolator.model.request;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

public class RequestEvent {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private Request request;
    private RequestStats requestStats;
    private RequestState requestState;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public RequestEvent (final Request request, final RequestStats requestStats, final RequestState requestState) {

        super ();

        this.request = request;
        this.requestStats = requestStats;
        this.requestState = requestState;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public Request getRequest () {

        return request;
    }

    public void setRequest (final Request request) {

        this.request = request;
    }

    public RequestStats getStats () {

        return requestStats;
    }

    public void setStats (final RequestStats requestStats) {

        this.requestStats = requestStats;
    }

    public RequestState getState () {

        return requestState;
    }

    public void setState (final RequestState requestState) {

        this.requestState = requestState;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (request, requestStats, requestState);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof RequestEvent) {
            final RequestEvent that = (RequestEvent) object;
            return Objects.equal (request, that.request) && Objects.equal (requestStats, that.requestStats)
                    && Objects.equal (requestState, that.requestState);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("request", request).add ("requestStats", requestStats)
                .add ("requestState", requestState).toString ();
    }

}