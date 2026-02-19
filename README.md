# rlink

rlink 是一个基于 Spring Boot 3、Spring Cloud 和 Maven 多模块组织的分布式短链接系统。

## 模块说明

- admin：管理后台服务，负责用户、分组、短链接与回收站等管理能力。
- project：核心短链接服务，负责短链生成、跳转、统计与消息处理。
- aggregation：聚合服务，用于统一装配管理端与核心服务能力。
- gateway：网关服务，负责请求路由与统一鉴权。
- console-vue：前端控制台。

## 技术栈

- Java 17
- Spring Boot 3
- Spring Cloud
- MyBatis Plus
- Redis / Redisson
- ShardingSphere
- Vue 3 + Vite

## 构建说明

项目采用 Maven 多模块结构组织。若本地未提供 Maven Wrapper，请直接使用本机 Maven 进行构建。

```bash
mvn -DskipTests compile
```

## 说明

仓库中保留 Apache License 2.0 相关许可证文件与必要头信息。其余与项目运行无关的宣传性文案、示例域名和推广信息已按当前项目命名进行清理或替换。
