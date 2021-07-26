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

package cn.zensezz.lingxiao.plugin.base;

import cn.zensezz.lingxiao.common.constants.Constants;
import cn.zensezz.lingxiao.common.enums.PluginEnum;
import cn.zensezz.lingxiao.common.enums.RpcTypeEnum;
import cn.zensezz.lingxiao.common.util.HttpParamConverterUtil;
import cn.zensezz.lingxiao.plugin.api.LingxiaoPlugin;
import cn.zensezz.lingxiao.plugin.api.LingxiaoPluginChain;
import cn.zensezz.lingxiao.plugin.api.context.LingxiaoContext;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.List;
import java.util.Objects;

/**
 * The param transform plugin.
 */
public class ParamTransformPlugin implements LingxiaoPlugin {

    private final List<HttpMessageReader<?>> messageReaders;

    /**
     * Instantiates a new param transform plugin.
     */
    public ParamTransformPlugin() {
        this.messageReaders = HandlerStrategies.withDefaults().messageReaders();
    }

    @Override
    public Mono<Void> execute(final ServerWebExchange exchange, final LingxiaoPluginChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        LingxiaoContext LingxiaoContext = exchange.getAttribute(Constants.CONTEXT);
        if (Objects.nonNull(LingxiaoContext)) {
            MediaType mediaType = request.getHeaders().getContentType();
            ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
            if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                return body(exchange, serverRequest, chain);
            }
            if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
                return formData(exchange, serverRequest, chain);
            }
            return query(exchange, serverRequest, chain);
        }
        return chain.execute(exchange);
    }

    @Override
    public int getOrder() {
        return PluginEnum.PARAM_TRANSFORM.getCode();
    }

    @Override
    public String named() {
        return PluginEnum.PARAM_TRANSFORM.getName();
    }

    private Mono<Void> body(final ServerWebExchange exchange, final ServerRequest serverRequest, final LingxiaoPluginChain chain) {
        return serverRequest.bodyToMono(String.class)
                .switchIfEmpty(Mono.defer(() -> Mono.just("")))
                .flatMap(body -> {
                    exchange.getAttributes().put(Constants.PARAM_TRANSFORM, body);
                    return chain.execute(exchange);
                });
    }
    
    private Mono<Void> formData(final ServerWebExchange exchange, final ServerRequest serverRequest, final LingxiaoPluginChain chain) {
        return serverRequest.formData()
                .switchIfEmpty(Mono.defer(() -> Mono.just(new LinkedMultiValueMap<>())))
                .flatMap(map -> {
                    exchange.getAttributes().put(Constants.PARAM_TRANSFORM, HttpParamConverterUtil.toMap(() -> map));
                    return chain.execute(exchange);
                });
    }
    
    private Mono<Void> query(final ServerWebExchange exchange, final ServerRequest serverRequest, final LingxiaoPluginChain chain) {
        exchange.getAttributes().put(Constants.PARAM_TRANSFORM, HttpParamConverterUtil.ofString(() -> serverRequest.uri().getQuery()));
        return chain.execute(exchange);
    }
    
    @Override
    public Boolean skip(final ServerWebExchange exchange) {
        LingxiaoContext LingxiaoContext = exchange.getAttribute(Constants.CONTEXT);
        assert LingxiaoContext != null;
        String rpcType = LingxiaoContext.getRpcType();
        return !Objects.equals(rpcType, RpcTypeEnum.DUBBO.getName()) 
                && !Objects.equals(rpcType, RpcTypeEnum.GRPC.getName())
                && !Objects.equals(rpcType, RpcTypeEnum.TARS.getName())
                && !Objects.equals(rpcType, RpcTypeEnum.MOTAN.getName())
                && !Objects.equals(rpcType, RpcTypeEnum.SOFA.getName());
    }
}
