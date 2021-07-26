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

import cn.hutool.core.util.StrUtil;
import cn.zensezz.lingxiao.client.core.disruptor.LingxiaoClientRegisterEventPublisher;
import cn.zensezz.lingxiao.client.core.disruptor.LingxiaoClientRegisterEventPublisher;
import cn.zensezz.lingxiao.common.util.IpUtils;
import cn.zensezz.lingxiao.register.client.api.LingxiaoClientRegisterRepository;
import cn.zensezz.lingxiao.springboot.starter.client.common.config.LingxiaoRegisterCenterConfig;
import cn.zensezz.lingxiao.springboot.starter.client.common.dto.MetaDataRegisterDTO;
import cn.znesezz.lingxiao.client.http.springmvc.annotation.LingxiaoSpringMvcClient;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SpringMvcClientBeanPostProcessor implements BeanPostProcessor {

    private final LingxiaoClientRegisterEventPublisher publisher = LingxiaoClientRegisterEventPublisher.getInstance();

    private final ExecutorService executorService;

    private final String contextPath;

    private final String appName;

    private final String host;

    private final Integer port;

    private final Boolean isFull;

    public SpringMvcClientBeanPostProcessor(final LingxiaoRegisterCenterConfig config, final LingxiaoClientRegisterRepository lingxiaoClientRegisterRepository) {
        String registerType = config.getRegisterType();
        String serverLists = config.getServerLists();
        Properties props = config.getProps();
        int port = Integer.parseInt(props.getProperty("port"));
        if (StrUtil.isBlank(registerType) || StrUtil.isBlank(serverLists) || port <= 0) {
            String errorMsg = "http register param must config the registerType , serverLists and port must > 0";
            log.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
        this.appName = props.getProperty("appName");
        this.host = props.getProperty("host");
        this.port = port;
        this.contextPath = props.getProperty("contextPath");
        this.isFull = Boolean.parseBoolean(props.getProperty("isFull", "false"));
        executorService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Lingxiao-spring-mvc-client-thread-pool-%d").build());
        publisher.start(lingxiaoClientRegisterRepository);
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull final Object bean, @NonNull final String beanName) throws BeansException {
        if (isFull) {
            return bean;
        }
        Controller controller = AnnotationUtils.findAnnotation(bean.getClass(), Controller.class);
        RequestMapping requestMapping = AnnotationUtils.findAnnotation(bean.getClass(), RequestMapping.class);
        if (controller != null || requestMapping != null) {
            LingxiaoSpringMvcClient clazzAnnotation = AnnotationUtils.findAnnotation(bean.getClass(), LingxiaoSpringMvcClient.class);
            String prePath = "";
            if (Objects.isNull(clazzAnnotation)) {
                return bean;
            }
            if (clazzAnnotation.path().indexOf("*") > 1) {
                String finalPrePath = prePath;
                executorService.execute(() -> publisher.publishEvent(buildMetaDataDTO(clazzAnnotation, finalPrePath)));
                return bean;
            }
            prePath = clazzAnnotation.path();
            final Method[] methods = ReflectionUtils.getUniqueDeclaredMethods(bean.getClass());
            for (Method method : methods) {
                LingxiaoSpringMvcClient LingxiaoSpringMvcClient = AnnotationUtils.findAnnotation(method, LingxiaoSpringMvcClient.class);
                if (Objects.nonNull(LingxiaoSpringMvcClient)) {
                    String finalPrePath = prePath;
                    executorService.execute(() -> publisher.publishEvent(buildMetaDataDTO(LingxiaoSpringMvcClient, finalPrePath)));
                }
            }
        }
        return bean;
    }

    private MetaDataRegisterDTO buildMetaDataDTO(final LingxiaoSpringMvcClient LingxiaoSpringMvcClient, final String prePath) {
        String contextPath = this.contextPath;
        String appName = this.appName;
        Integer port = this.port;
        String path;
        if (StringUtils.isEmpty(contextPath)) {
            path = prePath + LingxiaoSpringMvcClient.path();
        } else {
            path = contextPath + prePath + LingxiaoSpringMvcClient.path();
        }
        String desc = LingxiaoSpringMvcClient.desc();
        String host = IpUtils.isCompleteHost(this.host) ? this.host : IpUtils.getHost(this.host);
        String configRuleName = LingxiaoSpringMvcClient.ruleName();
        String ruleName = StrUtil.isBlank(configRuleName) ? path : configRuleName;
        return MetaDataRegisterDTO.builder()
                .contextPath(contextPath)
                .host(host)
                .port(port)
                .appName(appName)
                .path(path)
                .pathDesc(desc)
                .rpcType(LingxiaoSpringMvcClient.rpcType())
                .enabled(LingxiaoSpringMvcClient.enabled())
                .ruleName(ruleName)
                .registerMetaData(LingxiaoSpringMvcClient.registerMetaData())
                .build();
    }
}


