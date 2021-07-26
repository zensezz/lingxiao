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

package cn.znesezz.lingxiao.client.http.springmvc.init;

import cn.zensezz.lingxiao.client.core.disruptor.LingxiaoClientRegisterEventPublisher;
import cn.zensezz.lingxiao.common.enums.RpcTypeEnum;
import cn.zensezz.lingxiao.common.util.IpUtils;
import cn.zensezz.lingxiao.register.client.api.LingxiaoClientRegisterRepository;
import cn.zensezz.lingxiao.springboot.starter.client.common.config.LingxiaoRegisterCenterConfig;
import cn.zensezz.lingxiao.springboot.starter.client.common.dto.MetaDataRegisterDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ContextRegisterListener implements ApplicationListener<ContextRefreshedEvent> {

    private LingxiaoClientRegisterEventPublisher publisher = LingxiaoClientRegisterEventPublisher.getInstance();

    private final AtomicBoolean registered = new AtomicBoolean(false);

    private String contextPath;

    private String appName;

    private String host;

    private Integer port;

    private final Boolean isFull;

    public ContextRegisterListener(final LingxiaoRegisterCenterConfig config, final LingxiaoClientRegisterRepository lingxiaoClientRegisterRepository) {
        Properties props = config.getProps();
        this.isFull = Boolean.parseBoolean(props.getProperty("isFull", "false"));
        if (isFull) {
            String registerType = config.getRegisterType();
            String serverLists = config.getServerLists();
            String contextPath = props.getProperty("contextPath");
            int port = Integer.parseInt(props.getProperty("port"));
            if (StringUtils.isBlank(contextPath) || StringUtils.isBlank(registerType)
                    || StringUtils.isBlank(serverLists) || port <= 0) {
                String errorMsg = "http register param must config the contextPath, registerType , serverLists and port must > 0";
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            this.appName = props.getProperty("appName");
            this.host = props.getProperty("host");
            this.port = port;
            this.contextPath = contextPath;
            publisher.start(lingxiaoClientRegisterRepository);
        }

    }

    @Override
    public void onApplicationEvent(@NonNull final ContextRefreshedEvent contextRefreshedEvent) {
        if (!registered.compareAndSet(false, true)) {
            return;
        }
        if (isFull) {
            publisher.publishEvent(buildMetaDataDTO());
        }
    }

    private MetaDataRegisterDTO buildMetaDataDTO() {
        String contextPath = this.contextPath;
        String appName = this.appName;
        Integer port = this.port;
        String path = contextPath + "/**";
        String host = IpUtils.isCompleteHost(this.host) ? this.host : IpUtils.getHost(this.host);
        return MetaDataRegisterDTO.builder()
                .contextPath(contextPath)
                .host(host)
                .port(port)
                .appName(appName)
                .path(path)
                .rpcType(RpcTypeEnum.HTTP.getName())
                .enabled(true)
                .ruleName(path)
                .build();
    }
}
