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
public enum RpcTypeEnum {

    HTTP("http", true),

    DUBBO("dubbo", true),

    SOFA("sofa", true),

    TARS("tars", true),

    WEB_SOCKET("web_socket", true),

    SPRING_CLOUD("spring_cloud", true),

    MOTAN("motan", true),

    GRPC("grpc", true);


    private final String name;

    private final Boolean support;

    public static List<RpcTypeEnum> acquireSupports() {
        return Arrays.stream(RpcTypeEnum.values())
                .filter(e -> e.support).collect(Collectors.toList());
    }

    public static List<RpcTypeEnum> acquireSupportURIs() {
        return Arrays.asList(RpcTypeEnum.GRPC, RpcTypeEnum.HTTP, RpcTypeEnum.TARS);
    }

    public static List<RpcTypeEnum> acquireSupportMetadatas() {
        return Arrays.asList(RpcTypeEnum.DUBBO, RpcTypeEnum.GRPC, RpcTypeEnum.HTTP, RpcTypeEnum.SPRING_CLOUD, RpcTypeEnum.SOFA, RpcTypeEnum.TARS);
    }

    public static RpcTypeEnum acquireByName(final String name) {
        return Arrays.stream(RpcTypeEnum.values())
                .filter(e -> e.support && e.name.equals(name)).findFirst()
                .orElseThrow(() -> new LingxiaoException(String.format(" this rpc type can not support %s", name)));
    }
}
