package com.rws.pirkolator.model.filter;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.rws.pirkolator.model.Message;
import com.rws.pirkolator.model.request.Request;
import com.rws.utility.common.Externalizables;

public class RequestFilter implements IContentFilter, Externalizable {

    private static final Set<Class<?>> sTypeSet = new HashSet<> ();
    static {
        sTypeSet.add (Request.class);
    }

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private String requestName;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public RequestFilter () {

        super ();

        requestName = "default";
    }

    public RequestFilter (final String requestName) {

        super ();

        this.requestName = requestName;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getRequestName () {

        return requestName;
    }

    public void setRequestName (final String requestName) {

        this.requestName = requestName;
    }

    @Override
    public Set<Class<?>> getTypeSet () {

        return sTypeSet;
    }

    @Override
    public MatchType getMatchType () {

        return MatchType.Any;
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    @Override
    public boolean match (final IFilter filter) {

        if (filter instanceof RequestFilter) {

            return requestName.equals (((RequestFilter) filter).getRequestName ());
        }

        return false;
    }

    @Override
    public boolean supports (final Message message) {

        return message.containsType (Request.class);
    }

    @Override
    public boolean supportsObject (final Object object, final String[] propertyList) {

        if (object instanceof Request) {
            final Request request = (Request) object;

            for (final String prop : propertyList) {
                if (prop.equals (request.getRequestType ())) {
                    return true;
                }
            }

            if (requestName.equals (request.getRequestType ())) {
                return true;
            }
        }

        return false;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public void writeExternal (final @Nullable ObjectOutput out) throws IOException {

        checkNotNull (out);

        out.writeUTF (requestName);
    }

    @Override
    public void readExternal (final @Nullable ObjectInput in) throws IOException, ClassNotFoundException {

        checkNotNull (in);
        
        requestName = Externalizables.readUTF (in);
    }

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), requestName);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof RequestFilter) {
            if (!super.equals (object))
                return false;
            final RequestFilter that = (RequestFilter) object;
            return Objects.equal (requestName, that.requestName);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("requestName", requestName)
                .toString ();
    }
}
