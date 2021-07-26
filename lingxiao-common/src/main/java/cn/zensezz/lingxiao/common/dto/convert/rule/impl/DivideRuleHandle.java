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

package cn.zensezz.lingxiao.common.dto.convert.rule.impl;


import cn.zensezz.lingxiao.common.constants.Constants;
import cn.zensezz.lingxiao.common.constants.RuleHandleConstants;
import cn.zensezz.lingxiao.common.dto.convert.rule.RuleHandle;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class DivideRuleHandle implements RuleHandle {

    private static final long serialVersionUID = 1L;

    private String loadBalance;


    private int retry;


    private long timeout = Constants.TIME_OUT;


    private long headerMaxSize = Constants.HEADER_MAX_SIZE;

    /**
     * requestMaxSize.
     */
    private long requestMaxSize = Constants.REQUEST_MAX_SIZE;

    @Override
    public RuleHandle createDefault(final String path) {
        this.loadBalance = RuleHandleConstants.DEFAULT_LOAD_BALANCE.getName();
        this.retry = RuleHandleConstants.DEFAULT_RETRY;
        return this;
    }
}
