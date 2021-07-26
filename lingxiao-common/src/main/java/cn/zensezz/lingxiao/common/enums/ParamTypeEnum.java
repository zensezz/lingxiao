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

package cn.zensezz.lingxiao.common.enums;

import cn.zensezz.lingxiao.common.exception.LingxiaoException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum ParamTypeEnum {

    PATH("path", true),

    FORM_DATA("form-data", true),

    POST("post", true),

    URI("uri", true),

    QUERY("query", true),

    HOST("host", true),

    IP("ip", true),

    HEADER("header", true),

    COOKIE("cookie", true),

    REQUEST_METHOD("req_method", true);

    private final String name;

    private final Boolean support;

    public static List<ParamTypeEnum> acquireSupport() {
        return Arrays.stream(ParamTypeEnum.values())
                .filter(e -> e.support).collect(Collectors.toList());
    }

    public static ParamTypeEnum getParamTypeEnumByName(final String name) {
        return Arrays.stream(ParamTypeEnum.values())
                .filter(e -> e.getName().equals(name) && e.support).findFirst()
                .orElseThrow(() -> new  LingxiaoException(String.format(" this  param type can not support %s", name)));
    }
}
