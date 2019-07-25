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
package com.rws.utility.json;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;

/**
 * @author jpirkey
 * @since 0.1.0
 */
public class JsonUtils {

    private final static ObjectMapper jsonObjectMapper = new ObjectMapper();
    private final static ObjectMapper jsonJaxbMapper = new ObjectMapper();

    static {
        final AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
        final AnnotationIntrospector secondary = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance ());

        JsonUtils.jsonObjectMapper.getDeserializationConfig().with(primary);
        JsonUtils.jsonObjectMapper.getSerializationConfig().with(primary);

        final AnnotationIntrospector pair = new AnnotationIntrospectorPair(primary, secondary);
        JsonUtils.jsonJaxbMapper.getDeserializationConfig().with(pair);
        JsonUtils.jsonJaxbMapper.getSerializationConfig().with(pair);
    }

    public static <T> T generateObjectFromJsonFile(final File jsonFile, final Class<T> type) throws IOException {

        return JsonUtils.jsonObjectMapper.readValue(jsonFile, type);
    }

    public static <T> T generateObjectFromJson(final String json, final Class<T> type) throws IOException {

        return JsonUtils.jsonObjectMapper.readValue(json, type);
    }

    public static String generateJson(final Serializable object) throws IOException {

        return JsonUtils.jsonObjectMapper.writeValueAsString(object);
    }
    
    public static String generateJsonFromCollection(final Collection<? extends Serializable> object) throws IOException {

        return JsonUtils.jsonObjectMapper.writeValueAsString(object);
    }
    
    public static String generateJson(final Map<? extends Serializable, ? extends Serializable> map) throws IOException {

        return JsonUtils.jsonObjectMapper.writeValueAsString(map);
    }

    public static void generateJsonFile(final Serializable object, final File file) throws IOException {

        JsonUtils.jsonObjectMapper.writeValue(file, object);
    }

    public static <T> T generateObjectFromJsonJaxbFile(final File jsonFile, final Class<T> type) throws IOException {

        return JsonUtils.jsonJaxbMapper.readValue(jsonFile, type);
    }

    public static <T> T generateObjectFromJsonJaxb(final String json, final Class<T> type) throws IOException {

        return JsonUtils.jsonJaxbMapper.readValue(json, type);
    }

    public static String generateJsonJaxb(final Serializable object) throws IOException {

        return JsonUtils.jsonJaxbMapper.writeValueAsString(object);
    }

    public static void generateJsonJaxbFile(final Serializable object, final File file) throws IOException {

        JsonUtils.jsonJaxbMapper.writeValue(file, object);
    }

}
