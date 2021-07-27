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

package cn.zensezz.lingxiao.client.dubbo.client;


import cn.zensezz.lingxiao.client.core.disruptor.LingxiaoClientRegisterEventPublisher;
import cn.zensezz.lingxiao.client.dubbo.common.annotation.LingxiaoDubboClient;
import cn.zensezz.lingxiao.client.dubbo.common.dto.DubboRpcExt;
import cn.zensezz.lingxiao.common.util.GsonUtils;
import cn.zensezz.lingxiao.common.util.IpUtils;
import cn.zensezz.lingxiao.register.client.api.LingxiaoClientRegisterRepository;
import cn.zensezz.lingxiao.springboot.starter.client.common.config.LingxiaoRegisterCenterConfig;
import cn.zensezz.lingxiao.springboot.starter.client.common.dto.MetaDataRegisterDTO;
import com.alibaba.dubbo.common.Constants;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.spring.ServiceBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("all")
public class ApacheDubboServiceBeanListener implements ApplicationListener<ContextRefreshedEvent> {

    private LingxiaoClientRegisterEventPublisher lingxiaoClientRegisterEventPublisher = LingxiaoClientRegisterEventPublisher.getInstance();

    private final AtomicBoolean registered = new AtomicBoolean(false);

    private ExecutorService executorService;

    private String contextPath;

    private String appName;

    private final String host;

    private final String port;

    public ApacheDubboServiceBeanListener(final LingxiaoRegisterCenterConfig config, final LingxiaoClientRegisterRepository lingxiaoClientRegisterRepository) {
        Properties props = config.getProps();
        String contextPath = props.getProperty("contextPath");
        String appName = props.getProperty("appName");
        if (StringUtils.isEmpty(contextPath)) {
            throw new RuntimeException("apache dubbo client must config the contextPath");
        }
        this.contextPath = contextPath;
        this.appName = appName;
        this.host = props.getProperty("host");
        this.port = props.getProperty("port");
        executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("lingxiao-apache-dubbo-client-thread-pool-%d").build());
        lingxiaoClientRegisterEventPublisher.start(lingxiaoClientRegisterRepository);
    }

    private void handler(final ServiceBean serviceBean) {
        Object refProxy = serviceBean.getRef();
        Class<?> clazz = refProxy.getClass();
        if (AopUtils.isAopProxy(refProxy)) {
            clazz = AopUtils.getTargetClass(refProxy);
        }
        Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(clazz);
        for (Method method : methods) {
            LingxiaoDubboClient lingxiaoDubboClient = method.getAnnotation(LingxiaoDubboClient.class);
            if (Objects.nonNull(lingxiaoDubboClient)) {
                lingxiaoClientRegisterEventPublisher.publishEvent(buildMetaDataDTO(serviceBean, lingxiaoDubboClient, method));
            }
        }
    }

    private MetaDataRegisterDTO buildMetaDataDTO(final ServiceBean serviceBean, final LingxiaoDubboClient lingxiaoDubboClient, final Method method) {
        String appName = this.appName;
        if (StringUtils.isEmpty(appName)) {
            appName = serviceBean.getApplication().getName();
        }
        String path = contextPath + lingxiaoDubboClient.path();
        String desc = lingxiaoDubboClient.desc();
        String serviceName = serviceBean.getInterface();
        String host = IpUtils.isCompleteHost(this.host) ? this.host : IpUtils.getHost(this.host);
        int port = StringUtils.isBlank(this.port) ? -1 : Integer.parseInt(this.port);
        String configRuleName = lingxiaoDubboClient.ruleName();
        String ruleName = ("".equals(configRuleName)) ? path : configRuleName;
        String methodName = method.getName();
        Class<?>[] parameterTypesClazz = method.getParameterTypes();
        String parameterTypes = Arrays.stream(parameterTypesClazz).map(Class::getName).collect(Collectors.joining(","));
        return MetaDataRegisterDTO.builder()
                .appName(appName)
                .serviceName(serviceName)
                .methodName(methodName)
                .contextPath(contextPath)
                .host(host)
                .port(port)
                .path(path)
                .ruleName(ruleName)
                .pathDesc(desc)
                .parameterTypes(parameterTypes)
                .rpcExt(buildRpcExt(serviceBean))
                .rpcType("dubbo")
                .enabled(lingxiaoDubboClient.enabled())
                .build();
    }

    private String buildRpcExt(final ServiceBean serviceBean) {
        DubboRpcExt build = DubboRpcExt.builder()
                .group(StringUtils.isNotEmpty(serviceBean.getGroup()) ? serviceBean.getGroup() : "")
                .version(StringUtils.isNotEmpty(serviceBean.getVersion()) ? serviceBean.getVersion() : "")
                .loadbalance(StringUtils.isNotEmpty(serviceBean.getLoadbalance()) ? serviceBean.getLoadbalance() : Constants.DEFAULT_LOADBALANCE)
                .retries(Objects.isNull(serviceBean.getRetries()) ? Constants.DEFAULT_RETRIES : serviceBean.getRetries())
                .timeout(Objects.isNull(serviceBean.getTimeout()) ? Constants.DEFAULT_CONNECT_TIMEOUT : serviceBean.getTimeout())
                .url("")
                .build();
        return GsonUtils.getInstance().toJson(build);
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent contextRefreshedEvent) {
        if (!registered.compareAndSet(false, true)) {
            return;
        }
        Map<String, ServiceBean> serviceBean = contextRefreshedEvent.getApplicationContext().getBeansOfType(ServiceBean.class);
        for (Map.Entry<String, ServiceBean> entry : serviceBean.entrySet()) {
            executorService.execute(() -> handler(entry.getValue()));
        }
    }
}
