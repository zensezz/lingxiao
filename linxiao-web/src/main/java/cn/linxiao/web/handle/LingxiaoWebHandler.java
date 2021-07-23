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

package cn.linxiao.web.handle;

import cn.linxiao.web.plugin.LingxiaoPlugin;
import cn.linxiao.web.plugin.LingxiaoPluginChain;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.List;

public final class LingxiaoWebHandler implements WebHandler {

    private List<LingxiaoPlugin> plugins;

    public LingxiaoWebHandler(final List<LingxiaoPlugin> plugins) {
        this.plugins = plugins;
    }

    @Override
    public Mono<Void> handle(@NotNull final ServerWebExchange exchange) {
        return new DefaultLingxiaoPluginChain(plugins).execute(exchange).doOnError(Throwable::printStackTrace);
    }

    private static class DefaultLingxiaoPluginChain implements LingxiaoPluginChain {

        private int index;

        private final List<LingxiaoPlugin> plugins;

        DefaultLingxiaoPluginChain(final List<LingxiaoPlugin> plugins) {
            this.plugins = plugins;
        }

        @Override
        public Mono<Void> execute(final ServerWebExchange exchange) {
            if (this.index < plugins.size()) {
                LingxiaoPlugin plugin = plugins.get(this.index++);
                return plugin.execute(exchange, this);
            } else {
                return Mono.empty();
            }
        }
    }
}
