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

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rlink.admin.common.biz.user.UserContext;
import com.rlink.admin.common.convention.exception.ClientException;
import com.rlink.admin.common.convention.exception.ServiceException;
import com.rlink.admin.common.enums.UserErrorCodeEnum;
import com.rlink.admin.dao.entity.UserDO;
import com.rlink.admin.dao.mapper.UserMapper;
import com.rlink.admin.dto.req.UserLoginReqDTO;
import com.rlink.admin.dto.req.UserPageReqDTO;
import com.rlink.admin.dto.req.UserRegisterReqDTO;
import com.rlink.admin.dto.req.UserStatusUpdateReqDTO;
import com.rlink.admin.dto.req.UserUpdateReqDTO;
import com.rlink.admin.dto.resp.UserLoginRespDTO;
import com.rlink.admin.dto.resp.UserRespDTO;
import com.rlink.admin.service.SystemSettingsService;
import com.rlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.rlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.rlink.admin.common.constant.RedisCacheConstant.USER_LOGIN_KEY;
import static com.rlink.admin.common.enums.UserErrorCodeEnum.USER_EXIST;
import static com.rlink.admin.common.enums.UserErrorCodeEnum.USER_NAME_EXIST;
import static com.rlink.admin.common.enums.UserErrorCodeEnum.USER_SAVE_ERROR;

/**
 * 用户接口实现层
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_USER = "user";
    private static final String ROLE_GUEST = "guest";
    private static final int STATUS_ENABLED = 0;
    private static final int STATUS_DISABLED = 1;

    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final SystemSettingsService systemSettingsService;

    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ServiceException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(userDO, result);
        return result;
    }

    @Override
    public Boolean hasUsername(String username) {
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(UserRegisterReqDTO requestParam) {
        if (!systemSettingsService.getSettings().getAllowRegister()) {
            throw new ClientException("系统当前已关闭注册");
        }
        doCreateUser(requestParam);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void adminCreate(UserRegisterReqDTO requestParam) {
        ensureAdminOperator();
        doCreateUser(requestParam);
    }

    private void doCreateUser(UserRegisterReqDTO requestParam) {
        if (!hasUsername(requestParam.getUsername())) {
            throw new ClientException(USER_NAME_EXIST);
        }
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + requestParam.getUsername());
        if (!lock.tryLock()) {
            throw new ClientException(USER_NAME_EXIST);
        }
        try {
            if (StrUtil.isBlank(requestParam.getRole())) {
                requestParam.setRole(ROLE_USER);
            }
            if (requestParam.getStatus() == null) {
                requestParam.setStatus(STATUS_ENABLED);
            }
            int inserted = baseMapper.insert(BeanUtil.toBean(requestParam, UserDO.class));
            if (inserted < 1) {
                throw new ClientException(USER_SAVE_ERROR);
            }
            userRegisterCachePenetrationBloomFilter.add(requestParam.getUsername());
        } catch (DuplicateKeyException ex) {
            throw new ClientException(USER_EXIST);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void update(UserUpdateReqDTO requestParam) {
        if (!Objects.equals(requestParam.getUsername(), UserContext.getUsername())) {
            throw new ClientException("当前登录用户修改请求异常");
        }
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername());
        baseMapper.update(BeanUtil.toBean(requestParam, UserDO.class), updateWrapper);
    }

    @Override
    public UserLoginRespDTO login(UserLoginReqDTO requestParam) {
        String username = StrUtil.trim(requestParam.getUsername());
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username)
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = baseMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new ClientException("用户不存在");
        }
        if (!StrUtil.equals(userDO.getPassword(), requestParam.getPassword())) {
            throw new ClientException("密码错误");
        }
        if (Objects.equals(userDO.getStatus(), STATUS_DISABLED)) {
            throw new ClientException("用户已被封禁");
        }
        Map<Object, Object> hasLoginMap = stringRedisTemplate.opsForHash()
                .entries(USER_LOGIN_KEY + username);
        if (CollUtil.isNotEmpty(hasLoginMap)) {
            stringRedisTemplate.expire(USER_LOGIN_KEY + username, 30L, TimeUnit.MINUTES);
            String token = hasLoginMap.keySet().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElseThrow(() -> new ClientException("用户登录错误"));
            return new UserLoginRespDTO(token, userDO.getRole(), userDO.getStatus());
        }
        /**
         * Hash
         * Key：login_用户名
         * Value：
         * Key：token标识
         * Val：JSON 字符串（用户信息）
         */
        String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(USER_LOGIN_KEY + username, uuid,
                JSON.toJSONString(userDO));
        stringRedisTemplate.expire(USER_LOGIN_KEY + username, 30L, TimeUnit.MINUTES);
        return new UserLoginRespDTO(uuid, userDO.getRole(), userDO.getStatus());
    }

    @Override
    public Boolean checkLogin(String username, String token) {
        return stringRedisTemplate.opsForHash().get(USER_LOGIN_KEY + username, token) != null;
    }

    @Override
    public void logout(String username, String token) {
        if (checkLogin(username, token)) {
            stringRedisTemplate.delete(USER_LOGIN_KEY + username);
            return;
        }
        throw new ClientException("用户Token不存在或用户未登录");
    }

    @Override
    public Page<UserRespDTO> pageUsers(UserPageReqDTO requestParam) {
        ensureAdminOperator();
        Page<UserDO> page = new Page<>(requestParam.getCurrent(), requestParam.getSize());
        LambdaQueryWrapper<UserDO> queryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(requestParam.getStatus() != null, UserDO::getStatus, requestParam.getStatus())
                .eq(StrUtil.isNotBlank(requestParam.getRole()), UserDO::getRole, requestParam.getRole())
                .and(StrUtil.isNotBlank(requestParam.getKeyword()), wrapper -> wrapper
                        .like(UserDO::getUsername, requestParam.getKeyword())
                        .or()
                        .like(UserDO::getRealName, requestParam.getKeyword())
                        .or()
                        .like(UserDO::getMail, requestParam.getKeyword()))
                .eq(UserDO::getDelFlag, 0)
                .orderByDesc(UserDO::getCreateTime);
        Page<UserDO> resultPage = baseMapper.selectPage(page, queryWrapper);
        Page<UserRespDTO> respPage = new Page<>(resultPage.getCurrent(), resultPage.getSize(), resultPage.getTotal());
        respPage.setRecords(BeanUtil.copyToList(resultPage.getRecords(), UserRespDTO.class));
        return respPage;
    }

    @Override
    public void adminUpdate(UserUpdateReqDTO requestParam) {
        ensureAdminOperator();
        assertUserOperable(requestParam.getUsername(), false);
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getDelFlag, 0);
        UserDO userDO = BeanUtil.toBean(requestParam, UserDO.class);
        if (StrUtil.isBlank(userDO.getRole())) {
            userDO.setRole(null);
        }
        if (userDO.getStatus() == null) {
            userDO.setStatus(null);
        }
        baseMapper.update(userDO, updateWrapper);
    }

    @Override
    public void updateUserStatus(UserStatusUpdateReqDTO requestParam) {
        ensureAdminOperator();
        assertUserOperable(requestParam.getUsername(), true);
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, requestParam.getUsername())
                .eq(UserDO::getDelFlag, 0)
                .set(UserDO::getStatus, requestParam.getStatus());
        baseMapper.update(null, updateWrapper);
    }

    @Override
    public void deleteUser(String username) {
        ensureAdminOperator();
        assertUserOperable(username, false);
        LambdaUpdateWrapper<UserDO> updateWrapper = Wrappers.lambdaUpdate(UserDO.class)
                .eq(UserDO::getUsername, username)
                .eq(UserDO::getDelFlag, 0)
                .set(UserDO::getDelFlag, 1);
        baseMapper.update(null, updateWrapper);
    }

    private void ensureAdminOperator() {
        String username = UserContext.getUsername();
        if (StrUtil.isBlank(username)) {
            throw new ClientException("当前登录用户不存在");
        }
        UserDO currentUser = baseMapper.selectOne(Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username)
                .eq(UserDO::getDelFlag, 0));
        if (currentUser == null || !StrUtil.equalsIgnoreCase(ROLE_ADMIN, currentUser.getRole())) {
            throw new ClientException("当前用户无管理员权限");
        }
    }

    private void assertUserOperable(String username, boolean allowGuest) {
        if (StrUtil.equalsIgnoreCase(ROLE_ADMIN, username)) {
            throw new ClientException("admin 用户不可被修改为禁用或删除");
        }
        if (!allowGuest && StrUtil.equalsIgnoreCase(ROLE_GUEST, username)) {
            throw new ClientException("guest 用户不可删除");
        }
    }
}
