package com.rws.pirkolator.core.engine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface FilteredContent {

    Class<?> contentType ();
    
    String[] propertyArray () default {};
}
