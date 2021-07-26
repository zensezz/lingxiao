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

package cn.zensezz.lingxiao.plugin.divide;


import cn.zensezz.lingxiao.common.constants.Constants;
import cn.zensezz.lingxiao.common.dto.RuleData;
import cn.zensezz.lingxiao.common.dto.SelectorData;
import cn.zensezz.lingxiao.common.dto.convert.DivideUpstream;
import cn.zensezz.lingxiao.common.dto.convert.rule.impl.DivideRuleHandle;
import cn.zensezz.lingxiao.common.enums.PluginEnum;
import cn.zensezz.lingxiao.common.enums.RpcTypeEnum;
import cn.zensezz.lingxiao.plugin.base.util.WebFluxResultUtils;
import cn.zensezz.lingxiao.plugin.api.LingxiaoPluginChain;
import cn.zensezz.lingxiao.plugin.api.context.LingxiaoContext;
import cn.zensezz.lingxiao.plugin.api.result.LingxiaoResultEnum;
import cn.zensezz.lingxiao.plugin.api.result.LingxiaoResultWrap;
import cn.zensezz.lingxiao.plugin.base.AbstractLingxiaoPlugin;
import cn.zensezz.lingxiao.plugin.base.util.CacheKeyUtils;
import cn.zensezz.lingxiao.plugin.divide.balance.utils.LoadBalanceUtils;
import cn.zensezz.lingxiao.plugin.divide.cache.UpstreamCacheManager;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Slf4j
public class DividePlugin extends AbstractLingxiaoPlugin {

    @SneakyThrows
    @Override
    protected Mono<Void> doExecute(final ServerWebExchange exchange, final LingxiaoPluginChain chain, final SelectorData selector, final RuleData rule) {
        LingxiaoContext LingxiaoContext = exchange.getAttribute(Constants.CONTEXT);
        assert  LingxiaoContext != null;
        DivideRuleHandle ruleHandle = UpstreamCacheManager.getInstance().obtainHandle(CacheKeyUtils.INST.getKey(rule));
        long headerSize = 0;
        for (List<String> multiHeader : exchange.getRequest().getHeaders().values()) {
            for (String value : multiHeader) {
                headerSize += value.getBytes(StandardCharsets.UTF_8).length;
            }
        }
        if (headerSize > ruleHandle.getHeaderMaxSize()) {
            log.error("request header is too large");
            Object error =  LingxiaoResultWrap.error( LingxiaoResultEnum.REQUEST_HEADER_TOO_LARGE.getCode(),  LingxiaoResultEnum.REQUEST_HEADER_TOO_LARGE.getMsg(), null);
            return WebFluxResultUtils.result(exchange, error);
        }
        if (exchange.getRequest().getHeaders().getContentLength() > ruleHandle.getRequestMaxSize()) {
            log.error("request entity is too large");
            Object error =  LingxiaoResultWrap.error( LingxiaoResultEnum.REQUEST_ENTITY_TOO_LARGE.getCode(),  LingxiaoResultEnum.REQUEST_ENTITY_TOO_LARGE.getMsg(), null);
            return WebFluxResultUtils.result(exchange, error);
        }
        List<DivideUpstream> upstreamList = UpstreamCacheManager.getInstance().findUpstreamListBySelectorId(selector.getId());
        if (CollectionUtils.isEmpty(upstreamList)) {
            log.error("divide upstream configuration error： {}", rule);
            Object error =  LingxiaoResultWrap.error( LingxiaoResultEnum.CANNOT_FIND_URL.getCode(),  LingxiaoResultEnum.CANNOT_FIND_URL.getMsg(), null);
            return WebFluxResultUtils.result(exchange, error);
        }
        String ip = Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress();
        DivideUpstream divideUpstream = LoadBalanceUtils.selector(upstreamList, ruleHandle.getLoadBalance(), ip);
        if (Objects.isNull(divideUpstream)) {
            log.error("divide has no upstream");
            Object error =  LingxiaoResultWrap.error( LingxiaoResultEnum.CANNOT_FIND_URL.getCode(),  LingxiaoResultEnum.CANNOT_FIND_URL.getMsg(), null);
            return WebFluxResultUtils.result(exchange, error);
        }
        String domain = buildDomain(divideUpstream);
        String realURL = buildRealURL(domain,  LingxiaoContext, exchange);
        exchange.getAttributes().put(Constants.HTTP_URL, realURL);
        exchange.getAttributes().put(Constants.HTTP_TIME_OUT, ruleHandle.getTimeout());
        exchange.getAttributes().put(Constants.HTTP_RETRY, ruleHandle.getRetry());
        return chain.execute(exchange);
    }

    @Override
    public String named() {
        return PluginEnum.DIVIDE.getName();
    }

    @Override
    public Boolean skip(final ServerWebExchange exchange) {
        final  LingxiaoContext  LingxiaoContext = exchange.getAttribute(Constants.CONTEXT);
        return !Objects.equals(Objects.requireNonNull( LingxiaoContext).getRpcType(), RpcTypeEnum.HTTP.getName());
    }

    @Override
    public int getOrder() {
        return PluginEnum.DIVIDE.getCode();
    }

    @Override
    protected Mono<Void> handleSelectorIfNull(final String pluginName, final ServerWebExchange exchange, final  LingxiaoPluginChain chain) {
        return WebFluxResultUtils.noSelectorResult(pluginName, exchange);
    }

    @Override
    protected Mono<Void> handleRuleIfNull(final String pluginName, final ServerWebExchange exchange, final  LingxiaoPluginChain chain) {
        return WebFluxResultUtils.noRuleResult(pluginName, exchange);
    }

    private String buildDomain(final DivideUpstream divideUpstream) {
        String protocol = divideUpstream.getProtocol();
        if (StringUtils.isBlank(protocol)) {
            protocol = "http://";
        }
        return protocol + divideUpstream.getUpstreamUrl().trim();
    }

    private String buildRealURL(final String domain, final  LingxiaoContext  LingxiaoContext, final ServerWebExchange exchange) {
        String path = domain;
        final String rewriteURI = (String) exchange.getAttributes().get(Constants.REWRITE_URI);
        if (StringUtils.isNoneBlank(rewriteURI)) {
            path = path + rewriteURI;
        } else {
            final String realUrl =  LingxiaoContext.getRealUrl();
            if (StringUtils.isNoneBlank(realUrl)) {
                path = path + realUrl;
            }
        }
        String query = exchange.getRequest().getURI().getQuery();
        if (StringUtils.isNoneBlank(query)) {
            return path + "?" + query;
        }
        return path;
    }
}
