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

package cn.linxiao.web.disruptor.publisher;

import cn.linxiao.web.disruptor.event.LingxiaoDataEvent;
import cn.linxiao.web.disruptor.factory.LingxiaoEventFactory;
import cn.linxiao.web.disruptor.handle.ClearingEventHandler;
import cn.linxiao.web.disruptor.handle.LingxiaoEventHandler;
import cn.linxiao.web.disruptor.translator.LingxiaoEventTranslator;
import cn.linxiao.web.entity.dos.MonitorDo;
import cn.linxiao.web.factory.LingxiaoThreadFactory;
import cn.zensezz.lingxiao.common.constants.Constants;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class LingxiaoEventPublisher implements InitializingBean, DisposableBean {

    private Disruptor<LingxiaoDataEvent> disruptor;

    private final LingxiaoEventHandler lingxiaoEventHandler;

    private final ClearingEventHandler clearingEventHandler;

    private int bufferSize;

    @Autowired
    public LingxiaoEventPublisher(final LingxiaoEventHandler lingxiaoEventHandler,
                              final ClearingEventHandler clearingEventHandler) {
        this.lingxiaoEventHandler = lingxiaoEventHandler;
        this.clearingEventHandler = clearingEventHandler;
    }

    /**
     * disruptor start with bufferSize.
     *
     * @param bufferSize bufferSize
     */
    private void start(final int bufferSize) {
        disruptor = new Disruptor<>(new LingxiaoEventFactory(),
                bufferSize, LingxiaoThreadFactory.create(Constants.LINGXIOA_DISRUPTOR_THREAD_NAME,
                false), ProducerType.SINGLE, new SleepingWaitStrategy());
        disruptor.handleEventsWith(lingxiaoEventHandler).then(clearingEventHandler);
        disruptor.start();
    }

    /**
     * publish disruptor event.
     *
     * @param monitorDO data.
     */
    public void publishEvent(final MonitorDo monitorDO) {
        final RingBuffer<LingxiaoDataEvent> ringBuffer = disruptor.getRingBuffer();
        ringBuffer.publishEvent(new LingxiaoEventTranslator(), monitorDO);
    }

    @Override
    public void destroy() {
        disruptor.shutdown();
    }

    @Override
    public void afterPropertiesSet() {
        start(Constants.LINGXIAO_EVENT_PUBLISHER_BUFF_SIZE);
    }
}
