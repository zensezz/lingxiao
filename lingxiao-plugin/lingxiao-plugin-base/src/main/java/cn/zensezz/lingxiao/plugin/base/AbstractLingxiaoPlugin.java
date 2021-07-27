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

import cn.zensezz.lingxiao.common.dto.PluginData;
import cn.zensezz.lingxiao.common.dto.RuleData;
import cn.zensezz.lingxiao.common.dto.SelectorData;
import cn.zensezz.lingxiao.common.enums.SelectorTypeEnum;
import cn.zensezz.lingxiao.plugin.api.LingxiaoPlugin;
import cn.zensezz.lingxiao.plugin.api.LingxiaoPluginChain;
import cn.zensezz.lingxiao.plugin.base.cache.BaseDataCache;
import cn.zensezz.lingxiao.plugin.base.condition.strategy.MatchStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Slf4j
public abstract class AbstractLingxiaoPlugin implements LingxiaoPlugin {
    
    protected abstract Mono<Void> doExecute(ServerWebExchange exchange, LingxiaoPluginChain chain, SelectorData selector, RuleData rule);
    
    @Override
    public Mono<Void> execute(final ServerWebExchange exchange, final LingxiaoPluginChain chain) {
        String pluginName = named();
        PluginData pluginData = BaseDataCache.getInstance().obtainPluginData(pluginName);
        if (pluginData != null && pluginData.getEnabled()) {
            final Collection<SelectorData> selectors = BaseDataCache.getInstance().obtainSelectorData(pluginName);
            if (CollectionUtils.isEmpty(selectors)) {
                return handleSelectorIfNull(pluginName, exchange, chain);
            }
            SelectorData selectorData = matchSelector(exchange, selectors);
            if (Objects.isNull(selectorData)) {
                return handleSelectorIfNull(pluginName, exchange, chain);
            }
            selectorLog(selectorData, pluginName);
            List<RuleData> rules = BaseDataCache.getInstance().obtainRuleData(selectorData.getId());
            if (CollectionUtils.isEmpty(rules)) {
                return handleRuleIfNull(pluginName, exchange, chain);
            }
            RuleData rule;
            if (selectorData.getType() == SelectorTypeEnum.FULL_FLOW.getCode()) {

                rule = rules.get(rules.size() - 1);
            } else {
                rule = matchRule(exchange, rules);
            }
            if (Objects.isNull(rule)) {
                return handleRuleIfNull(pluginName, exchange, chain);
            }
            ruleLog(rule, pluginName);
            return doExecute(exchange, chain, selectorData, rule);
        }
        return chain.execute(exchange);
    }

    protected Mono<Void> handleSelectorIfNull(final String pluginName, final ServerWebExchange exchange, final LingxiaoPluginChain chain) {
        return chain.execute(exchange);
    }

    protected Mono<Void> handleRuleIfNull(final String pluginName, final ServerWebExchange exchange, final LingxiaoPluginChain chain) {
        return chain.execute(exchange);
    }

    private SelectorData matchSelector(final ServerWebExchange exchange, final Collection<SelectorData> selectors) {
        return selectors.stream()
                .filter(selector -> selector.getEnabled() && filterSelector(selector, exchange))
                .findFirst().orElse(null);
    }

    private Boolean filterSelector(final SelectorData selector, final ServerWebExchange exchange) {
        if (selector.getType() == SelectorTypeEnum.CUSTOM_FLOW.getCode()) {
            if (CollectionUtils.isEmpty(selector.getConditionList())) {
                return false;
            }
            return MatchStrategyFactory.match(selector.getMatchMode(), selector.getConditionList(), exchange);
        }
        return true;
    }

    private RuleData matchRule(final ServerWebExchange exchange, final Collection<RuleData> rules) {
        return rules.stream().filter(rule -> filterRule(rule, exchange)).findFirst().orElse(null);
    }

    private Boolean filterRule(final RuleData ruleData, final ServerWebExchange exchange) {
        return ruleData.getEnabled() && MatchStrategyFactory.match(ruleData.getMatchMode(), ruleData.getConditionDataList(), exchange);
    }

    private void selectorLog(final SelectorData selectorData, final String pluginName) {
        if (selectorData.getLogged()) {
            log.info("{} selector success match , selector name :{}", pluginName, selectorData.getName());
        }
    }

    private void ruleLog(final RuleData ruleData, final String pluginName) {
        if (ruleData.getLoged()) {
            log.info("{} rule success match , rule name :{}", pluginName, ruleData.getName());
        }
    }
}
