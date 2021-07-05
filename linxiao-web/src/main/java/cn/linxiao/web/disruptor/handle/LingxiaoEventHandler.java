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

package cn.linxiao.web.disruptor.handle;

import cn.linxiao.web.disruptor.event.LingxiaoDataEvent;
import cn.linxiao.web.service.InfluxDbService;
import com.lmax.disruptor.EventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LingxiaoEventHandler implements EventHandler<LingxiaoDataEvent> {

    private final InfluxDbService influxDbService;

    @Autowired
    public LingxiaoEventHandler(final InfluxDbService influxDbService) {
        this.influxDbService = influxDbService;
    }

    @Override
    public void onEvent(final LingxiaoDataEvent lingxiaolDataEvent, final long sequence, final boolean endOfBatch) {
        influxDbService.writeData(lingxiaolDataEvent.getMonitorDO());
        lingxiaolDataEvent.clear();
    }
}
