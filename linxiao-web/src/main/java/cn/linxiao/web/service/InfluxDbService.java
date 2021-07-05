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

package cn.linxiao.web.service;

import cn.linxiao.web.entity.dos.MonitorDo;
import org.influxdb.dto.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.influxdb.InfluxDBTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@SuppressWarnings("unchecked")
public class InfluxDbService {

    private final InfluxDBTemplate influxDBTemplate;

    @Autowired
    public InfluxDbService(final InfluxDBTemplate influxDBTemplate) {
        this.influxDBTemplate = influxDBTemplate;
    }

    public void writeData(final MonitorDo monitorDO) {
        final Point.Builder builder = Point.measurement("monitorDO")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        builder.tag("host", monitorDO.getHost())
                .tag("ip", monitorDO.getIp())
                .tag("method", monitorDO.getMethod())
                .tag("module", monitorDO.getModule())
                .tag("resultType", monitorDO.getResultType())
                .tag("rpcType", monitorDO.getRpcType())
                .addField("count", monitorDO.getCount());
        final Point point = builder.build();
        influxDBTemplate.write(point);
    }
}
