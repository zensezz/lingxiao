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

package cn.zensezz.lingxiao.examples.http.router;

import cn.zensezz.lingxiao.examples.http.result.ResultBean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Component
public class LingxiaoTestHttpRouter {

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(1);

    public RouterFunction<ServerResponse> routes() {
        return nest(path("/test"),
                route(POST("/helloWorld2").and(accept(APPLICATION_JSON)), this::postHandler)
                        .and(route(GET("/rewrite"), this::rewriteHandler))
                        .and(route(GET("/pdm"), this::pdmHttpGet))
                        .and(route(GET("/oms"), this::omsHttpGet))
                        .and(route(GET("/timeout"), this::testRetry)));
    }

    private Mono<ServerResponse> testRetry(final ServerRequest req) {
        int i = ATOMIC_INTEGER.incrementAndGet();
        System.out.println("Retry count: " + i);
        ResultBean resultBean = new ResultBean(1, "msg", "this is retry hello world");
        return ok().body(Mono.just(resultBean), ResultBean.class);
    }

    private Mono<ServerResponse> pdmHttpGet(final ServerRequest req) {
        ResultBean resultBean = new ResultBean(1, "msg", "this is pdm get hello world");
        return ok().body(Mono.just(resultBean), ResultBean.class);
    }

    private Mono<ServerResponse> omsHttpGet(final ServerRequest req) {
        ResultBean resultBean = new ResultBean(1, "msg", "this is oms get hello world");
        return ok().body(Mono.just(resultBean), ResultBean.class);
    }

    private Mono<ServerResponse> rewriteHandler(final ServerRequest req) {
        ResultBean resultBean = new ResultBean(1, "msg", "this is rewrite hello world");
        return ok().body(Mono.just(resultBean), ResultBean.class);
    }

    private Mono<ServerResponse> postHandler(final ServerRequest req) {
        final Mono<String> string = req.bodyToMono(String.class);
        return ok().body(string, String.class);
    }

}
