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

package cn.zensezz.lingxiao.client.core.disruptor;

import cn.zensezz.lingxiao.client.core.disruptor.executor.RegisterClientExecutorFactory;
import cn.zensezz.lingxiao.client.core.disruptor.subcriber.LingxiaoClientMetadataExecutorSubscriber;
import cn.zensezz.lingxiao.disruptor.DisruptorProviderManage;
import cn.zensezz.lingxiao.disruptor.provider.DisruptorProvider;
import cn.zensezz.lingxiao.register.client.api.LingxiaoClientRegisterRepository;

@SuppressWarnings("all")
public class LingxiaoClientRegisterEventPublisher {
    
    private static final LingxiaoClientRegisterEventPublisher INSTANCE = new LingxiaoClientRegisterEventPublisher();
    
    private DisruptorProviderManage providerManage;
    
    private RegisterClientExecutorFactory factory;

    public static LingxiaoClientRegisterEventPublisher getInstance() {
        return INSTANCE;
    }

    public void start(final LingxiaoClientRegisterRepository lingxiaoClientRegisterRepository) {
        factory = new RegisterClientExecutorFactory(
                new LingxiaoClientMetadataExecutorSubscriber(lingxiaoClientRegisterRepository));
        providerManage = new DisruptorProviderManage(factory);
        providerManage.startup();
    }

    public <T> void publishEvent(final T data) {
        DisruptorProvider<Object> provider = providerManage.getProvider();
        provider.onData(f -> f.setData(data));
    }
}
