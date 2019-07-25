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
package com.rws.pirkolator.core.web.controller;

import static com.rws.utility.common.Preconditions.notNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.rws.pirkolator.core.engine.AbstractSubscriber;
import com.rws.pirkolator.core.engine.LocalPubSub;
import com.rws.pirkolator.core.store.IStore;
import com.rws.pirkolator.core.web.controller.exception.NoContentException;
import com.rws.pirkolator.core.web.controller.exception.RESTEndpointNotEnabledException;
import com.rws.pirkolator.core.web.controller.exception.ResourceAlreadyExistsException;
import com.rws.pirkolator.schema.IIdentifiable;
import com.rws.utility.common.UUIDs;
import com.rws.utility.common.sort.Sort;
import com.rws.utility.common.sort.SortUtils;
import com.rws.utility.common.sort.ViewComparator;

/**
 * 
 * @author jpirkey
 * @since 0.1.0
 */
public abstract class AbstractSubscribingController<T extends IIdentifiable> extends AbstractSubscriber {

    final transient Logger LOG = notNull (LoggerFactory.getLogger (getClass ().getName ()));

    // *************************************************************************
    // ** Member variables
    // *************************************************************************

    @Nullable
    @Autowired
    private LocalPubSub localPubSub;

    private final Class<T> type;

    // *************************************************************************
    // ** Constructors
    // *************************************************************************

    public AbstractSubscribingController (final Class<T> type) {

        super (UUIDs.generateUUID ());

        this.type = type;
    }

    // *************************************************************************
    // ** Member properties
    // *************************************************************************

    public final Class<T> getType () {

        return type;
    }

    public abstract IStore<T> getStore ();

    public abstract boolean isGetListEnabled ();

    public abstract boolean isGetByIdEnabled ();

    public abstract boolean isPostEnabled ();

    public abstract boolean isPutEnabled ();

    public abstract boolean isDeleteEnabled ();

    // *************************************************************************
    // ** Member methods
    // *************************************************************************

    public void resync () {

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("No resync");
        }
    }

    @RequestMapping (method = RequestMethod.GET)
    @ResponseBody
    public final List<T> getList (@RequestParam final Map<String, Object> requestMap, @RequestHeader (value = "Range",
            defaultValue = "") final String items, final HttpServletResponse response) {

        if (isGetListEnabled ()) {
            if (LOG.isDebugEnabled ()) {
                LOG.debug ("Processing GET request, requestParams={}", requestMap);
            }

            return processParameterMap (requestMap, items, response);
        }

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("GET request not enabled");
        }

        return ImmutableList.of ();
    }

    public List<T> processParameterMap (final Map<String, Object> requestMap, final String items,
            final HttpServletResponse response) {

        final Map<String, Object> parameterMap = Maps.newConcurrentMap ();
        for (final Entry<String, Object> entry : requestMap.entrySet ()) {
            if ("sortBy".equals (entry.getKey ())) {
                continue;
            }
            parameterMap.put (entry.getKey (), entry.getValue ());
        }

        final Iterable<? extends T> it;
        if (parameterMap.isEmpty ()) {
            it = getStore ().findAll ();
        } else {
            it = getStore ().findByParameterMap (parameterMap);
        }

        final ArrayList<T> list = new ArrayList<> ();
        for (final T obj : it) {
            list.add(obj);
        }
        
        final List<T> sortedList = processSortBy (requestMap, list);
        if (items.isEmpty ()) {
            response.addHeader ("Content-Range", "items 0-" + sortedList.size () + "/" + sortedList.size ());
            return sortedList;
        }

        final String tmp = items.replace ("items=", "");
        final String[] split = tmp.split ("-");
        if (split.length != 2) {
            LOG.error ("Incorrect request header for paging items processing {}", items);
            response.addHeader ("Content-Range", "items 0-" + sortedList.size () + "/" + sortedList.size ());
            return sortedList;
        }

        final int startIndex = Integer.parseInt (split[0]);
        int endIndex = Integer.parseInt (split[1]);

        // Adjust for range past allowed size
        if (endIndex >= sortedList.size ()) {
            endIndex = sortedList.size () - 1;
        }

        final List<T> pagedList = new ArrayList<> ();
        for (int i = startIndex; i <= endIndex; i++) {
            pagedList.add (sortedList.get (i));
        }

        response.addHeader ("Content-Range", "items " + startIndex + "-" + endIndex + "/" + sortedList.size ());
        return pagedList;
    }

    public final List<T> processSortBy (final String sortBy, final List<T> viewList) {

        final List<Sort> sortList = SortUtils.getSortList (sortBy);

        if (!sortList.isEmpty ()) {
            final ViewComparator<T> viewComparator = new ViewComparator<> (sortList);
            Collections.sort (viewList, viewComparator);
        }

        return viewList;
    }

    public final List<T> processSortBy (final Map<String, Object> requestMap, final List<T> viewList) {

        final String sortBy = (String) requestMap.get ("sortBy");
        final List<Sort> sortList = SortUtils.getSortList (sortBy);

        if (!sortList.isEmpty ()) {
            final ViewComparator<T> viewComparator = new ViewComparator<> (sortList);
            Collections.sort (viewList, viewComparator);
        }

        return viewList;
    }

    @RequestMapping (value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    @Nullable
    public final T getById (@PathVariable ("id") final String id) {

        if (isGetByIdEnabled ()) {
            if (LOG.isDebugEnabled ()) {
                LOG.debug ("Processing GET by id={}", id);
            }

            final Optional<T> tmpOpt = getStore ().findById (id);
            if (tmpOpt.isPresent ()) {

                return tmpOpt.get ();
            }

            if (LOG.isDebugEnabled ()) {
                LOG.debug ("The requested resource {} with id [{}] was not found", type.getSimpleName (), id);
            }

            return null;
        }

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("GET by ID is not enabled for this controller {}", type.getSimpleName ());
        }

        throw new RESTEndpointNotEnabledException ("The requested endpoint for " + type.getSimpleName ()
                + " is not enabled");
    }

    @RequestMapping (value = "/resync", method = RequestMethod.GET)
    @ResponseBody
    public final void getResync () {

        resync ();
    }

    @RequestMapping (method = RequestMethod.POST, consumes = "application/json")
    @ResponseStatus (HttpStatus.CREATED)
    @ResponseBody
    public final T post (@RequestBody final T data) {

        if (isPostEnabled ()) {

            final T workingData = prePost (data);

            getStore ().put (workingData);

            if (LOG.isDebugEnabled ()) {
                LOG.debug ("Finished processing POST request for {}", workingData);
            }

            postPost (workingData);

            return workingData;
        }

        if (LOG.isDebugEnabled ()) {
            LOG.debug ("POST request not enabled.");
        }

        throw new RESTEndpointNotEnabledException ("The requested endpoint for " + type.getSimpleName ()
                + " is not enabled");
    }

    public T prePost (final T data) {

        if (LOG.isDebugEnabled ()) {
            LOG.info ("Pre-processing POST {}", data);
        }

        return data;
    }

    public void postPost (final T data) {

        if (LOG.isDebugEnabled ()) {
            LOG.info ("Post-processing POST {}", data);
        }
    }

    @RequestMapping (method = RequestMethod.PUT)
    @ResponseStatus (HttpStatus.OK)
    @ResponseBody
    public final T put (@RequestBody final T data) {

        if (isPutEnabled ()) {
            final T workingData = prePut (data);

            final String id = workingData.getId ();
            if (id.isEmpty ()) {
                // throw exception
                throw new IllegalArgumentException (getType ().getSimpleName () + " paramater has an undefined ID");
            }

            // Resource Already Exists?
            final Optional<T> existingViewOpt = getStore ().findById (id);
            if (existingViewOpt.isPresent ()) {

                // throw exception
                throw new ResourceAlreadyExistsException ("PUT of " + getType ().getSimpleName () + " with id=" + id
                        + " failed because it already exists");
            }

            getStore ().put (workingData);

            postPut (workingData);

            if (LOG.isDebugEnabled ()) {
                LOG.debug ("Finished processing PUT request for {}", workingData);
            }

            return workingData;
        }

        throw new RESTEndpointNotEnabledException ("The requested endpoint for " + type.getSimpleName ()
                + " is not enabled");
    }

    public T prePut (final T data) {

        if (LOG.isDebugEnabled ()) {
            LOG.info ("Pre-processing PUT {}", data);
        }

        return data;
    }

    public void postPut (final T data) {

        if (LOG.isDebugEnabled ()) {
            LOG.info ("Post-processed PUT {}", data);
        }
    }

    @RequestMapping (value = "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus (HttpStatus.OK)
    public final void deleteById (@PathVariable ("id") final String id) {

        if (isDeleteEnabled ()) {
            if (LOG.isDebugEnabled ()) {
                LOG.info ("Processing DELETE id={}", id);
            }

            if (id.isEmpty ()) {
                // throw exception
                throw new IllegalArgumentException ("Unable to delete " + getType ().getSimpleName ()
                        + " - ID is null or empty");
            }

            final Optional<T> view = getStore ().removeById (id);

            if (!view.isPresent ()) {
                throw new NoContentException (getType ().getSimpleName () + " with id=" + id
                        + " already deleted or didn't exist in store");
            }
        }
    }

    // *************************************************************************
    // ** Life-cycle methods
    // *************************************************************************

    @PostConstruct
    private void postConstruct () {

        LOG.info ("Initializing subscribing controller...");
        notNull (localPubSub).addSubscriber (this);
        getStore ().registerListener (this);
        resync ();
        LOG.info ("Initialized subscribing controller.");
    }
}
