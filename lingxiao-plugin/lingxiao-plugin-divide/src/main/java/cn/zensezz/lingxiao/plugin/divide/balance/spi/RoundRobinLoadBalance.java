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

import cn.zensezz.lingxiao.common.dto.convert.DivideUpstream;
import cn.zensezz.lingxiao.spi.Join;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Join
public class RoundRobinLoadBalance extends cn.zensezz.lingxiao.plugin.divide.balance.spi.AbstractLoadBalance {

    private final int recyclePeriod = 60000;

    private final ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobin>> methodWeightMap = new ConcurrentHashMap<>(16);

    private final AtomicBoolean updateLock = new AtomicBoolean();

    @Override
    public DivideUpstream doSelect(final List<DivideUpstream> upstreamList, final String ip) {
        String key = upstreamList.get(0).getUpstreamUrl();
        ConcurrentMap<String, WeightedRoundRobin> map = methodWeightMap.get(key);
        if (map == null) {
            methodWeightMap.putIfAbsent(key, new ConcurrentHashMap<>(16));
            map = methodWeightMap.get(key);
        }
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        long now = System.currentTimeMillis();
        DivideUpstream selectedInvoker = null;
        WeightedRoundRobin selectedWRR = null;
        for (DivideUpstream upstream : upstreamList) {
            String rKey = upstream.getUpstreamUrl();
            WeightedRoundRobin weightedRoundRobin = map.get(rKey);
            int weight = getWeight(upstream);
            if (weightedRoundRobin == null) {
                weightedRoundRobin = new WeightedRoundRobin();
                weightedRoundRobin.setWeight(weight);
                map.putIfAbsent(rKey, weightedRoundRobin);
            }
            if (weight != weightedRoundRobin.getWeight()) {
                //weight changed
                weightedRoundRobin.setWeight(weight);
            }
            long cur = weightedRoundRobin.increaseCurrent();
            weightedRoundRobin.setLastUpdate(now);
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedInvoker = upstream;
                selectedWRR = weightedRoundRobin;
            }
            totalWeight += weight;
        }
        if (!updateLock.get() && upstreamList.size() != map.size() && updateLock.compareAndSet(false, true)) {
            try {
                // copy -> modify -> update reference
                ConcurrentMap<String, WeightedRoundRobin> newMap = new ConcurrentHashMap<>(map);
                newMap.entrySet().removeIf(item -> now - item.getValue().getLastUpdate() > recyclePeriod);
                methodWeightMap.put(key, newMap);
            } finally {
                updateLock.set(false);
            }
        }
        if (selectedInvoker != null) {
            selectedWRR.sel(totalWeight);
            return selectedInvoker;
        }
        // should not happen here
        return upstreamList.get(0);
    }

    protected static class WeightedRoundRobin {

        private int weight;

        private final AtomicLong current = new AtomicLong(0);

        private long lastUpdate;

        int getWeight() {
            return weight;
        }

        void setWeight(final int weight) {
            this.weight = weight;
            current.set(0);
        }

        long increaseCurrent() {
            return current.addAndGet(weight);
        }

        void sel(final int total) {
            current.addAndGet(-1 * total);
        }

        long getLastUpdate() {
            return lastUpdate;
        }

        void setLastUpdate(final long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }
}
