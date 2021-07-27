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

package cn.zensezz.lingxiao.examples.dubbo.api.service;


import cn.zensezz.lingxiao.examples.dubbo.api.entity.ComplexBeanTest;
import cn.zensezz.lingxiao.examples.dubbo.api.entity.DubboTest;

import java.util.List;

public interface DubboMultiParamService {

    DubboTest findByIdsAndName(List<Integer> ids, String name);

    DubboTest findByArrayIdsAndName(Integer[] ids, String name);

    DubboTest findByStringArray(String[] ids);

    DubboTest findByListId(List<String> ids);

    DubboTest batchSave(List<DubboTest> dubboTestList);

    DubboTest batchSaveAndNameAndId(List<DubboTest> dubboTestList, String id, String name);

    DubboTest saveComplexBeanTest(ComplexBeanTest complexBeanTest);

    DubboTest saveComplexBeanTestAndName(ComplexBeanTest complexBeanTest, String name);
}
