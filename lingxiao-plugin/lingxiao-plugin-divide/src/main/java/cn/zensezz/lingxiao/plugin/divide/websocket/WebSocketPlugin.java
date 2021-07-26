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

package cn.zensezz.lingxiao.plugin.divide.websocket;

import cn.hutool.core.collection.CollUtil;
import cn.zensezz.lingxiao.common.constants.Constants;
import cn.zensezz.lingxiao.common.dto.RuleData;
import cn.zensezz.lingxiao.common.dto.SelectorData;
import cn.zensezz.lingxiao.common.dto.convert.DivideUpstream;
import cn.zensezz.lingxiao.common.dto.convert.rule.impl.DivideRuleHandle;
import cn.zensezz.lingxiao.common.enums.PluginEnum;
import cn.zensezz.lingxiao.common.enums.RpcTypeEnum;
import cn.zensezz.lingxiao.common.util.GsonUtils;
import cn.zensezz.lingxiao.plugin.base.util.WebFluxResultUtils;
import cn.zensezz.lingxiao.plugin.api.LingxiaoPluginChain;
import cn.zensezz.lingxiao.plugin.api.context.LingxiaoContext;
import cn.zensezz.lingxiao.plugin.api.result.LingxiaoResultEnum;
import cn.zensezz.lingxiao.plugin.api.result.LingxiaoResultWrap;
import cn.zensezz.lingxiao.plugin.base.AbstractLingxiaoPlugin;
import cn.zensezz.lingxiao.plugin.divide.balance.utils.LoadBalanceUtils;
import cn.zensezz.lingxiao.plugin.divide.cache.UpstreamCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The type Web socket plugin.
 */
@Slf4j
public class WebSocketPlugin extends AbstractLingxiaoPlugin {

    private static final String SEC_WEB_SOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

    private final WebSocketClient webSocketClient;

    private final WebSocketService webSocketService;

    /**
     * Instantiates a new Web socket plugin.
     *
     * @param webSocketClient  the web socket client
     * @param webSocketService the web socket service
     */
    public WebSocketPlugin(final WebSocketClient webSocketClient, final WebSocketService webSocketService) {
        this.webSocketClient = webSocketClient;
        this.webSocketService = webSocketService;
    }

    @Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, final LingxiaoPluginChain chain, final SelectorData selector, final RuleData rule) {
        final List<DivideUpstream> upstreamList = UpstreamCacheManager.getInstance().findUpstreamListBySelectorId(selector.getId());
        final LingxiaoContext lingxiaoContext = exchange.getAttribute(Constants.CONTEXT);
        if (CollectionUtils.isEmpty(upstreamList) || Objects.isNull(lingxiaoContext)) {
            log.error("divide upstream configuration error：{}", rule.toString());
            return chain.execute(exchange);
        }
        final DivideRuleHandle ruleHandle = GsonUtils.getInstance().fromJson(rule.getHandle(), DivideRuleHandle.class);
        final String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
        DivideUpstream divideUpstream = LoadBalanceUtils.selector(upstreamList, ruleHandle.getLoadBalance(), ip);
        if (Objects.isNull(divideUpstream)) {
            log.error("websocket has no upstream");
            Object error = LingxiaoResultWrap.error(LingxiaoResultEnum.CANNOT_FIND_URL.getCode(), LingxiaoResultEnum.CANNOT_FIND_URL.getMsg(), null);
            return WebFluxResultUtils.result(exchange, error);
        }
        URI wsRequestUrl = UriComponentsBuilder.fromUri(URI.create(buildWsRealPath(divideUpstream, lingxiaoContext))).build().toUri();
        log.info("you websocket urlPath is :{}", wsRequestUrl.toASCIIString());
        HttpHeaders headers = exchange.getRequest().getHeaders();
        return this.webSocketService.handleRequest(exchange, new LingxiaoWebSocketHandler(
                wsRequestUrl, this.webSocketClient, filterHeaders(headers), buildWsProtocols(headers)));
    }

    private String buildWsRealPath(final DivideUpstream divideUpstream, final LingxiaoContext lingxiaoContext) {
        String protocol = divideUpstream.getProtocol();
        if (StringUtils.isEmpty(protocol)) {
            protocol = "ws://";
        }
        return protocol + divideUpstream.getUpstreamUrl() + lingxiaoContext.getMethod();
    }

    private List<String> buildWsProtocols(final HttpHeaders headers) {
        List<String> protocols = headers.get(SEC_WEB_SOCKET_PROTOCOL);
        if (CollUtil.isNotEmpty(protocols)) {
            protocols = protocols
                    .stream().flatMap(header -> Arrays.stream(StringUtils.commaDelimitedListToStringArray(header)))
                    .map(String::trim).collect(Collectors.toList());
        }
        return protocols;
    }

    private HttpHeaders filterHeaders(final HttpHeaders headers) {
        HttpHeaders filtered = new HttpHeaders();
        headers.entrySet().stream()
                .filter(entry -> !entry.getKey().toLowerCase()
                        .startsWith("sec-websocket"))
                .forEach(header -> filtered.addAll(header.getKey(),
                        header.getValue()));
        return filtered;
    }

    @Override
    public String named() {
        return PluginEnum.DIVIDE.getName();
    }

    @Override
    public Boolean skip(final ServerWebExchange exchange) {
        final LingxiaoContext body = exchange.getAttribute(Constants.CONTEXT);
        return !Objects.equals(Objects.requireNonNull(body).getRpcType(), RpcTypeEnum.WEB_SOCKET.getName());
    }

    @Override
    public int getOrder() {
        return PluginEnum.WEB_SOCKET.getCode();
    }

    private static class LingxiaoWebSocketHandler implements WebSocketHandler {

        private final WebSocketClient client;

        private final URI url;

        private final HttpHeaders headers;

        private final List<String> subProtocols;

        LingxiaoWebSocketHandler(final URI url, final WebSocketClient client,
                               final HttpHeaders headers,
                               final List<String> protocols) {
            this.client = client;
            this.url = url;
            this.headers = headers;
            if (protocols != null) {
                this.subProtocols = protocols;
            } else {
                this.subProtocols = Collections.emptyList();
            }
        }

        @NonNull
        @Override
        public List<String> getSubProtocols() {
            return this.subProtocols;
        }

        @NonNull
        @Override
        public Mono<Void> handle(@NonNull final WebSocketSession session) {
            return client.execute(url, this.headers, new WebSocketHandler() {

                @NonNull
                @Override
                public Mono<Void> handle(@NonNull final WebSocketSession webSocketSession) {
                    // Use retain() for Reactor Netty
                    Mono<Void> sessionSend = webSocketSession
                            .send(session.receive().doOnNext(WebSocketMessage::retain));
                    Mono<Void> serverSessionSend = session.send(
                            webSocketSession.receive().doOnNext(WebSocketMessage::retain));
                    return Mono.zip(sessionSend, serverSessionSend).then();
                }

                @NonNull
                @Override
                public List<String> getSubProtocols() {
                    return LingxiaoWebSocketHandler.this.subProtocols;
                }
            });
        }
    }
}
