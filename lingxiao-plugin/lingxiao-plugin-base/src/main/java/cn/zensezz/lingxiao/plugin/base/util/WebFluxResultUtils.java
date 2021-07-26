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

package cn.zensezz.lingxiao.plugin.base.util;

import cn.zensezz.lingxiao.common.util.JSONUtils;
import cn.zensezz.lingxiao.plugin.api.result.LingxiaoResultEnum;
import cn.zensezz.lingxiao.plugin.api.result.LingxiaoResultWrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Slf4j
public final class WebFluxResultUtils {

    public static Mono<Void> result(final ServerWebExchange exchange, final Object result) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(Objects.requireNonNull(JSONUtils.toJson(result)).getBytes())));
    }

    public static Mono<Void> noSelectorResult(final String pluginName, final ServerWebExchange exchange) {
        log.error("can not match selector data: {}", pluginName);
        Object error = LingxiaoResultWrap.error(LingxiaoResultEnum.SELECTOR_NOT_FOUND.getCode(), LingxiaoResultEnum.SELECTOR_NOT_FOUND.getMsg(), null);
        return WebFluxResultUtils.result(exchange, error);
    }

    public static Mono<Void> noRuleResult(final String pluginName, final ServerWebExchange exchange) {
        log.error("can not match rule data: {}", pluginName);
        Object error = LingxiaoResultWrap.error(LingxiaoResultEnum.RULE_NOT_FOUND.getCode(), LingxiaoResultEnum.RULE_NOT_FOUND.getMsg(), null);
        return WebFluxResultUtils.result(exchange, error);
    }
}
