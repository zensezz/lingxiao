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

package cn.zensezz.lingxiao.disruptor.provider;

import cn.zensezz.lingxiao.disruptor.event.DataEvent;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class DisruptorProvider<T> {
    
    private final RingBuffer<DataEvent<T>> ringBuffer;
    
    private final Disruptor<DataEvent<T>> disruptor;

    public DisruptorProvider(final RingBuffer<DataEvent<T>> ringBuffer, final Disruptor<DataEvent<T>> disruptor) {
        this.ringBuffer = ringBuffer;
        this.disruptor = disruptor;
    }

    public void onData(final Consumer<DataEvent<T>> function) {
        long position = ringBuffer.next();
        try {
            DataEvent<T> dataEvent = ringBuffer.get(position);
            function.accept(dataEvent);
            ringBuffer.publish(position);
        } catch (Exception ex) {
            log.error("ex", ex);
        }
    }

    public void shutdown() {
        if (null != disruptor) {
            disruptor.shutdown();
        }
    }
}
