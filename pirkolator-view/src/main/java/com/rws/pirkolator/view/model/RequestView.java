package com.rws.pirkolator.view.model;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.rws.utility.common.Preconditions.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.model.AbstractLabeledIdentifiable;

public class RequestView extends AbstractLabeledIdentifiable {

    private static final long serialVersionUID = 1L;

    public static Builder newBuilder () {

        return new Builder ();
    }

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private String requestType;
    private String requesterId;
    private String requestState;
    private long timeSubmitted;
    private long timeCompleted;

    private int currentProgress;
    private int totalProgress;

    private final List<String> parameterList = new ArrayList<> ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public RequestView (final String id, final String label, final String requestType, final String requesterId,
            final String requestState, final long timeSubmitted, final long timeCompleted, final int currentProgress,
            final int totalProgress) {

        super (id, label);
        this.requestType = requestType;
        this.requesterId = requesterId;
        this.requestState = requestState;
        this.timeSubmitted = timeSubmitted;
        this.timeCompleted = timeCompleted;
        this.currentProgress = currentProgress;
        this.totalProgress = totalProgress;
    }

    public RequestView (final String id, final String label, final String requestType, final String requesterId,
            final String requestState, final long timeSubmitted, final long timeCompleted, final int currentProgress,
            final int totalProgress, final Collection<String> list) {

        this (id, label, requestType, requesterId, requestState, timeSubmitted, timeCompleted, currentProgress,
                totalProgress);

        parameterList.addAll (list);
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getRequesterId () {

        return requesterId;
    }

    public void setRequesterId (final String requesterId) {

        this.requesterId = requesterId;
    }

    public String getRequestType () {

        return requestType;
    }

    public void setRequestType (final String requestType) {

        this.requestType = requestType;
    }

    public String getRequestState () {

        return requestState;
    }

    public void setRequestState (final String requestState) {

        this.requestState = requestState;
    }

    public long getTimeSubmitted () {

        return timeSubmitted;
    }

    public void setTimeSubmitted (final long timeSubmitted) {

        this.timeSubmitted = timeSubmitted;
    }

    public long getTimeCompleted () {

        return timeCompleted;
    }

    public void setTimeCompleted (final long timeCompleted) {

        this.timeCompleted = timeCompleted;
    }

    public int getCurrentProgress () {

        return currentProgress;
    }

    public void setCurrentProgress (final int currentProgress) {

        this.currentProgress = currentProgress;
    }

    public int getTotalProgress () {

        return totalProgress;
    }

    public void setTotalProgress (final int totalProgress) {

        this.totalProgress = totalProgress;
    }

    public List<String> getParameterList () {

        return parameterList;
    }

    public void setParameterList (final Collection<String> paramList) {

        parameterList.clear ();
        parameterList.addAll (paramList);
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void addParameter (final String param) {

        parameterList.add (checkNotNull (param));
    }

    public void addAllParameter (final Collection<String> params) {

        // TODO jpirkey check each value for null?
        parameterList.addAll (checkNotNull (params));
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), requestType, requesterId, requestState, timeSubmitted,
                timeCompleted, currentProgress, totalProgress, parameterList);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof RequestView) {
            if (!super.equals (object))
                return false;
            final RequestView that = (RequestView) object;
            return Objects.equal (requestType, that.requestType) && Objects.equal (requesterId, that.requesterId)
                    && Objects.equal (requestState, that.requestState)
                    && Objects.equal (timeSubmitted, that.timeSubmitted)
                    && Objects.equal (timeCompleted, that.timeCompleted)
                    && Objects.equal (currentProgress, that.currentProgress)
                    && Objects.equal (totalProgress, that.totalProgress)
                    && Objects.equal (parameterList, that.parameterList);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("requestType", requestType)
                .add ("requesterId", requesterId).add ("requestState", requestState)
                .add ("timeSubmitted", timeSubmitted).add ("timeCompleted", timeCompleted)
                .add ("currentProgress", currentProgress).add ("totalProgress", totalProgress)
                .add ("parameterList", parameterList).toString ();
    }

    // *************************************************************************
    // ** Member classes
    // *************************************************************************

    public static class Builder {

        @Nullable
        private String id;
        @Nullable
        private String label;
        @Nullable
        private String requestType;
        @Nullable
        private String requesterId;
        @Nullable
        private String requestState;
        @Nullable
        private Long timeSubmitted;
        @Nullable
        private Long timeCompleted;
        @Nullable
        private Integer currentProgress;
        @Nullable
        private Integer totalProgress;

        private final List<String> parameterList = new ArrayList<> ();

        public Builder addParameter (final String value) {

            parameterList.add (value);
            return this;
        }

        public Builder id (final String value) {

            id = value;
            return this;
        }

        public Builder label (final String value) {

            label = value;
            return this;
        }

        public Builder requestType (final String value) {

            requestType = value;
            return this;
        }

        public Builder requesterId (final String value) {

            requesterId = value;
            return this;
        }

        public Builder requestState (final String state) {

            requestState = state;
            return this;
        }

        public Builder timeSubmitted (final long value) {

            timeSubmitted = value;
            return this;
        }

        public Builder timeCompleted (final long value) {

            timeCompleted = value;
            return this;
        }

        public Builder currentProgress (final int value) {

            currentProgress = value;
            return this;
        }

        public Builder totalProgress (final int value) {

            totalProgress = value;
            return this;
        }

        public Builder parameterList (final Collection<String> c) {

            parameterList.addAll (c);
            return this;
        }

        public RequestView build () {

            return new RequestView (notNull (id), notNull (label), notNull (requestType),
                    notNull (requesterId), notNull (requestState), notNull (timeSubmitted),
                    notNull (timeCompleted), notNull (currentProgress), notNull (totalProgress));
        }
    }
}
