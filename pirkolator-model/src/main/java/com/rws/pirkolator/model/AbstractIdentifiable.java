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
package com.rws.pirkolator.model;

import javax.annotation.Nullable;

import org.springframework.data.annotation.Id;

import com.google.common.base.Objects;
import com.rws.pirkolator.schema.IIdentifiable;

/**
 * This abstraction provides basic implementations for the {@link IIdentifiable}
 * interface.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public abstract class AbstractIdentifiable implements IIdentifiable {

    private static final long serialVersionUID = 1L;

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** define **/
    @Id
    private final String id;

    private final Metadata metadata;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public AbstractIdentifiable (final String id) {

        super ();

        this.id = id;
        metadata = new Metadata ();
    }

    public AbstractIdentifiable (final String id, final Metadata metadata) {

        super ();

        this.id = id;
        this.metadata = metadata;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    @Override
    public String getId () {

        return id;
    }

    @Override
    public Metadata getMetadata () {

        return metadata;
    }

    // *************************************************************************
    // ** Class methods
    // *************************************************************************

    @Override
    public int hashCode () {

        return Objects.hashCode (super.hashCode (), id, metadata);
    }

    @Override
    public boolean equals (final @Nullable Object object) {

        if (object instanceof AbstractIdentifiable) {
            if (!super.equals (object))
                return false;
            final AbstractIdentifiable that = (AbstractIdentifiable) object;
            return Objects.equal (id, that.id) && Objects.equal (metadata, that.metadata);
        }
        return false;
    }

    @Override
    public String toString () {

        return Objects.toStringHelper (this).add ("super", super.toString ()).add ("id", id).add ("metadata", metadata)
                .toString ();
    }
}
