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

package cn.zensezz.lingxiao.disruptor.consumer;

import com.lmax.disruptor.WorkHandler;
import cn.zensezz.lingxiao.disruptor.event.DataEvent;

import java.util.concurrent.ExecutorService;

public class QueueConsumer<T> implements WorkHandler<DataEvent<T>> {
    
    private ExecutorService executor;
    
    private QueueConsumerFactory<T> factory;

    public QueueConsumer(final ExecutorService executor, final QueueConsumerFactory<T> factory) {
        this.executor = executor;
        this.factory = factory;
    }

    @Override
    public void onEvent(final DataEvent<T> t) {
        if (t != null) {
            QueueConsumerExecutor<T> queueConsumerExecutor = factory.create();
            queueConsumerExecutor.setData(t.getData());
            executor.execute(queueConsumerExecutor);
        }
    }
}
