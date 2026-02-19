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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rlink.admin.common.convention.result.Result;
import com.rlink.admin.remote.ShortLinkActualRemoteService;
import com.rlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.rlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.rlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * URL 回收站接口实现层
 */
@Service(value = "recycleBinServiceImplByAdmin")
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    @Override
    public Result<Page<ShortLinkPageRespDTO>> pageRecycleBinShortLink(ShortLinkRecycleBinPageReqDTO requestParam) {
        return shortLinkActualRemoteService.pageRecycleBinShortLink(requestParam.getCurrent(), requestParam.getSize());
    }
}
