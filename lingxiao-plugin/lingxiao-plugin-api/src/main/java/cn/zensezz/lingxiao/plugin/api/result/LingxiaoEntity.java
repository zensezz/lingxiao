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

import lombok.Data;

import java.io.Serializable;


@Data
public class LingxiaoEntity implements Serializable {

    private static final long serialVersionUID = -1L;
    
    private static final int ERROR = 500;
    
    private static final int SUCCESSFUL = 200;

    private Integer code;

    private String message;

    private Object data;

    public LingxiaoEntity(final Integer code, final String message, final Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }


    public static LingxiaoEntity success() {
        return success("");
    }


    public static LingxiaoEntity success(final String msg) {
        return success(msg, null);
    }

    public static LingxiaoEntity success(final Object data) {
        return success(null, data);
    }

    public static LingxiaoEntity success(final String msg, final Object data) {
        return get(SUCCESSFUL, msg, data);
    }

    public static LingxiaoEntity success(final int code, final String msg, final Object data) {
        return get(code, msg, data);
    }

    public static LingxiaoEntity error(final String msg) {
        return error(ERROR, msg);
    }

    public static LingxiaoEntity error(final int code, final String msg) {
        return get(code, msg, null);
    }

    public static LingxiaoEntity error(final int code, final String msg, final Object data) {
        return get(code, msg, data);
    }

    public static LingxiaoEntity timeout(final String msg) {
        return error(ERROR, msg);
    }

    private static LingxiaoEntity get(final int code, final String msg, final Object data) {
        return new LingxiaoEntity(code, msg, data);
    }
}
