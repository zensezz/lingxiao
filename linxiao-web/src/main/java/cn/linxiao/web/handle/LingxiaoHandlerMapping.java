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

package cn.linxiao.web.handle;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.handler.AbstractHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public final class LingxiaoHandlerMapping extends AbstractHandlerMapping {

    private final LingxiaoWebHandler lingxiaoWebHandler;

    public LingxiaoHandlerMapping(final LingxiaoWebHandler lingxiaoWebHandler) {
        this.lingxiaoWebHandler = lingxiaoWebHandler;
        setOrder(1);
    }

    @Override
    protected Mono<?> getHandlerInternal(final ServerWebExchange exchange) {
        return Mono.just(lingxiaoWebHandler);
    }

    @Override
    protected CorsConfiguration getCorsConfiguration(final Object handler, final ServerWebExchange exchange) {
        return super.getCorsConfiguration(handler, exchange);
    }

}
