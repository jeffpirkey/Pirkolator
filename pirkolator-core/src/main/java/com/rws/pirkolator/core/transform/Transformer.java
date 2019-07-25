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
package com.rws.pirkolator.core.transform;

import static com.rws.utility.common.Preconditions.notNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.rws.pirkolator.core.engine.AbstractPublisher;
import com.rws.pirkolator.core.engine.Publication;
import com.rws.pirkolator.core.engine.SystemResourceManager;
import com.rws.pirkolator.model.filter.TypeFilter;
import com.rws.utility.common.Preconditions;
import com.rws.utility.common.UUIDs;

public class Transformer extends AbstractPublisher {

    @SuppressWarnings ("null")
    final static transient Logger LOG = LoggerFactory.getLogger (Transformer.class);

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    /** spring **/
    @Resource
    @Nullable
    TransformLibrary transformLibrary;

    @Resource
    @Nullable
    SystemResourceManager resourceManager;

    final Set<Class<?>> toTypeSet = Sets.newConcurrentHashSet ();

    final LinkedBlockingQueue<TransformTask> transformQueue = new LinkedBlockingQueue<> ();

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public Transformer () {

        super (UUIDs.generateUUID (), "Transforming Publisher");
    }

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void transformAndPublish (final Serializable object, final boolean copy) {

        try {
            transformQueue.put (new TransformTask (object, copy));
        } catch (final InterruptedException ex) {
            LOG.info ("TransformTask put interrupted");
        }
    }

    public <T> Collection<T> transform (final Serializable object, final Class<T> returnType) {

        final Collection<T> list = new ArrayList<> ();

        final Collection<ITransformFunction<Object, Serializable>> transformList =

        getTransformLibrary ().get (notNull (object.getClass ()));
        if (!transformList.isEmpty ()) {
            for (final ITransformFunction<Object, Serializable> transform : transformList) {
                final T tmp = returnType.cast (transform.apply (object));
                list.add (tmp);
            }
        }

        return list;
    }

    SystemResourceManager getResourceManager () {

        return Preconditions.notNull (resourceManager, "The ResourceManager in Transformer has not been defined."
                + " Check the Spring configuration to ensure that the SystemResourceManager is defined properly.");
    }

    TransformLibrary getTransformLibrary () {

        return Preconditions.notNull (transformLibrary, "The TransformLibrary in Transformer has not been defined."
                + " Check the Spring configuration to ensure that the SystemResourceManager is defined properly.");
    }

    // *************************************************************************
    // ** Life-cycle methods 
    // *************************************************************************

    @PostConstruct
    void postConstruct () {

        final ExecutorService executioner = getResourceManager ().getFixedThreadExecutor ("transformation-runner", 4);

        // Start 4 transformation tasks
        for (int i = 0; i < 4; i++) {
            executioner.execute (new TransformRunner ());
        }
    }

    @Override
    public void doConstruct (final Publication pub) {

        pub.setLabel ("Transformation Publication");
        pub.setDescription ("Transformation filters");

        toTypeSet.addAll (getTransformLibrary ().getClassTypeCollection ());

        for (final Class<?> type : toTypeSet) {
            pub.addFilter (new TypeFilter (type));
        }
    }

    // *************************************************************************
    // ** Member classes 
    // *************************************************************************

    class TransformTask {

        Serializable fromObj;
        boolean copy;

        public TransformTask (final Serializable from, final boolean copy) {

            super ();

            fromObj = from;
            this.copy = copy;
        }
    }

    class TransformRunner implements Runnable {

        AtomicBoolean running = new AtomicBoolean (false);

        public TransformRunner () {

            super ();
        }

        @Override
        public void run () {

            running.set (true);

            while (running.get ()) {

                TransformTask task = null;
                try {
                    task = transformQueue.take ();
                } catch (final InterruptedException ex) {
                    LOG.info ("TransformTask interrupted, terminating.");
                    running.set (false);
                }

                if (task != null) {
                    final Collection<ITransformFunction<Object, Serializable>> transformList =
                            getTransformLibrary ().get (notNull (task.fromObj.getClass ()));
                    if (transformList.isEmpty ()) {
                        Transformer.this.publish (task.fromObj, task.copy);
                    } else {
                        for (final ITransformFunction<Object, Serializable> transform : transformList) {
                            Transformer.this.publish (transform.apply (task.fromObj), task.copy);
                        }
                    }
                } else {
                    throw new IllegalArgumentException ("TransformTask is undefined");
                }
            }
        }
    }

}
