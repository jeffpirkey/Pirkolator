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

import com.rws.pirkolator.core.engine.MessageDistributor;
import com.rws.pirkolator.core.transform.Transformer;
import com.rws.pirkolator.model.Message;

/**
 * This final class provides an asynchronous implementation for an
 * {@link IPubSubChannel}.
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public final class AsyncPubSubChannel implements IPubSubChannel {

    private final MessageDistributor distributor;
    private final Transformer transformer;

    public AsyncPubSubChannel (final MessageDistributor distributor, final Transformer transformer)
            throws IllegalArgumentException {

        this.distributor = distributor;
        this.transformer = transformer;
    }

    @Override
    public void publish (final Serializable object) {

        publish (object, true);
    }

    @Override
    public void publish (final Serializable object, final boolean copy) {

        final Message message = new Message ();
        message.add (object);
        distributor.publish (message, copy);
    }

    @Override
    public void publishAll (final Collection<? extends Serializable> objects) {

        final Message message = new Message ();
        message.add (objects);
        distributor.publish (message);
    }

    @Override
    public void transformPublish (final Serializable object, final boolean copy) {

        transformPublish (object, copy, true);
    }

    @Override
    public void transformPublish (final Serializable object, final boolean copy, final boolean publishOriginal) {

        if (publishOriginal) {
            publish (object, copy);
        }

        transformer.transformAndPublish (object, copy);
    }
}
