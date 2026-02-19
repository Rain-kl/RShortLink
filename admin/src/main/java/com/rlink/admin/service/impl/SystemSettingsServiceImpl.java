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

package com.rlink.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.rlink.admin.common.biz.user.UserContext;
import com.rlink.admin.common.convention.exception.ClientException;
import com.rlink.admin.common.constant.RedisCacheConstant;
import com.rlink.admin.dao.entity.UserDO;
import com.rlink.admin.dao.mapper.UserMapper;
import com.rlink.admin.dto.req.SystemSettingsUpdateReqDTO;
import com.rlink.admin.dto.resp.SystemSettingsRespDTO;
import com.rlink.admin.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SystemSettingsServiceImpl implements SystemSettingsService {

    private final StringRedisTemplate stringRedisTemplate;
    private final UserMapper userMapper;

    @Override
    public SystemSettingsRespDTO getSettings() {
        Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(RedisCacheConstant.SYSTEM_SETTINGS_KEY);
        return SystemSettingsRespDTO.builder()
                .allowRegister(Boolean.parseBoolean(String.valueOf(map.getOrDefault("allowRegister", "true"))))
                .allowGuest(Boolean.parseBoolean(String.valueOf(map.getOrDefault("allowGuest", "true"))))
                .blackList(String.valueOf(map.getOrDefault("blackList", "")))
                .build();
    }

    @Override
    public void updateSettings(SystemSettingsUpdateReqDTO requestParam) {
        ensureAdminOperator();
        stringRedisTemplate.opsForHash().put(RedisCacheConstant.SYSTEM_SETTINGS_KEY, "allowRegister",
                String.valueOf(Boolean.TRUE.equals(requestParam.getAllowRegister())));
        stringRedisTemplate.opsForHash().put(RedisCacheConstant.SYSTEM_SETTINGS_KEY, "allowGuest",
                String.valueOf(Boolean.TRUE.equals(requestParam.getAllowGuest())));
        stringRedisTemplate.opsForHash().put(RedisCacheConstant.SYSTEM_SETTINGS_KEY, "blackList",
                StrUtil.nullToDefault(requestParam.getBlackList(), ""));
    }

    private void ensureAdminOperator() {
        String username = UserContext.getUsername();
        UserDO currentUser = userMapper
                .selectOne(com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery(UserDO.class)
                        .eq(UserDO::getUsername, username)
                        .eq(UserDO::getDelFlag, 0));
        if (currentUser == null || !StrUtil.equalsIgnoreCase("admin", currentUser.getRole())) {
            throw new ClientException("当前用户无管理员权限");
        }
    }
}