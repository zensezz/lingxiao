/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.zensezz.lingxiao.common.util;


import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Slf4j
public final class JSONUtils {

    static ObjectMapper mapper = new ObjectMapper();
    
    public static byte[] getJsonBytes(Object o) {
        try {
            return mapper.writerFor(o.getClass()).writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getJsonString(Object instance) {
        try {
            return mapper.writerFor(instance.getClass()).writeValueAsString(instance);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T readJsonString(String jsonStr, Class<T> objClass) {
        try {
            return mapper.readerFor(objClass).readValue(jsonStr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        //SimpleModule byteModule = new SimpleModule();
        //byteModule.addSerializer(byte[].class, new ByteSerializer());
        //byteModule.addDeserializer(byte[].class, new ByteDeserializer());
        //mapper.registerModule(byteModule);
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        mapper.registerModule(javaTimeModule);
    }
    
    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            log.warn("write to json string error:" + object, e);
            return null;
        }
    }
    
    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StrUtil.isEmpty(jsonString)) {
            return null;
        }
        try {
            return mapper.readValue(jsonString, clazz);
        } catch (IOException e) {
            log.warn("parse json string error:" + jsonString, e);
            return null;
        }
    }

    public static <T> T fromJson(String jsonString, TypeReference<T> jsonTypeReference) {
        if (StrUtil.isEmpty(jsonString)) {
            return null;
        }
        try {
            return mapper.readValue(jsonString, jsonTypeReference);
        } catch (IOException e) {
            log.warn("parse json string error:" + jsonString, e);
            return null;
        }
    }

    public static Map<String, Object> fromJson(String jsonString) {
        JavaType map = buildMapType(HashMap.class, String.class, Object.class);
        return fromJson(jsonString, map);
    }

    public static Map<String, String> mapStrFromJson(String jsonString) {
        JavaType map = buildMapType(HashMap.class, String.class, String.class);
        return fromJson(jsonString, map);
    }

    public static TypeReference<Map<String, Object>> newMapTypeReference() {
        return new TypeReference<Map<String, Object>>() {
        };
    }
    
    public static <T> T fromJson(String jsonString, JavaType javaType) {
        if (StrUtil.isEmpty(jsonString)) {
            return null;
        }
        try {
            return (T) mapper.readValue(jsonString, javaType);
        } catch (IOException e) {
            log.warn("parse json string error:" + jsonString, e);
            return null;
        }
    }
    
    public static JavaType buildCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass) {
        return mapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
    }

    
    public static JavaType buildMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
        return mapper.getTypeFactory().constructMapType(mapClass, keyClass, valueClass);
    }

  
    public static String toJsonP(String functionName, Object object) {
        return toJson(new JSONPObject(functionName, object));
    }
    
    public void enableEnumUseToString() {
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
    }

}
