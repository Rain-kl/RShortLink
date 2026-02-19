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

package com.rlink.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rlink.admin.common.convention.result.Result;
import com.rlink.admin.common.convention.result.Results;
import com.rlink.admin.dao.entity.UserDO;
import com.rlink.admin.dto.resp.AdminOverviewRespDTO;
import com.rlink.admin.dto.req.SystemSettingsUpdateReqDTO;
import com.rlink.admin.dto.resp.SystemSettingsRespDTO;
import com.rlink.admin.remote.ShortLinkActualRemoteService;
import com.rlink.admin.remote.dto.req.AdminShortLinkPageReqDTO;
import com.rlink.admin.remote.dto.resp.AdminShortLinkOverviewRespDTO;
import com.rlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.rlink.admin.service.SystemSettingsService;
import com.rlink.admin.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AdminConsoleController {

    private final SystemSettingsService systemSettingsService;
    private final ShortLinkActualRemoteService shortLinkActualRemoteService;
    private final UserService userService;

    @GetMapping("/api/short-link/admin/v1/system-settings")
    public Result<SystemSettingsRespDTO> getSystemSettings() {
        return Results.success(systemSettingsService.getSettings());
    }

    @PutMapping("/api/short-link/admin/v1/system-settings")
    public Result<Void> updateSystemSettings(@RequestBody SystemSettingsUpdateReqDTO requestParam) {
        systemSettingsService.updateSettings(requestParam);
        return Results.success();
    }

    @GetMapping("/api/short-link/admin/v1/overview")
    public Result<AdminOverviewRespDTO> overview() {
        Result<AdminShortLinkOverviewRespDTO> shortLinkOverviewResult = shortLinkActualRemoteService
                .adminShortLinkOverview();
        long totalUsers = userService.count(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getDelFlag, 0));
        long enabledUsers = userService.count(Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getDelFlag, 0)
                .eq(UserDO::getStatus, 0));
        AdminShortLinkOverviewRespDTO shortLinkOverview = shortLinkOverviewResult.getData();
        return Results.success(AdminOverviewRespDTO.builder()
                .totalLinks(shortLinkOverview.getTotalLinks())
                .totalClicks(shortLinkOverview.getTotalClicks())
                .totalVisitors(shortLinkOverview.getTotalVisitors())
                .totalUsers(totalUsers)
                .enabledUsers(enabledUsers)
                .build());
    }

    @GetMapping("/api/short-link/admin/v1/links/page")
    public Result<Page<ShortLinkPageRespDTO>> pageLinks(AdminShortLinkPageReqDTO requestParam) {
        return shortLinkActualRemoteService.adminPageShortLink(
                requestParam.getCurrent(),
                requestParam.getSize(),
                requestParam.getKeyword(),
                requestParam.getUsername(),
                requestParam.getSortField(),
                requestParam.getSortOrder());
    }
}