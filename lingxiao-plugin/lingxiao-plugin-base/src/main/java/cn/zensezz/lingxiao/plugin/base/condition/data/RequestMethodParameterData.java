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

package cn.zensezz.lingxiao.plugin.base.condition.data;

import cn.zensezz.lingxiao.spi.Join;
import org.springframework.web.server.ServerWebExchange;


@Join
public class RequestMethodParameterData implements ParameterData {
    
    @Override
    public String builder(final String paramName, final ServerWebExchange exchange) {
        return exchange.getRequest().getMethodValue();
    }
}
