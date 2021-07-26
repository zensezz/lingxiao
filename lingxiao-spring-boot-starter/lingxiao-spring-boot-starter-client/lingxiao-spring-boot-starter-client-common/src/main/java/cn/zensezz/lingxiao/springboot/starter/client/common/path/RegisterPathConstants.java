/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.zensezz.lingxiao.springboot.starter.client.common.path;

/**
 *  zk 注册
 */
public class RegisterPathConstants {

    public static final String ROOT_PATH = "/lingxiao/register";


    private static final String SEPARATOR = "/";

    private static final String DOT_SEPARATOR = ".";

    public static String buildMetaDataContextPathParent(final String rpcType) {
        return String.join(SEPARATOR, ROOT_PATH, "metadata", rpcType);
    }
    public static String buildMetaDataParentPath(final String rpcType, final String contextPath) {
        return String.join(SEPARATOR, ROOT_PATH, "metadata", rpcType, contextPath);
    }
    

    public static String buildURIContextPathParent(final String rpcType) {
        return String.join(SEPARATOR, ROOT_PATH, "uri", rpcType);
    }

    public static String buildURIParentPath(final String rpcType, final String contextPath) {
        return String.join(SEPARATOR, ROOT_PATH, "uri", rpcType, contextPath);
    }

    public static String buildURIReadNode(final String rpcType, final String contextPath, final String nodeName) {
        return buildRealNode(buildURIParentPath(rpcType, contextPath), nodeName);
    }

    public static String buildMetaDataReadNode(final String rpcType, final String contextPath, final String nodeName) {
        return buildRealNode(buildMetaDataParentPath(rpcType, contextPath), nodeName);
    }

    public static String buildRealNode(final String nodePath, final String nodeName) {
        return String.join(SEPARATOR, nodePath, nodeName);
    }

    public static String buildServiceInstancePath(final String rpcType) {
        return String.join(SEPARATOR, ROOT_PATH, "service", rpcType)
                .replace("/", ".").substring(1);
    }
    public static String buildServiceConfigPath(final String rpcType, final String contextPath) {
        return String.join(SEPARATOR, ROOT_PATH, "service", rpcType, contextPath)
                .replace("/", ".").substring(1);
    }

    public static String buildNodeName(final String serviceName, final String methodName) {
        return String.join(DOT_SEPARATOR, serviceName, methodName);
    }
}
