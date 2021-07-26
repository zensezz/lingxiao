package cn.zensezz.lingxiao.client.core.disruptor.subcriber;/*
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


import cn.zensezz.lingxiao.client.core.shutdown.LingxiaoClientShutdownHook;
import cn.zensezz.lingxiao.register.client.api.LingxiaoClientRegisterRepository;
import cn.zensezz.lingxiao.springboot.starter.client.common.dto.MetaDataRegisterDTO;
import cn.zensezz.lingxiao.springboot.starter.client.common.subsriber.ExecutorTypeSubscriber;
import cn.zensezz.lingxiao.springboot.starter.client.common.type.DataType;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.TimeUnit;


public class LingxiaoClientMetadataExecutorSubscriber implements ExecutorTypeSubscriber<MetaDataRegisterDTO> {

    private final LingxiaoClientRegisterRepository lingxiaoClientRegisterRepository;

    public LingxiaoClientMetadataExecutorSubscriber(final LingxiaoClientRegisterRepository lingxiaoClientRegisterRepository) {
        this.lingxiaoClientRegisterRepository  = lingxiaoClientRegisterRepository;
    }

    @Override
    public DataType getType() {
        return DataType.META_DATA;
    }

    @Override
    public void executor(final Collection<MetaDataRegisterDTO> metaDataRegisterDTOList) {
        for (MetaDataRegisterDTO metaDataRegisterDTO : metaDataRegisterDTOList) {
            while (true) {
                try (Socket socket = new Socket(metaDataRegisterDTO.getHost(), metaDataRegisterDTO.getPort())) {
                    break;
                } catch (IOException e) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            LingxiaoClientShutdownHook.delayOtherHooks();
            lingxiaoClientRegisterRepository.persistInterface(metaDataRegisterDTO);
        }
    }
}
