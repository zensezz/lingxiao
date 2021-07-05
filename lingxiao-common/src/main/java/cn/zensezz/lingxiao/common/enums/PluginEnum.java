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

@Getter
@RequiredArgsConstructor
public enum PluginEnum {

    GLOBAL(1, "global"),

    SIGN(2, "sign"),

    WAF(10, "waf"),

    RATE_LIMITER(20, "rate_limiter"),

    REWRITE(30, "rewrite"),

    REDIRECT(40, "redirect"),

    DIVIDE(50, "divide"),

    DUBBO(60, "dubbo"),

    MONITOR(70, "monitor");

    private final int code;

    private final String name;

}
