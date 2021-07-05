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

package cn.zensezz.lingxiao.common.result;

import cn.zensezz.lingxiao.common.exception.ErrorCode;
import lombok.Data;
import java.io.Serializable;

@Data
public class LinxiaoResult implements Serializable {

    private static final long serialVersionUID = -2792556188993845048L;

    private Integer code;

    private String message;

    private Object data;

    public LinxiaoResult(final Integer code, final String message, final Object data) {

        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static LinxiaoResult success() {
        return success("");
    }

    public static LinxiaoResult success(final String msg) {
        return success(msg, null);
    }

    public static LinxiaoResult success(final Object data) {
        return success(null, data);
    }

    public static LinxiaoResult success(final String msg, final Object data) {
        return get(ErrorCode.SUCCESSFUL, msg, data);
    }

    public static LinxiaoResult error(final String msg) {
        return error(ErrorCode.ERROR, msg);
    }

    public static LinxiaoResult error(final int code, final String msg) {
        return get(code, msg, null);
    }

    private static LinxiaoResult get(final int code, final String msg, final Object data) {
        return new LinxiaoResult(code, msg, data);
    }

}
