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

import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
public class GlobalErrorHandler implements WebExceptionHandler {
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        return handle(ex).flatMap(it -> it.writeTo(exchange,
                new HandlerStrategiesResponseContext(HandlerStrategies.withDefaults())))
                .flatMap(i -> Mono.empty());
    }


    private Mono<ServerResponse> handle(Throwable ex) {
        return createResponse(INTERNAL_SERVER_ERROR, "GENERIC_ERROR", "Unhandled exception");
    }

    private Mono<ServerResponse> createResponse(HttpStatus httpStatus, String code, String mesage) {

        return ServerResponse.status(httpStatus).syncBody(mesage);
    }

    static class HandlerStrategiesResponseContext implements ServerResponse.Context {

        private HandlerStrategies handlerStrategies;

        public HandlerStrategiesResponseContext(HandlerStrategies handlerStrategies) {
            this.handlerStrategies = handlerStrategies;
        }

        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            return this.handlerStrategies.messageWriters();
        }

        @Override
        public List<ViewResolver> viewResolvers() {
            return this.handlerStrategies.viewResolvers();
        }
    }

}


