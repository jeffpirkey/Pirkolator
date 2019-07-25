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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.rws.utility.common.Preconditions;

public class TransformLibrary implements ApplicationContextAware {

    /** spring **/
    @Nullable
    private ApplicationContext applicationContext;
    
    /** <p>Multimap of {@link ITransformFunction} instances.
     * <br/><b>Key = {@link Class} type</b> **/
    final Multimap<Class<?>, ITransformFunction<Object, Serializable>> viewTransformerMap = HashMultimap
            .<Class<?>, ITransformFunction<Object, Serializable>> create ();

    
    // *************************************************************************
    // ** Spring methods 
    // *************************************************************************

    @Override
    public void setApplicationContext (final @Nullable ApplicationContext arg0) throws BeansException {

        applicationContext = arg0;
    }
    
    Collection<Class<?>> getClassTypeCollection () {
        
        final Set<Class<?>> set = new HashSet<> ();
        for (final ITransformFunction<Object, Serializable> function : viewTransformerMap.values()) {
            set.add (function.getToType ());
        }
        
        return set;
    }
    
    // *************************************************************************
    // ** Life-cycle methods 
    // *************************************************************************

    @PostConstruct
    void postConstruct () {

        if (applicationContext != null) {

            // IPubSub
            for (final ITransformFunction<Object, Serializable> transformer : Preconditions.notNull (applicationContext).getBeansOfType (
                    ITransformFunction.class).values ()) {
                viewTransformerMap.put (transformer.getFromType (), transformer);
            }
        }
    }

    public Collection<ITransformFunction<Object, Serializable>> get (final Class<?> type) {
        return viewTransformerMap.get(type);
    }
}
