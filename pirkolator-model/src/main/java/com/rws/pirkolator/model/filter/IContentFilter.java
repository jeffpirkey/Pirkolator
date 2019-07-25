package com.rws.pirkolator.model.filter;

import java.util.Set;

import com.rws.pirkolator.model.Message;

/**
 * A content filter is a specialized filter that delivers each instance of
 * the defined object type to a Subscriber annotation of a filter.
 * 
 * @author jpirkey
 *
 */
public interface IContentFilter extends IFilter {

    Set<Class<?>> getTypeSet();
    
    boolean supports (Message message);
    
    boolean supportsObject (Object obj, String[] propertyList);
}
