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

package cn.zensezz.lingxiao.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * this is http method support.
 * @author xiaoyu(Myth)
 */
@RequiredArgsConstructor
@Getter
public enum HttpMethodEnum {

    GET("get"),

    POST("post"),

    PUT("put"),

    DELETE("delete"),

    HEAD("head"),

    CONNECT("connect"),

    OPTIONS("options"),

    TRACE("trace"),

    ;


    private final String name;

    public static HttpMethodEnum convertByName(final String name) {
        return Arrays.stream(HttpMethodEnum.values())
                .filter(httpMethodEnum -> Objects.equals(httpMethodEnum.name, name))
                .findFirst().orElse(null);
    }

}