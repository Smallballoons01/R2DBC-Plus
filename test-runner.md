# R2DBC Plus 测试运行器

## 快速测试命令

### 一键启动测试套件

```bash
# 1. 全部测试（完整验证）
mvn test -Dtest=AllTestSuite

# 2. 核心功能测试（日常开发）
mvn test -Dtest=CoreTestSuite

# 3. 快速测试（CI/CD）
mvn test -Dtest=QuickTestSuite

# 4. 慢速测试（性能验证）
mvn test -Dtest=SlowTestSuite
```

### 按功能模块测试

```bash
# 注解功能测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.annotation.**"

# CRUD 操作测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.functional.crud.**"

# 字段映射测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.functional.field.**"

# 逻辑删除测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.functional.logic.**"

# 查询包装器测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.functional.wrapper.**"

# XML 功能测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.xml.**"

# 单元测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.unit.**"

# 集成测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.integration.**"

# 性能测试
mvn test -Dtest="com.scrm.r2dbc.plus.test.performance.**"
```

### 按标签测试

```bash
# 核心功能
mvn test -Dgroups=core

# 快速测试
mvn test -Dgroups=fast

# 单元测试
mvn test -Dgroups=unit

# 集成测试
mvn test -Dgroups=integration

# 排除慢速测试
mvn test -DexcludedGroups=slow
```

## 测试验证清单

### ✅ 基础功能验证
- [ ] 注解功能测试通过
- [ ] CRUD 操作测试通过
- [ ] 字段映射测试通过
- [ ] 逻辑删除测试通过
- [ ] 查询包装器测试通过

### ✅ 高级功能验证
- [ ] XML 映射测试通过
- [ ] 性能测试通过
- [ ] 集成测试通过
- [ ] 兼容性测试通过

### ✅ 测试套件验证
- [ ] AllTestSuite 运行成功
- [ ] CoreTestSuite 运行成功
- [ ] QuickTestSuite 运行成功
- [ ] SlowTestSuite 运行成功

## 故障排查

### 如果测试失败：

1. **检查数据库连接**
   ```bash
   # 确保 MySQL 服务运行
   # 检查 application-mysql.yml 配置
   ```

2. **检查依赖**
   ```bash
   mvn clean compile
   ```

3. **单独运行失败的测试**
   ```bash
   mvn test -Dtest=SpecificTestClass#specificTestMethod
   ```

4. **查看详细日志**
   ```bash
   mvn test -Dtest=TestClass -X
   ```

## 开发建议

### 新功能开发流程：
1. 编写测试 → 2. 实现功能 → 3. 运行测试 → 4. 重构优化

### 测试运行频率：
- **开发时**：运行 QuickTestSuite
- **提交前**：运行 CoreTestSuite  
- **发布前**：运行 AllTestSuite
- **定期**：运行 SlowTestSuite
