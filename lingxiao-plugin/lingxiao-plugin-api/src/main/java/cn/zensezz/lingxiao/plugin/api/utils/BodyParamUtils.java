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

package cn.zensezz.lingxiao.plugin.api.utils;

import cn.zensezz.lingxiao.common.util.GsonUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BodyParamUtils {

    public static Pair<String[], Object[]> buildSingleParameter(final String body, final String parameterTypes) {
        final Map<String, Object> paramMap = GsonUtils.getInstance().toObjectMap(body);
        for (String key : paramMap.keySet()) {
            Object obj = paramMap.get(key);
            if (obj instanceof JsonObject) {
                paramMap.put(key, GsonUtils.getInstance().convertToMap(obj.toString()));
            } else if (obj instanceof JsonArray) {
                paramMap.put(key, GsonUtils.getInstance().fromList(obj.toString(), Object.class));
            } else {
                paramMap.put(key, obj);
            }
        }
        return new ImmutablePair<>(new String[]{parameterTypes}, new Object[]{paramMap});
    }

    public static Pair<String[], Object[]> buildParameters(final String body, final String parameterTypes) {
        List<String> paramNameList = new ArrayList<>();
        List<String> paramTypeList = new ArrayList<>();

        if (isNameMapping(parameterTypes)) {
            Map<String, String> paramNameMap = GsonUtils.getInstance().toObjectMap(parameterTypes, String.class);
            paramNameList.addAll(paramNameMap.keySet());
            paramTypeList.addAll(paramNameMap.values());
        } else {
            Map<String, Object> paramMap = GsonUtils.getInstance().toObjectMap(body);
            paramNameList.addAll(paramMap.keySet());
            paramTypeList.addAll(Arrays.asList(StringUtils.split(parameterTypes, ",")));
        }

        if (paramTypeList.size() == 1 && !isBaseType(paramTypeList.get(0))) {
            return buildSingleParameter(body, parameterTypes);
        }
        Map<String, Object> paramMap = GsonUtils.getInstance().toObjectMap(body);
        Object[] objects = paramNameList.stream().map(key -> {
            Object obj = paramMap.get(key);
            if (obj instanceof JsonObject) {
                return GsonUtils.getInstance().convertToMap(obj.toString());
            } else if (obj instanceof JsonArray) {
                return GsonUtils.getInstance().fromList(obj.toString(), Object.class);
            } else {
                return obj;
            }
        }).toArray();
        String[] paramTypes = paramTypeList.toArray(new String[0]);
        return new ImmutablePair<>(paramTypes, objects);
    }

    private static boolean isNameMapping(final String parameterTypes) {
        return parameterTypes.startsWith("{") && parameterTypes.endsWith("}");
    }

    private static boolean isBaseType(final String paramType) {
        return paramType.startsWith("java") || paramType.startsWith("[Ljava");
    }
}
