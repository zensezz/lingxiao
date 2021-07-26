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

package cn.zensezz.lingxiao.plugin.divide.handler;


import cn.zensezz.lingxiao.common.dto.RuleData;
import cn.zensezz.lingxiao.common.dto.SelectorData;
import cn.zensezz.lingxiao.common.dto.convert.rule.impl.DivideRuleHandle;
import cn.zensezz.lingxiao.common.enums.PluginEnum;
import cn.zensezz.lingxiao.common.util.GsonUtils;
import cn.zensezz.lingxiao.plugin.base.handle.PluginDataHandler;
import cn.zensezz.lingxiao.plugin.base.util.CacheKeyUtils;
import cn.zensezz.lingxiao.plugin.divide.cache.UpstreamCacheManager;

import java.util.Optional;

/**
 * The type Divide plugin data handler.
 */
public class DividePluginDataHandler implements PluginDataHandler {

    @Override
    public void handlerSelector(final SelectorData selectorData) {
        UpstreamCacheManager.getInstance().submit(selectorData);
    }

    @Override
    public void removeSelector(final SelectorData selectorData) {
        UpstreamCacheManager.getInstance().removeByKey(selectorData.getId());
    }

    @Override
    public void handlerRule(final RuleData ruleData) {
        Optional.ofNullable(ruleData.getHandle()).ifPresent(s -> {
            DivideRuleHandle divideRuleHandle = GsonUtils.getInstance().fromJson(s, DivideRuleHandle.class);
            UpstreamCacheManager.getInstance().cachedHandle(CacheKeyUtils.INST.getKey(ruleData), divideRuleHandle);
        });
    }

    @Override
    public void removeRule(final RuleData ruleData) {
        Optional.ofNullable(ruleData.getHandle()).ifPresent(s -> UpstreamCacheManager.getInstance().removeHandle(CacheKeyUtils.INST.getKey(ruleData)));
    }

    @Override
    public String pluginNamed() {
        return PluginEnum.DIVIDE.getName();
    }
}
