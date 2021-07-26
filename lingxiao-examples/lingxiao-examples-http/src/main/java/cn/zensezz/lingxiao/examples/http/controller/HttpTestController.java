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

import cn.zensezz.lingxiao.examples.http.dto.UserDTO;
import cn.znesezz.lingxiao.client.http.springmvc.annotation.LingxiaoSpringMvcClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@LingxiaoSpringMvcClient(path = "/test/**")
public class HttpTestController {

    @PostMapping("/payment")
    public UserDTO post(@RequestBody final UserDTO userDTO) {
        return userDTO;
    }

    @GetMapping("/findByUserId")
    public UserDTO findByUserId(@RequestParam("userId") final String userId) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(userId);
        userDTO.setUserName("hello world");
        return userDTO;
    }

    @GetMapping("/path/{id}")
    public UserDTO getPathVariable(@PathVariable("id") final String id, @RequestParam("name") final String name) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(id);
        userDTO.setUserName("hello world");
        return userDTO;
    }

    @GetMapping("/path/{id}/name")
    public UserDTO testRestFul(@PathVariable("id") final String id) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(id);
        userDTO.setUserName("hello world");
        return userDTO;
    }


    @PutMapping("/putPathBody/{id}")
    public UserDTO putPathVariableAndBody(@PathVariable("id") final String id, @RequestBody final UserDTO userDTO) {
        userDTO.setUserId(id);
        userDTO.setUserName("hello world");
        return userDTO;
    }

}
