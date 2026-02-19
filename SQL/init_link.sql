use link;

CREATE TABLE `t_link_0` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `domain` varchar(128) DEFAULT NULL COMMENT '域名',
    `short_uri` varchar(8) CHARACTER SET utf8 COLLATE utf8_bin DEFAULT NULL COMMENT '短链接',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `origin_url` varchar(1024) DEFAULT NULL COMMENT '原始链接',
    `click_num` int(11) DEFAULT '0' COMMENT '点击量',
    `username` varchar(256) DEFAULT NULL COMMENT '创建用户名',
    `favicon` varchar(256) DEFAULT NULL COMMENT '网站图标',
    `enable_status` tinyint(1) DEFAULT NULL COMMENT '启用标识 0：启用 1：未启用',
    `created_type` tinyint(1) DEFAULT NULL COMMENT '创建类型 0：接口创建 1：控制台创建',
    `valid_date_type` tinyint(1) DEFAULT NULL COMMENT '有效期类型 0：永久有效 1：自定义',
    `valid_date` datetime DEFAULT NULL COMMENT '有效期',
    `describe` varchar(1024) DEFAULT NULL COMMENT '描述',
    `total_pv` int(11) DEFAULT NULL COMMENT '历史PV',
    `total_uv` int(11) DEFAULT NULL COMMENT '历史UV',
    `total_uip` int(11) DEFAULT NULL COMMENT '历史UIP',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_time` bigint(20) DEFAULT '0' COMMENT '删除时间戳',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_full-short-url` (`full_short_url`, `del_time`) USING BTREE,
    KEY `idx_username` (`username`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_link_1` LIKE `t_link_0`;

CREATE TABLE `t_link_2` LIKE `t_link_0`;

CREATE TABLE `t_link_goto_0` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '短链接路径',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_full_short_url` (`full_short_url`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_link_goto_1` LIKE `t_link_goto_0`;

CREATE TABLE `t_link_goto_2` LIKE `t_link_goto_0`;

CREATE TABLE `t_link_access_logs` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `user` varchar(64) DEFAULT NULL COMMENT '用户信息',
    `browser` varchar(64) DEFAULT NULL COMMENT '浏览器',
    `os` varchar(64) DEFAULT NULL COMMENT '操作系统',
    `ip` varchar(64) DEFAULT NULL COMMENT 'IP',
    `network` varchar(64) DEFAULT NULL COMMENT '访问网络',
    `device` varchar(64) DEFAULT NULL COMMENT '访问设备',
    `locale` varchar(256) DEFAULT NULL COMMENT '地区',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    KEY `idx_full_short_url` (`full_short_url`) USING BTREE,
    KEY `idx_user` (`user`) USING BTREE,
    KEY `idx_ip` (`ip`) USING BTREE,
    KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_link_access_stats` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `date` date DEFAULT NULL COMMENT '日期',
    `pv` int(11) DEFAULT '0' COMMENT '访问量',
    `uv` int(11) DEFAULT '0' COMMENT '独立访客数',
    `uip` int(11) DEFAULT '0' COMMENT '独立IP数',
    `hour` int(11) DEFAULT NULL COMMENT '小时',
    `weekday` int(11) DEFAULT NULL COMMENT '星期',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_access_stats` (
        `full_short_url`,
        `date`,
        `hour`,
        `weekday`
    ) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_link_browser_stats` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `date` date DEFAULT NULL COMMENT '日期',
    `cnt` int(11) DEFAULT NULL COMMENT '访问量',
    `browser` varchar(64) DEFAULT NULL COMMENT '浏览器',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_browser_stats` (
        `full_short_url`,
        `date`,
        `browser`
    ) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_link_device_stats` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `date` date DEFAULT NULL COMMENT '日期',
    `cnt` int(11) DEFAULT NULL COMMENT '访问量',
    `device` varchar(64) DEFAULT NULL COMMENT '访问设备',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_device_stats` (
        `full_short_url`,
        `date`,
        `device`
    ) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_link_locale_stats` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `date` date DEFAULT NULL COMMENT '日期',
    `cnt` int(11) DEFAULT NULL COMMENT '访问量',
    `country` varchar(64) DEFAULT NULL COMMENT '国家标识',
    `province` varchar(64) DEFAULT NULL COMMENT '省份名称',
    `city` varchar(64) DEFAULT NULL COMMENT '城市名称',
    `adcode` varchar(64) DEFAULT NULL COMMENT '城市编码',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_locale_stats` (
        `full_short_url`,
        `date`,
        `country`,
        `province`,
        `city`,
        `adcode`
    ) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_link_network_stats` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `date` date DEFAULT NULL COMMENT '日期',
    `cnt` int(11) DEFAULT NULL COMMENT '访问量',
    `network` varchar(64) DEFAULT NULL COMMENT '访问网络',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_network_stats` (
        `full_short_url`,
        `date`,
        `network`
    ) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_link_os_stats` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',
    `date` date DEFAULT NULL COMMENT '日期',
    `cnt` int(11) DEFAULT NULL COMMENT '访问量',
    `os` varchar(64) DEFAULT NULL COMMENT '操作系统',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_os_stats` (
        `full_short_url`,
        `date`,
        `os`
    ) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_link_stats_today` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `full_short_url` varchar(128) DEFAULT NULL COMMENT '短链接',
    `date` date DEFAULT NULL COMMENT '日期',
    `today_pv` int(11) DEFAULT '0' COMMENT '今日PV',
    `today_uv` int(11) DEFAULT '0' COMMENT '今日UV',
    `today_uip` int(11) DEFAULT '0' COMMENT '今日IP数',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_today_stats` (`full_short_url`, `date`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_user_0` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `username` varchar(256) DEFAULT NULL COMMENT '用户名',
    `password` varchar(512) DEFAULT NULL COMMENT '密码',
    `real_name` varchar(256) DEFAULT NULL COMMENT '真实姓名',
    `phone` varchar(128) DEFAULT NULL COMMENT '手机号',
    `mail` varchar(512) DEFAULT NULL COMMENT '邮箱',
    `role` varchar(32) DEFAULT 'user' COMMENT '用户角色：admin / user / guest',
    `status` tinyint(1) DEFAULT '0' COMMENT '用户状态 0：启用 1：封禁',
    `deletion_time` bigint(20) DEFAULT NULL COMMENT '注销时间戳',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_username` (`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1716344307570487299 DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_user_1` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `username` varchar(256) DEFAULT NULL COMMENT '用户名',
    `password` varchar(512) DEFAULT NULL COMMENT '密码',
    `real_name` varchar(256) DEFAULT NULL COMMENT '真实姓名',
    `phone` varchar(128) DEFAULT NULL COMMENT '手机号',
    `mail` varchar(512) DEFAULT NULL COMMENT '邮箱',
    `role` varchar(32) DEFAULT 'user' COMMENT '用户角色：admin / user / guest',
    `status` tinyint(1) DEFAULT '0' COMMENT '用户状态 0：启用 1：封禁',
    `deletion_time` bigint(20) DEFAULT NULL COMMENT '注销时间戳',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_username` (`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1726253659068588035 DEFAULT CHARSET = utf8mb4;

CREATE TABLE `t_user_2` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `username` varchar(256) DEFAULT NULL COMMENT '用户名',
    `password` varchar(512) DEFAULT NULL COMMENT '密码',
    `real_name` varchar(256) DEFAULT NULL COMMENT '真实姓名',
    `phone` varchar(128) DEFAULT NULL COMMENT '手机号',
    `mail` varchar(512) DEFAULT NULL COMMENT '邮箱',
    `role` varchar(32) DEFAULT 'user' COMMENT '用户角色：admin / user / guest',
    `status` tinyint(1) DEFAULT '0' COMMENT '用户状态 0：启用 1：封禁',
    `deletion_time` bigint(20) DEFAULT NULL COMMENT '注销时间戳',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_username` (`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1726260205890691074 DEFAULT CHARSET = utf8mb4;
