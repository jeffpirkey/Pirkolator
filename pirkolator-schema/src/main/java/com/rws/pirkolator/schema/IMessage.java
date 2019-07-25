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
package com.rws.pirkolator.schema;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * This interface describes the minimum set of functionality used to pass
 * messages through the framework.
 * 
 * @author pirk
 * @since 1.0.0
 */
public interface IMessage extends Serializable {

    /**
     * This is the Id of this message.  If this message is a copy, then the
     * original Id will contain the UUID of the original message.
     * 
     * @return
     */
    UUID getId ();

    /**
     * This is the original Id of a message.  This will be the same as the Id, if
     * this message is the original message and not a copy.
     * 
     * @return
     */
    UUID getOriginalId ();

    /**
     * 
     * @param name
     * @return {@link String} value of the given name or null if the name does not exist
     */
    @Nullable
    String getHeader (String name);

    Map<String, String> getHeaderMap ();

    /**
     * Provides a {@link List} of the objects contained in the message.
     * 
     * @return
     */
    List<Serializable> get ();

    /** 
     * Provides an {@link Iterator} used to traverse the objects in the message.
     * 
     * @return
     */
    Iterator<Serializable> iterate ();

    boolean containsType (Class<?> type);

    Set<Class<?>> getTypes ();

    boolean isEmpty ();

    /**
     * Provides an {@link Iterator} used to traverse the objects of the defined type
     * in the message.
     * 
     * @param type
     * @return
     */
    <T> Iterator<T> iterate (Class<T> type);
}
