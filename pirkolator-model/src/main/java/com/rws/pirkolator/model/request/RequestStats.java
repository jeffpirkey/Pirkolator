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
package com.rws.pirkolator.model.request;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;
import com.rws.utility.common.Externalizables;
import com.rws.utility.common.UUIDs;

@XmlRootElement
public class RequestStats implements Externalizable {

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    private String id;
    private long timeCompleted;
    private long timeSubmitted;
    private int currentProgress;
    private int totalProgress;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public RequestStats () {

        super ();
        
        id = UUIDs.defaultUUIDAsString ();
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public String getId () {

        return id;
    }

    public void setId (final String id) {

        this.id = id;
    }

    public long getTimeCompleted () {

        return timeCompleted;
    }

    public void setTimeCompleted (final long timeCompleted) {

        this.timeCompleted = timeCompleted;
    }

    public long getTimeSubmitted () {

        return timeSubmitted;
    }

    public void setTimeSubmitted (final long timeSubmitted) {

        this.timeSubmitted = timeSubmitted;
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

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public void writeExternal (final @Nullable ObjectOutput out) throws IOException {

        checkNotNull (out);

        out.writeLong (timeCompleted);
        out.writeInt (currentProgress);
        out.writeUTF (checkNotNull (id));
        out.writeLong (timeSubmitted);
        out.writeInt (totalProgress);
    }

    @Override
    public void readExternal (final @Nullable ObjectInput in) throws IOException, ClassNotFoundException {

        checkNotNull (in);

        timeCompleted = in.readLong ();
        currentProgress = in.readInt ();
        id = Externalizables.readUTF (in);
        timeSubmitted = in.readLong ();
        totalProgress = in.readInt ();
    }

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), id, timeCompleted, timeSubmitted, currentProgress, totalProgress);
    }

    @Override
    public boolean equals (@Nullable final Object object) {

        if (object instanceof RequestStats) {
            if (!super.equals (object))
                return false;
            final RequestStats that = (RequestStats) object;
            return Objects.equal (id, that.id) && Objects.equal (timeCompleted, that.timeCompleted)
                    && Objects.equal (timeSubmitted, that.timeSubmitted)
                    && Objects.equal (currentProgress, that.currentProgress)
                    && Objects.equal (totalProgress, that.totalProgress);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("id", id)
                .add ("timeCompleted", timeCompleted).add ("timeSubmitted", timeSubmitted)
                .add ("currentProgress", currentProgress).add ("totalProgress", totalProgress).toString ();
    }

}
