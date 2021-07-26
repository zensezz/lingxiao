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

package cn.zensezz.lingxiao.plugin.api.result;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LingxiaoResultEnum {

    FAIL(-1, "Internal exception in gateway. Please try again later!"),

    SUCCESS(200, "Access to success!"),

    SIGN_IS_NOT_PASS(401, "Sign is not pass lingxiao!"),

    ERROR_TOKEN(401, "Illegal authorization"),

    PAYLOAD_TOO_LARGE(403, "Payload too large!"),

    TOO_MANY_REQUESTS(429, "You have been restricted, please try again later!"),

    HYSTRIX_PLUGIN_FALLBACK(429, "HystrixPlugin fallback success, please check your service status!"),

    RESILIENCE4J_PLUGIN_FALLBACK(429, "Resilience4JPlugin fallback success, please check your service status!"),


    META_DATA_ERROR(430, "Meta data error!"),

    DUBBO_HAVE_BODY_PARAM(431, "Dubbo must have body param, please enter the JSON format in the body!"),

    SOFA_HAVE_BODY_PARAM(432, "Sofa must have body param, please enter the JSON format in the body!"),

    TARS_HAVE_BODY_PARAM(433, "Tars must have body param, please enter the JSON format in the body!"),

    TARS_INVOKE(434, "Tars invoke error!"),

    GRPC_HAVE_BODY_PARAM(435, "Grpc must have body param, please enter the JSON format in the body!"),

    GRPC_CLIENT_NULL(436, "Grpc client is null, please check the context path!"),

    MOTAN_HAVE_BODY_PARAM(437, "Motan must have body param, please enter the JSON format in the body!"),

    PARAM_ERROR(-100, "Your parameter error, please check the relevant documentation!"),

    TIME_ERROR(-101, "Your time parameter is incorrect or has expired!"),

    RULE_NOT_FOUND(-102, "rule not found!"),

    SERVICE_RESULT_ERROR(-103, "Service invocation exception, or no result is returned!"),

    SERVICE_TIMEOUT(-104, "Service call timeout!"),

    SIGN_TIME_IS_TIMEOUT(-105, "The signature timestamp has exceeded %s minutes!"),

    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),

    CANNOT_FIND_URL(-106,"can not find url"),


    SELECTOR_NOT_FOUND(-107,"can not find  selector"),

    REQUEST_HEADER_TOO_LARGE(431, "Request Header Fields Too Large"),

    ;




    private final int code;

    private final String msg;
}
