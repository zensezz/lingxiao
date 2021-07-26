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

package cn.zensezz.lingxiao.plugin.base.cache;

import cn.zensezz.lingxiao.common.dto.PluginData;
import cn.zensezz.lingxiao.common.dto.RuleData;
import cn.zensezz.lingxiao.common.dto.SelectorData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public final class BaseDataCache {

    private static final BaseDataCache INSTANCE = new BaseDataCache();

    private static final ConcurrentMap<String, PluginData> PLUGIN_MAP = Maps.newConcurrentMap();

    private static final ConcurrentMap<String, List<SelectorData>> SELECTOR_MAP = Maps.newConcurrentMap();

    private static final ConcurrentMap<String, List<RuleData>> RULE_MAP = Maps.newConcurrentMap();


    public static BaseDataCache getInstance() {
        return INSTANCE;
    }

    public void cachePluginData(final PluginData pluginData) {
        Optional.ofNullable(pluginData).ifPresent(data -> PLUGIN_MAP.put(data.getName(), data));
    }

    public void removePluginData(final PluginData pluginData) {
        Optional.ofNullable(pluginData).ifPresent(data -> PLUGIN_MAP.remove(data.getName()));
    }
    public void cleanPluginData() {
        PLUGIN_MAP.clear();
    }

    public void cleanPluginDataSelf(final List<PluginData> pluginDataList) {
        pluginDataList.forEach(this::removePluginData);
    }

    public PluginData obtainPluginData(final String pluginName) {
        return PLUGIN_MAP.get(pluginName);
    }

    public void cacheSelectData(final SelectorData selectorData) {
        Optional.ofNullable(selectorData).ifPresent(this::selectorAccept);
    }

    public void removeSelectData(final SelectorData selectorData) {
        Optional.ofNullable(selectorData).ifPresent(data -> {
            final List<SelectorData> selectorDataList = SELECTOR_MAP.get(data.getPluginName());
            Optional.ofNullable(selectorDataList).ifPresent(list -> list.removeIf(e -> e.getId().equals(data.getId())));
        });
    }

    public void cleanSelectorData() {
        SELECTOR_MAP.clear();
    }

    public void cleanSelectorDataSelf(final List<SelectorData> selectorDataList) {
        selectorDataList.forEach(this::removeSelectData);
    }

    public List<SelectorData> obtainSelectorData(final String pluginName) {
        return SELECTOR_MAP.get(pluginName);
    }

    public void cacheRuleData(final RuleData ruleData) {
        Optional.ofNullable(ruleData).ifPresent(this::ruleAccept);
    }

    public void removeRuleData(final RuleData ruleData) {
        Optional.ofNullable(ruleData).ifPresent(data -> {
            final List<RuleData> ruleDataList = RULE_MAP.get(data.getSelectorId());
            Optional.ofNullable(ruleDataList).ifPresent(list -> list.removeIf(rule -> rule.getId().equals(data.getId())));
        });
    }

    public void cleanRuleData() {
        RULE_MAP.clear();
    }

    public void cleanRuleDataSelf(final List<RuleData> ruleDataList) {
        ruleDataList.forEach(this::removeRuleData);
    }

    public List<RuleData> obtainRuleData(final String selectorId) {
        return RULE_MAP.get(selectorId);
    }

    private void ruleAccept(final RuleData data) {
        String selectorId = data.getSelectorId();
        synchronized (RULE_MAP) {
            if (RULE_MAP.containsKey(selectorId)) {
                List<RuleData> existList = RULE_MAP.get(selectorId);
                final List<RuleData> resultList = existList.stream().filter(r -> !r.getId().equals(data.getId())).collect(Collectors.toList());
                resultList.add(data);
                final List<RuleData> collect = resultList.stream().sorted(Comparator.comparing(RuleData::getSort)).collect(Collectors.toList());
                RULE_MAP.put(selectorId, collect);
            } else {
                RULE_MAP.put(selectorId, Lists.newArrayList(data));
            }
        }
    }

    private void selectorAccept(final SelectorData data) {
        String key = data.getPluginName();
        synchronized (SELECTOR_MAP) {
            if (SELECTOR_MAP.containsKey(key)) {
                List<SelectorData> existList = SELECTOR_MAP.get(key);
                final List<SelectorData> resultList = existList.stream().filter(r -> !r.getId().equals(data.getId())).collect(Collectors.toList());
                resultList.add(data);
                final List<SelectorData> collect = resultList.stream().sorted(Comparator.comparing(SelectorData::getSort)).collect(Collectors.toList());
                SELECTOR_MAP.put(key, collect);
            } else {
                SELECTOR_MAP.put(key, Lists.newArrayList(data));
            }
        }
    }
}
