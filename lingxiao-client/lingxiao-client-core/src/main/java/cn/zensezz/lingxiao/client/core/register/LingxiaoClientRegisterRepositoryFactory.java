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

package cn.zensezz.lingxiao.client.core.register;

import cn.zensezz.lingxiao.client.core.shutdown.LingxiaoClientShutdownHook;
import cn.zensezz.lingxiao.register.client.api.LingxiaoClientRegisterRepository;
import cn.zensezz.lingxiao.spi.ExtensionLoader;
import cn.zensezz.lingxiao.springboot.starter.client.common.config.LingxiaoRegisterCenterConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LingxiaoClientRegisterRepositoryFactory {
    
    private static final Map<String, LingxiaoClientRegisterRepository> REPOSITORY_MAP = new ConcurrentHashMap<>();
    
  
    public static LingxiaoClientRegisterRepository newInstance(final LingxiaoRegisterCenterConfig LingxiaoRegisterCenterConfig) {
        if (!REPOSITORY_MAP.containsKey(LingxiaoRegisterCenterConfig.getRegisterType())) {
            LingxiaoClientRegisterRepository result = ExtensionLoader.getExtensionLoader(LingxiaoClientRegisterRepository.class).getJoin(LingxiaoRegisterCenterConfig.getRegisterType());
            result.init(LingxiaoRegisterCenterConfig);
            LingxiaoClientShutdownHook.set(result, LingxiaoRegisterCenterConfig.getProps());
            REPOSITORY_MAP.put(LingxiaoRegisterCenterConfig.getRegisterType(), result);
            return result;
        }
        return REPOSITORY_MAP.get(LingxiaoRegisterCenterConfig.getRegisterType());
    }
}
