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

package cn.zensezz.lingxiao.common.constants;

public interface ZkPathConstants {

    public static final String SELECTOR_PARENT = "/lingxiao/selector";

    public static final String SELECTOR_JOIN_RULE = "-";

    public static final String PLUGIN_PARENT = "/lingxiao/plugin";

    public static final String RULE_PARENT = "/lingxiao/rule";

    public static final String APP_AUTH_PARENT = "/lingxiao/auth";

    public static String buildAppAuthPath(final String appKey){
        return String.join("/", APP_AUTH_PARENT, appKey);
    }

    public static String buildPluginPath(final String pluginName) {
        return String.join("/", PLUGIN_PARENT, pluginName);
    }

    public static String buildSelectorParentPath(final String pluginName) {
        return String.join("/", SELECTOR_PARENT, pluginName);
    }

    public static String buildSelectorRealPath(final String pluginName, final String selectorId) {
        return String.join("/", SELECTOR_PARENT, pluginName, selectorId);
    }

    public static String buildRuleParentPath(final String pluginName) {
        return String.join("/", RULE_PARENT, pluginName);
    }

    public static String buildRulePath(final String pluginName, final String selectorId, final String ruleId) {
        return String.join("/", buildRuleParentPath(pluginName), selectorId + SELECTOR_JOIN_RULE + ruleId);
    }

}
