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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Getter
public enum PluginEnum {

    GLOBAL(1, 0, "global"),

    SIGN(2, 0, "sign"),

    JWT(9, 0, "jwt"),

    OAUTH2(3, 0, "oauth2"),

    WAF(10, 0, "waf"),

    RATE_LIMITER(20, 0, "rate_limiter"),

    PARAM_MAPPING(22, 0, "param_mapping"),

    CONTEXT_PATH(25, 0, "context_path"),

    REWRITE(30, 0, "rewrite"),

    REDIRECT(40, 0, "redirect"),

    REQUEST(42, 0, "request"),

    MODIFY_RESPONSE(44, 0, "modifyResponse"),

    HYSTRIX(45, 0, "hystrix"),

    SENTINEL(45, 0, "sentinel"),

    RESILIENCE4J(45, 0, "resilience4j"),

    LOGGING(45, 0, "logging"),

    DIVIDE(50, 0, "divide"),

    SPRING_CLOUD(50, 0, "springCloud"),

    WEB_SOCKET(55, 0, "webSocket"),

    PARAM_TRANSFORM(58, 0, "paramTransform"),

    DUBBO(60, 0, "dubbo"),

    SOFA(60, 0, "sofa"),

    TARS(60, 0, "tars"),

    GRPC(60, 0, "grpc"),

    MOTAN(60, 0, "motan"),

    MONITOR(80, 0, "monitor"),

    RESPONSE(100, 0, "response");


    private final int code;

    private final int role;

    private final String name;

    public static PluginEnum getPluginEnumByName(final String name) {
        return Arrays.stream(PluginEnum.values())
                .filter(pluginEnum -> pluginEnum.getName().equals(name))
                .findFirst().orElse(PluginEnum.GLOBAL);
    }

    public static List<String> getUpstreamNames() {
        return Arrays.asList(DIVIDE.name, GRPC.name, TARS.name);
    }
}
