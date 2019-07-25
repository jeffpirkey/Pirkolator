/*******************************************************************************
 * Copyright 2014 Reality Warp Software
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
package com.rws.pirkolator.core.engine;

import java.io.Serializable;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Objects;

public class GlobalPublication implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID systemId;
    private final UUID publisherId;
    private final String publisherName;
    private final Publication publication;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public GlobalPublication (final UUID systemId, final UUID publisherId, final String publisherName,
            final Publication publication) {

        super ();

        this.systemId = systemId;
        this.publisherId = publisherId;
        this.publisherName = publisherName;
        this.publication = publication;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public UUID getSystemId () {

        return systemId;
    }

    public UUID getPublisherId () {

        return publisherId;
    }

    public String getPublisherName () {

        return publisherName;
    }

    public Publication getPublication () {

        return publication;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), systemId, publisherId, publisherName, publication);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof GlobalPublication) {
            if (!super.equals (object))
                return false;
            final GlobalPublication that = (GlobalPublication) object;
            return Objects.equal (systemId, that.systemId) && Objects.equal (publisherId, that.publisherId)
                    && Objects.equal (publisherName, that.publisherName)
                    && Objects.equal (publication, that.publication);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("systemId", systemId)
                .add ("publisherId", publisherId).add ("publisherName", publisherName).add ("publication", publication)
                .toString ();
    }

}
