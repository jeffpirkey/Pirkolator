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
package com.rws.pirkolator.core.engine.channel;

import java.io.Serializable;
import java.util.Collection;

//TODO jpirkey - add publish with return ACK

/**
 * This interface describes a Pub/Sub channel that is used to publish
 * {@link Serializable} objects.  {@link IPubSubChannel} is passed to
 * each Publisher that is registered in the Hub.  The Publisher uses this
 * channel to publish objects.
 * 
 * @author jpirkey
 * @since 1.0.0
 */
public interface IPubSubChannel {

    /**
     * Publishes a single object. This will provide a copy of the object to each
     * subscriber.
     *
     * @param object
     */
    void publish (Serializable object);

    /**
     * Publishes a single object. The copy parameter determines if the object is
     * copied or passed by reference.
     *
     * @param object
     * @param copy
     */
    void publish (Serializable object, boolean copy);

    /**
     * Iterates the collection to publish each object.
     *
     * @param collection
     */
    void publishAll (Collection<? extends Serializable> collection);

    /**
     * This method automatically transforms the object to any registered transform
     * types in the Transformer.  This method first publishes the original object.
     * 
     * @param object
     * @param copy
     */
    void transformPublish (Serializable object, boolean copy);

    /**
     * This method automatically transforms the object to any registered transform
     * types in the Transformer.  This method optionally publishes the original object, first.
     * 
     * @param object
     * @param copy
     * @param publishOriginal
     */
    void transformPublish (Serializable object, boolean copy, boolean publishOriginal);
}
