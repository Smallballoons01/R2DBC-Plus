## 贡献指南

感谢参与 R2DBC-Plus。

### 环境要求

- JDK 17 或更高（支持到 25+）
- Maven 3.6+
- 无需本地数据库：测试默认使用 H2 内存库

### 开发流程

```bash
git clone https://github.com/Smallballoons01/R2DBC-Plus.git
cd R2DBC-Plus

# 编译与全量测试
mvn clean verify

# 只跑快速测试
mvn test -Dgroups=fast

# 验证 Spring Boot 4.x 兼容性
mvn clean verify -Dspring-boot.version=4.1.1
```

### 提交前请确认

- [ ] `mvn clean verify` 通过
- [ ] 新增功能附带测试
- [ ] 提交信息遵循 [Conventional Commits](https://www.conventionalcommits.org/zh-hans/)
- [ ] 不提交任何凭据（数据库密码、Token 等）

### 提交信息格式

```
<type>(<scope>): <subject>

<body>

<footer>
```

常用 type：`feat` / `fix` / `docs` / `refactor` / `test` / `perf` / `chore`

### 重要约定

- **不要设置 `skipTests=true`**：PR 的测试必须真实执行
- **不要用 `@Autowired(required = false)` 掩盖装配失败**：这会让真正的错误被静默吞掉
- **避免反射写私有字段**：优先用构造器注入或显式装配方法
- **注意线程安全**：响应式应用是多线程的，单例 Bean 中的可变状态必须用并发容器
- **新增单例 Bean 的可变字段请设为 `final`**：保证安全发布