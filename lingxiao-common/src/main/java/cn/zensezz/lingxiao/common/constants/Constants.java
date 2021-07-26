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

package cn.zensezz.lingxiao.common.constants;

public interface Constants {

    String REQUESTDTO = "requestDTO";

    String CLIENT_RESPONSE_ATTR = "webHandlerClientResponse";

    String DUBBO_RPC_RESULT = "dubbo_rpc_result";

    String CLIENT_RESPONSE_RESULT_TYPE = "webHandlerClientResponseResultType";

    String DUBBO_RPC_PARAMS = "dubbo_rpc_params";

    String LINGXIOA_DISRUPTOR_THREAD_NAME = "lingxiao-disruptor";

    int LINGXIAO_EVENT_PUBLISHER_BUFF_SIZE = 1024;

    String DECODE = "UTF-8";

    String CONTEXT = "context";

    String PARAM_TRANSFORM = "param_transform";

    int DEFAULT_WARMUP = 10 * 60 * 1000;

    long TIME_OUT = 3000;

    int HEADER_MAX_SIZE = 10240;

    int REQUEST_MAX_SIZE = 102400;

    String COLONS = ":";

    String HTTP_URL = "httpUrl";

    String HTTP_TIME_OUT = "httpTimeOut";

    String HTTP_RETRY = "httpRetry";

    String REWRITE_URI = "rewrite_uri";

}

