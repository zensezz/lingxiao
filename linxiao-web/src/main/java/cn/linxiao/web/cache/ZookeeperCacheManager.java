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

package cn.linxiao.web.cache;

import cn.zensezz.lingxiao.common.constants.ZkPathConstants;
import cn.zensezz.lingxiao.common.dto.zk.AppAuthZkDto;
import cn.zensezz.lingxiao.common.dto.zk.PluginZkDto;
import cn.zensezz.lingxiao.common.dto.zk.RuleZkDto;
import cn.zensezz.lingxiao.common.dto.zk.SelectorZkDto;
import cn.zensezz.lingxiao.common.dto.zk.SelectorZkDto;
import cn.zensezz.lingxiao.common.enums.PluginEnum;
import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@SuppressWarnings("unchecked")
public class ZookeeperCacheManager implements CommandLineRunner, DisposableBean {

    private static final Map<String, PluginZkDto> PLUGIN_MAP = Maps.newConcurrentMap();

    private static final Map<String, List<SelectorZkDto>> SELECTOR_MAP = Maps.newConcurrentMap();

    private static final Map<String, List<RuleZkDto>> RULE_MAP = Maps.newConcurrentMap();

    private static final Map<String, AppAuthZkDto> AUTH_MAP = Maps.newConcurrentMap();

    private final ZkClient zkClient;

    @Autowired(required = false)
    public ZookeeperCacheManager(final ZkClient zkClient) {
        this.zkClient = zkClient;
    }


    /**
     * acquire AppAuthZkDto by appKey with AUTH_MAP container.
     *
     * @param appKey this is appKey.
     * @return AppAuthZkDto {@linkplain AppAuthZkDto}
     */
    public AppAuthZkDto findAuthDtoByAppKey(final String appKey) {
        return AUTH_MAP.get(appKey);
    }


    /**
     * acquire PluginZkDto by pluginName with PLUGIN_MAP container.
     *
     * @param pluginName this is plugin name.
     * @return PluginZkDto {@linkplain  PluginZkDto}
     */
    public PluginZkDto findPluginByName(final String pluginName) {
        return PLUGIN_MAP.get(pluginName);
    }

    /**
     * acquire SelectorZkDto list  by pluginName with  SELECTOR_MAP HashMap container.
     *
     * @param pluginName this is plugin name.
     * @return SelectorZkDto list {@linkplain  SelectorZkDto}
     */
    public List<SelectorZkDto> findSelectorByPluginName(final String pluginName) {
        return SELECTOR_MAP.get(pluginName);
    }

    /**
     * acquire RuleZkDto list by selectorId with  RULE_MAP HashMap container.
     *
     * @param selectorId this is selectorId.
     * @return RuleZkDto list {@linkplain  RuleZkDto}
     */
    public List<RuleZkDto> findRuleBySelectorId(final String selectorId) {
        return RULE_MAP.get(selectorId);
    }

    @Override
    public void run(final String... args) {
        loadWatcherPlugin();
        loadWatcherSelector();
        loadWatcherRule();
        loadWatchAppAuth();
    }

    private void loadWatchAppAuth() {
        final String appAuthParent = ZkPathConstants.APP_AUTH_PARENT;
        if (!zkClient.exists(appAuthParent)) {
            zkClient.createPersistent(appAuthParent, true);
        }
        final List<String> childrenList = zkClient.getChildren(appAuthParent);
        if (CollectionUtils.isNotEmpty(childrenList)) {
            childrenList.forEach(children -> {
                String realPath = buildRealPath(appAuthParent, children);
                final AppAuthZkDto appAuthZkDto = zkClient.readData(realPath);
                Optional.ofNullable(appAuthZkDto)
                        .ifPresent(Dto -> AUTH_MAP.put(Dto.getAppKey(), Dto));
                subscribeAppAuthDataChanges(realPath);
            });
        }

        zkClient.subscribeChildChanges(appAuthParent, (parentPath, currentChilds) -> {
            if (CollectionUtils.isNotEmpty(currentChilds)) {
                final List<String> unsubscribePath = unsubscribePath(childrenList, currentChilds);
                unsubscribePath.stream().map(children -> buildRealPath(parentPath, children))
                        .forEach(this::subscribeAppAuthDataChanges);
            }
        });
    }

    private void subscribeAppAuthDataChanges(final String realPath) {
        zkClient.subscribeDataChanges(realPath, new IZkDataListener() {
            @Override
            public void handleDataChange(final String dataPath, final Object data) {
                Optional.ofNullable(data)
                        .ifPresent(o -> AUTH_MAP.put(((AppAuthZkDto) o).getAppKey(), (AppAuthZkDto) o));
            }

            @Override
            public void handleDataDeleted(final String dataPath) {
                final String key = dataPath.substring(ZkPathConstants.APP_AUTH_PARENT.length() + 1);
                AUTH_MAP.remove(key);
            }
        });
    }

    private void loadWatcherPlugin() {
        Arrays.stream(PluginEnum.values()).forEach(pluginEnum -> {
            String pluginPath = ZkPathConstants.buildPluginPath(pluginEnum.getName());
            if (!zkClient.exists(pluginPath)) {
                zkClient.createPersistent(pluginPath, true);
            }
            PluginZkDto data = zkClient.readData(pluginPath);
            Optional.ofNullable(data).ifPresent(d -> PLUGIN_MAP.put(pluginEnum.getName(), data));
            zkClient.subscribeDataChanges(pluginPath, new IZkDataListener() {
                @Override
                public void handleDataChange(final String dataPath, final Object data) {
                    Optional.ofNullable(data)
                            .ifPresent(o -> {
                                PluginZkDto Dto = (PluginZkDto) o;
                                PLUGIN_MAP.put(Dto.getName(), Dto);
                            });
                }

                @Override
                public void handleDataDeleted(final String dataPath) {
                    PLUGIN_MAP.remove(pluginEnum.getName());
                }
            });

        });
    }

    private void loadWatcherSelector() {
        Arrays.stream(PluginEnum.values()).forEach(pluginEnum -> {
            //获取选择器的节点
            String selectorParentPath =
                    ZkPathConstants.buildSelectorParentPath(pluginEnum.getName());

            if (!zkClient.exists(selectorParentPath)) {
                zkClient.createPersistent(selectorParentPath, true);
            }
            final List<String> childrenList = zkClient.getChildren(selectorParentPath);

            if (CollectionUtils.isNotEmpty(childrenList)) {
                childrenList.forEach(children -> {
                    String realPath = buildRealPath(selectorParentPath, children);
                    final SelectorZkDto selectorZkDto = zkClient.readData(realPath);
                    Optional.ofNullable(selectorZkDto)
                            .ifPresent(Dto -> {
                                final String key = Dto.getPluginName();
                                setSelectorMapByKey(key, Dto);
                            });
                    subscribeSelectorDataChanges(realPath);
                });

            }

            zkClient.subscribeChildChanges(selectorParentPath, (parentPath, currentChilds) -> {
                if (CollectionUtils.isNotEmpty(currentChilds)) {
                    final List<String> unsubscribePath = unsubscribePath(childrenList, currentChilds);
                    unsubscribePath.stream().map(p -> buildRealPath(parentPath, p))
                            .forEach(this::subscribeSelectorDataChanges);
                }

            });

        });
    }

    private void loadWatcherRule() {
        Arrays.stream(PluginEnum.values()).forEach(pluginEnum -> {
            final String ruleParent = ZkPathConstants.buildRuleParentPath(pluginEnum.getName());
            if (!zkClient.exists(ruleParent)) {
                zkClient.createPersistent(ruleParent, true);
            }

            final List<String> childrenList = zkClient.getChildren(ruleParent);
            if (CollectionUtils.isNotEmpty(childrenList)) {
                childrenList.forEach(children -> {
                    String realPath = buildRealPath(ruleParent, children);
                    final RuleZkDto ruleZkDto = zkClient.readData(realPath);
                    Optional.ofNullable(ruleZkDto)
                            .ifPresent(Dto -> {
                                String key = Dto.getSelectorId();
                                setRuleMapByKey(key, ruleZkDto);
                            });
                    subscribeRuleDataChanges(realPath);
                });
            }

            zkClient.subscribeChildChanges(ruleParent, (parentPath, currentChilds) -> {
                if (CollectionUtils.isNotEmpty(currentChilds)) {
                    final List<String> unsubscribePath = unsubscribePath(childrenList, currentChilds);
                    //获取新增的节点数据，并对该节点进行订阅
                    unsubscribePath.stream().map(p -> buildRealPath(parentPath, p))
                            .forEach(this::subscribeRuleDataChanges);
                }
            });
        });
    }

    /**
     * set  SelectorMap by key.
     *
     * @param key           SELECTOR_MAP key.
     * @param selectorZkDto data.
     */
    private void setSelectorMapByKey(final String key, final SelectorZkDto selectorZkDto) {
        Optional.ofNullable(key)
                .ifPresent(k -> {
                    if (SELECTOR_MAP.containsKey(k)) {
                        final List<SelectorZkDto> selectorZkDtoList = SELECTOR_MAP.get(key);
                        final List<SelectorZkDto> resultList = selectorZkDtoList.stream()
                                .filter(r -> !r.getId()
                                        .equals(selectorZkDto.getId()))
                                .collect(Collectors.toList());
                        resultList.add(selectorZkDto);
                        final List<SelectorZkDto> collect = resultList.stream()
                                .sorted(Comparator.comparing(SelectorZkDto::getRank))
                                .collect(Collectors.toList());
                        SELECTOR_MAP.put(key, collect);
                    } else {
                        SELECTOR_MAP.put(key, Lists.newArrayList(selectorZkDto));
                    }
                });
    }

    private void subscribeSelectorDataChanges(final String path) {
        zkClient.subscribeDataChanges(path, new IZkDataListener() {
            @Override
            public void handleDataChange(final String dataPath, final Object data) {
                Optional.ofNullable(data)
                        .ifPresent(d -> {
                            SelectorZkDto Dto = (SelectorZkDto) d;
                            final String key = Dto.getPluginName();
                            final List<SelectorZkDto> selectorZkDtoList = SELECTOR_MAP.get(key);
                            if (CollectionUtils.isNotEmpty(selectorZkDtoList)) {
                                final List<SelectorZkDto> resultList =
                                        selectorZkDtoList.stream().filter(r -> !r.getId() .equals(Dto.getId())) .collect(Collectors.toList());
                                resultList.add(Dto);
                                final List<SelectorZkDto> collect = resultList.stream()
                                        .sorted(Comparator.comparing(SelectorZkDto::getRank))
                                        .collect(Collectors.toList());
                                SELECTOR_MAP.put(key, collect);
                            } else {
                                SELECTOR_MAP.put(key, Lists.newArrayList(Dto));
                            }
                        });
            }

            @Override
            public void handleDataDeleted(final String dataPath) {
                //规定路径 key-id key为selectorId, id为规则id
                final String id = dataPath.substring(dataPath.lastIndexOf("/") + 1);
                final String str = dataPath.substring(ZkPathConstants.SELECTOR_PARENT.length());
                final String key = str.substring(1, str.length() - id.length() - 1);
                Optional.of(key).ifPresent(k -> {
                    final List<SelectorZkDto> selectorZkDtoList = SELECTOR_MAP.get(k);
                    selectorZkDtoList.removeIf(e -> e.getId().equals(id));
                });
            }
        });
    }

    private void setRuleMapByKey(final String key, final RuleZkDto ruleZkDto) {
        Optional.ofNullable(key)
                .ifPresent(k -> {
                    if (RULE_MAP.containsKey(k)) {
                        final List<RuleZkDto> ruleZkDtoList = RULE_MAP.get(key);
                        final List<RuleZkDto> resultList = ruleZkDtoList.stream()
                                .filter(r -> !r.getId()
                                        .equals(ruleZkDto.getId()))
                                .collect(Collectors.toList());
                        resultList.add(ruleZkDto);
                        final List<RuleZkDto> collect = resultList.stream()
                                .sorted(Comparator.comparing(RuleZkDto::getRank))
                                .collect(Collectors.toList());
                        RULE_MAP.put(key, collect);

                    } else {
                        RULE_MAP.put(key, Lists.newArrayList(ruleZkDto));
                    }
                });
    }

    private void subscribeRuleDataChanges(final String path) {
        zkClient.subscribeDataChanges(path, new IZkDataListener() {
            @Override
            public void handleDataChange(final String dataPath, final Object data) {
                Optional.ofNullable(data)
                        .ifPresent(d -> {
                            RuleZkDto Dto = (RuleZkDto) d;
                            final String key = Dto.getSelectorId();
                            final List<RuleZkDto> ruleZkDtoList = RULE_MAP.get(key);
                            if (CollectionUtils.isNotEmpty(ruleZkDtoList)) {
                                final List<RuleZkDto> resultList = ruleZkDtoList.stream()
                                        .filter(r -> !r.getId()
                                                .equals(Dto.getId())).collect(Collectors.toList());
                                resultList.add(Dto);
                                final List<RuleZkDto> collect = resultList.stream()
                                        .sorted(Comparator.comparing(RuleZkDto::getRank))
                                        .collect(Collectors.toList());
                                RULE_MAP.put(key, collect);
                            } else {
                                RULE_MAP.put(key, Lists.newArrayList(Dto));
                            }
                        });
            }

            @Override
            public void handleDataDeleted(final String dataPath) {
                //规定路径 key-id key为selectorId, id为规则id
                final List<String> list = Splitter.on(ZkPathConstants.SELECTOR_JOIN_RULE)
                        .splitToList(dataPath.substring(dataPath.lastIndexOf("/") + 1));
                final String key = list.get(0);
                final String id = list.get(1);
                Optional.ofNullable(key).ifPresent(k -> {
                    final List<RuleZkDto> ruleZkDtoList = RULE_MAP.get(k);
                    ruleZkDtoList.removeIf(e -> e.getId().equals(id));
                });
            }
        });
    }

    private List<String> unsubscribePath(final List<String> alreadyChildren, final List<String> currentChilds) {
        if (CollectionUtils.isEmpty(alreadyChildren)) {
            return currentChilds;
        }
        return currentChilds.stream().filter(c -> alreadyChildren.stream().anyMatch(a -> !c.equals(a))).collect(Collectors.toList());
    }

    private String buildRealPath(final String parent, final String children) {
        return parent + "/" + children;
    }

    @Override
    public void destroy() {
        zkClient.close();
    }
}
