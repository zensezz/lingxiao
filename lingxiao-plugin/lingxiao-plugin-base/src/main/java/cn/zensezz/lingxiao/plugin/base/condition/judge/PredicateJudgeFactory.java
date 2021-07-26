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

package cn.zensezz.lingxiao.plugin.base.condition.judge;

import cn.zensezz.lingxiao.common.dto.ConditionData;
import cn.zensezz.lingxiao.spi.ExtensionLoader;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class PredicateJudgeFactory {

    public static PredicateJudge newInstance(final String operator) {
        return ExtensionLoader.getExtensionLoader(PredicateJudge.class).getJoin(processSpecialOperator(operator));
    }

    public static Boolean judge(final ConditionData conditionData, final String realData) {
        if (Objects.isNull(conditionData) || StringUtils.isBlank(realData)) {
            return false;
        }
        return newInstance(conditionData.getOperator()).judge(conditionData, realData);
    }

    private static String processSpecialOperator(final String operator) {
        return "=".equals(operator) ? "equals" : operator;
    }
}