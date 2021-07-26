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

package cn.zensezz.lingxiao.plugin.divide.balance.spi;

import cn.zensezz.lingxiao.common.constants.Constants;
import cn.zensezz.lingxiao.common.dto.convert.DivideUpstream;
import cn.zensezz.lingxiao.plugin.divide.balance.LoadBalance;
import cn.zensezz.lingxiao.plugin.divide.cache.balance.LoadBalance;
import org.springframework.util.CollectionUtils;

import java.util.List;


public abstract class AbstractLoadBalance implements LoadBalance {

    protected abstract DivideUpstream doSelect(List<DivideUpstream> upstreamList, String ip);

    @Override
    public DivideUpstream select(final List<DivideUpstream> upstreamList, final String ip) {
        if (CollectionUtils.isEmpty(upstreamList)) {
            return null;
        }
        if (upstreamList.size() == 1) {
            return upstreamList.get(0);
        }
        return doSelect(upstreamList, ip);
    }

    protected int getWeight(final DivideUpstream upstream) {
        if (!upstream.isStatus()) {
            return 0;
        }
        return getWeight(upstream.getTimestamp(), getWarmup(upstream.getWarmup(), Constants.DEFAULT_WARMUP), upstream.getWeight());
    }

    private int getWeight(final long timestamp, final int warmup, final int weight) {
        if (weight > 0 && timestamp > 0) {
            int uptime = (int) (System.currentTimeMillis() - timestamp);
            if (uptime > 0 && uptime < warmup) {
                return calculateWarmupWeight(uptime, warmup, weight);
            }
        }
        return weight;
    }

    private int getWarmup(final int warmup, final int defaultWarmup) {
        if (warmup > 0) {
            return warmup;
        }
        return defaultWarmup;
    }

    private int calculateWarmupWeight(final int uptime, final int warmup, final int weight) {
        int ww = (int) ((float) uptime / ((float) warmup / (float) weight));
        return ww < 1 ? 1 : (Math.min(ww, weight));
    }

}
