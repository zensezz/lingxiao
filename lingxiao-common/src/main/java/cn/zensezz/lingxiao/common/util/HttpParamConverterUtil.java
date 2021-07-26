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

package cn.zensezz.lingxiao.common.util;

import lombok.SneakyThrows;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HttpParamConverterUtil {

    private static final Pattern PATTERN = Pattern.compile("([^&=]+)(=?)([^&]+)?");

    public static String ofString(final Supplier<String> supplier) {
        return GsonUtils.getInstance().toJson(initQueryParams(supplier.get()));
    }


    public static <K, V> String toMap(final Supplier<MultiValueMap<K, V>> supplier) {
        return GsonUtils.getInstance().toJson(supplier.get().toSingleValueMap());
    }

    public static Map<String, String> initQueryParams(final String query) {
        final Map<String, String> queryParams = new LinkedHashMap<>();
        if (!StringUtils.isEmpty(query)) {
            final Matcher matcher = PATTERN.matcher(query);
            while (matcher.find()) {
                String name = decodeQueryParam(matcher.group(1));
                String eq = matcher.group(2);
                String value = matcher.group(3);
                value = !StringUtils.isEmpty(value) ? decodeQueryParam(value) : (StringUtils.hasLength(eq) ? "" : null);
                queryParams.put(name, value);
            }
        }
        return queryParams;
    }

    @SneakyThrows
    public static String decodeQueryParam(final String value) {
        return URLDecoder.decode(value, "UTF-8");
    }
}
