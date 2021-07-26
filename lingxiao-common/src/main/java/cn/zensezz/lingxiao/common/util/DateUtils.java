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

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
public class DateUtils {

    private static final String DATE_FORMAT_DATETIME = "yyyy-MM-dd HH:mm:ss";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT_DATETIME);

    public static LocalDateTime parseLocalDateTime(final String dataTime) {
        return LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern(DATE_FORMAT_DATETIME));
    }

    public static LocalDateTime parseLocalDateTime(final String dataTime, final String dateTimeFormatter) {
        return LocalDateTime.parse(dataTime, DateTimeFormatter.ofPattern(dateTimeFormatter));
    }

    public static long acquireMinutesBetween(final LocalDateTime start, final LocalDateTime end) {
        return start.until(end, ChronoUnit.MINUTES);
    }

    public static long acquireMillisBetween(final LocalDateTime start, final LocalDateTime end) {
        return start.until(end, ChronoUnit.MILLIS);
    }

    public static LocalDateTime formatLocalDateTimeFromTimestamp(final Long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.ofHours(8));
    }

    public static LocalDateTime formatLocalDateTimeFromTimestampBySystemTimezone(final Long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp / 1000, 0, OffsetDateTime.now().getOffset());
    }
    
    /**
     * Format local date time to string.
     * use default pattern yyyy-MM-dd HH:mm:ss
     *
     * @param localDateTime the localDateTime
     * @return the format string
     */
    public static String localDateTimeToString(final LocalDateTime localDateTime) {
        return DATE_TIME_FORMATTER.format(localDateTime);
    }
    
    /**
     * Format local date time to string.
     *
     * @param localDateTime the localDateTime
     * @param pattern formatter pattern
     * @return the format string
     */
    public static String localDateTimeToString(final LocalDateTime localDateTime, final String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return localDateTime.format(formatter);
    }
}
