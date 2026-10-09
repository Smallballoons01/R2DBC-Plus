# Changelog

本文件记录 R2DBC-Plus 的重要变更。
格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

## [1.1.0] - 2026-10-09

首个可用版本。修复了大量导致项目**无法被他人使用**的问题。

### 修复

- **构建**：移除私有父 POM `com.scrm:common-reactor`，此前任何人 clone 后均无法编译
- **构建**：源码目录从 `com.scrm.r2dbc.plus/` 修正为标准包路径 `com/scrm/r2dbc/plus/`
- **构建**：移除 surefire 的 `skipTests=true`，测试首次真正参与构建
- **自动配置**：`R2dbcPlusAutoConfiguration` 此前使用 `@Configuration` 且未注册到任何清单，
  导致自动配置从未生效；现改为 `@AutoConfiguration` 并补充 `AutoConfiguration.imports`
- **多数据源**：`MapperProxyFactory` 会用新建的 `XmlSqlExecutor` 覆盖外部注入的实例，
  导致 XML Mapper 走错数据源；现优先使用注入的实例
- **并发安全**：`MapperProxyFactory` 的代理缓存为单例 Bean 共享的 `HashMap`，
  在响应式多线程下并发 `computeIfAbsent` 可能破坏内部结构导致 CPU 打满；改为 `ConcurrentHashMap`
- **并发安全**：`R2dbcDataSourceRegistry` 的数据源映射同样改为 `ConcurrentHashMap`
- **主键生成**：`FieldFillProcessor.generateId` 遗漏 `IdType.NONE` / `INPUT` 分支，
  被`default -> null` 掩盖，会静默丢失主键；现补齐并去掉默认值兜底
- **装配**：`MapperProxyFactory` / `R2dbcPlusFactory` / `BaseMapperImpl` 混用
  `@Autowired` 字段注入与反射写私有字段；改为构造器注入与显式装配方法
- **装配**：移除 `MapperProxyFactory` 与 `R2dbcPlusFactory` 上的 `@Component`，
  避免与自动配置重复注册
- **扫描器**：`R2dbcPlusMapperScannerConfigurer` 作为 starter 组件带 `@Component`，
  会强制实例化整个应用上下文；改由自动配置 `@Import` 精确引入
- **扫描器**：修复未配置 `@MapperScan` 时 `basePackages` 为 `null` 的 NPE
- **分页**：`BaseMapperImpl.selectPage` 未做上限保护（仅 `ServiceImpl` 有），
  现统一由 `Page` 自身规范化
- **测试**：测试硬编码内网 MySQL 地址与明文密码，改为默认 H2 内存库

### 新增

- 类型安全的配置类 `R2dbcPlusProperties`（分页上限、批量大小、超时等）
- `Page` 分页参数防御性规范化：非法 `size` / `current` 自动纠正，超限自动截断
- 兼容性矩阵 CI：Spring Boot 3.5.x（JDK 17/21/25）与 Spring Boot 4.1.x（JDK 17/21）双线验证
- `LICENSE`(MIT)、`.gitignore`、Issue 与 PR 模板
- 并发安全回归测试与分页规范化测试（共 12 个）

### 变更

- 最低 JDK 要求由文档标注的 8 更正为实际的 **17**（代码使用了 switch 表达式）
- Spring Boot 默认版本 3.5.16，可通过 `-Dspring-boot.version=4.1.1` 切换验证
- 使用 `maven.compiler.release` 替代 `source`/`target`，避免高版本 JDK 误用高版本 API
- 异常信息由英文改为中文，并附带可操作的排查建议

### 安全

- 移除测试配置中的明文数据库密码，改由环境变量注入
- `.gitignore` 增加凭据类文件的忽略规则

[Unreleased]: https://github.com/Smallballoons01/R2DBC-Plus/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/Smallballoons01/R2DBC-Plus/releases/tag/v1.1.0