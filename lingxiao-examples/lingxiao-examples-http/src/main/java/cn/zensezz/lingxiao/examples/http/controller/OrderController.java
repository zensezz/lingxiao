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

package cn.zensezz.lingxiao.examples.http.controller;


import cn.zensezz.lingxiao.examples.http.dto.OAuth2DTO;
import cn.zensezz.lingxiao.examples.http.dto.OrderDTO;
import cn.znesezz.lingxiao.client.http.springmvc.annotation.LingxiaoSpringMvcClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/order")
@LingxiaoSpringMvcClient(path = "/order")
public class OrderController {


    @PostMapping("/save")
    @LingxiaoSpringMvcClient(path = "/save", desc = "Save order")
    public OrderDTO save(@RequestBody final OrderDTO orderDTO) {
        orderDTO.setName("hello world save order");
        return orderDTO;
    }

    @GetMapping("/findById")
    @LingxiaoSpringMvcClient(path = "/findById", desc = "Find by id")
    public OrderDTO findById(@RequestParam("id") final String id) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(id);
        orderDTO.setName("hello world findById");
        return orderDTO;
    }


    @GetMapping("/path/{id}/{name}")
    @LingxiaoSpringMvcClient(path = "/path/**")
    public OrderDTO getPathVariable(@PathVariable("id") final String id, @PathVariable("name") final String name) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(id);
        orderDTO.setName("hello world restful: " + name);
        return orderDTO;
    }

    @GetMapping("/path/{id}/name")
    @LingxiaoSpringMvcClient(path = "/path/**/name")
    public OrderDTO testRestFul(@PathVariable("id") final String id) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(id);
        orderDTO.setName("hello world restful inline " + id);
        return orderDTO;
    }

    @GetMapping("/oauth2/test")
    @LingxiaoSpringMvcClient(path = "/oauth2/test")
    public OAuth2DTO testRestFul(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        List<String> tokens = headers.get("Authorization");
        OAuth2DTO oAuth2DTO = new OAuth2DTO();
        if (Objects.isNull(tokens)) {
            oAuth2DTO.setToken("no authorization");
        } else {
            oAuth2DTO.setToken(tokens.get(0));
        }
        return oAuth2DTO;
    }

}
