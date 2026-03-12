# R2DBC Plus 测试套件

## 概述

R2DBC Plus 测试套件提供了完整的测试覆盖，包括单元测试、集成测试、功能测试、性能测试等。测试用例按功能模块组织，支持一键启动不同类型的测试。

## 测试结构

```
test/
├── suites/                    # 测试套件（一键启动入口）
│   ├── AllTestSuite.java     # 全部测试套件
│   ├── CoreTestSuite.java    # 核心功能测试套件
│   ├── QuickTestSuite.java   # 快速测试套件
│   └── SlowTestSuite.java    # 慢速测试套件
├── functional/               # 功能测试
│   ├── crud/                 # CRUD操作测试
│   ├── wrapper/              # 查询包装器测试
│   ├── logic/                # 逻辑删除测试
│   └── field/                # 字段映射测试
├── annotation/               # 注解测试
├── unit/                     # 单元测试
├── integration/              # 集成测试
├── performance/              # 性能测试
├── compatibility/            # 兼容性测试
└── xml/                      # XML测试
```

## 测试标签

测试用例使用 JUnit 5 的 `@Tag` 注解进行分类：

- `@Tag("core")` - 核心功能测试
- `@Tag("fast")` - 快速测试（通常 < 5秒）
- `@Tag("slow")` - 慢速测试（通常 > 5秒）
- `@Tag("unit")` - 单元测试
- `@Tag("integration")` - 集成测试
- `@Tag("functional")` - 功能测试
- `@Tag("annotation")` - 注解功能测试
- `@Tag("xml")` - XML 相关测试
- `@Tag("performance")` - 性能测试
- `@Tag("compatibility")` - 兼容性测试

## 一键启动测试

### 1. 全部测试
```bash
mvn test -Dtest=AllTestSuite
```
运行所有测试用例，完整验证框架功能。

### 2. 核心功能测试
```bash
mvn test -Dtest=CoreTestSuite
```
运行核心功能测试，适合日常开发验证。

### 3. 快速测试
```bash
mvn test -Dtest=QuickTestSuite
```
运行快速测试，适合 CI/CD 流水线。

### 4. 慢速测试
```bash
mvn test -Dtest=SlowTestSuite
```
运行性能测试等耗时测试。

## 按标签运行测试

### 运行特定标签的测试
```bash
# 运行核心功能测试
mvn test -Dgroups=core

# 运行快速测试
mvn test -Dgroups=fast

# 运行单元测试
mvn test -Dgroups=unit

# 运行集成测试
mvn test -Dgroups=integration

# 运行性能测试
mvn test -Dgroups=performance
```

### 排除特定标签的测试
```bash
# 排除慢速测试
mvn test -DexcludedGroups=slow

# 排除性能测试
mvn test -DexcludedGroups=performance
```

### 组合标签
```bash
# 运行核心且快速的测试
mvn test -Dgroups="core & fast"

# 运行功能测试但排除慢速测试
mvn test -Dgroups=functional -DexcludedGroups=slow
```

## 按包运行测试

### 运行特定包的测试
```bash
# 运行功能测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.functional.**"

# 运行注解测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.annotation.**"

# 运行单元测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.unit.**"

# 运行集成测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.integration.**"
```

## 测试配置

### 测试环境配置
测试使用 `@ActiveProfiles("mysql")` 配置，确保：
1. 数据库连接配置正确
2. 测试数据库可用
3. 必要的测试表已创建

### 测试数据
- 测试使用独立的测试数据库
- 每个测试方法使用唯一的测试数据
- 测试数据自动清理

## 测试最佳实践

### 1. 编写新测试
- 选择合适的测试分类包
- 添加适当的 `@Tag` 注解
- 使用描述性的测试方法名
- 确保测试独立性

### 2. 测试命名规范
- 测试类：`XxxTest.java`
- 测试方法：`testXxx()` 或 `shouldXxxWhenXxx()`
- 显示名称：使用 `@DisplayName` 提供清晰描述

### 3. 测试组织
- 相关测试放在同一个包中
- 使用 `@Nested` 组织复杂测试
- 合理使用 `@BeforeEach` 和 `@AfterEach`

## 持续集成

### CI/CD 推荐配置
```yaml
# 快速验证（PR检查）
- name: Quick Tests
  run: mvn test -Dtest=QuickTestSuite

# 完整验证（主分支）
- name: Full Tests
  run: mvn test -Dtest=AllTestSuite

# 性能回归测试（定期执行）
- name: Performance Tests
  run: mvn test -Dtest=SlowTestSuite
```

## 故障排查

### 常见问题
1. **数据库连接失败**：检查测试配置文件中的数据库连接信息
2. **测试数据冲突**：确保测试使用唯一的测试数据
3. **依赖注入失败**：检查 Spring 配置和组件扫描

### 调试技巧
- 使用 `@Disabled` 临时禁用测试
- 使用 `@EnabledIf` 条件执行测试
- 查看测试日志输出
- 使用断点调试

## 贡献指南

### 添加新测试
1. 确定测试分类和包位置
2. 创建测试类并添加适当标签
3. 编写测试方法
4. 更新相关文档
5. 验证测试套件正常运行

### 修改现有测试
1. 保持测试的独立性
2. 更新相关标签
3. 确保不影响其他测试
4. 运行相关测试套件验证
