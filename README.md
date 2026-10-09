# R2DBC Plus

<p>
    <a href="https://maven.apache.org/"><img src="https://img.shields.io/badge/Maven-3.6+-orange.svg" alt="Maven"></a>
    <a href="https://www.oracle.com/java/technologies/downloads/"><img src="https://img.shields.io/badge/JDK-17+-green.svg" alt="JDK"></a>
    <a href="https://spring.io/projects/spring-data-r2dbc"><img src="https://img.shields.io/badge/Spring%20Data%20R2DBC-3.x-blue.svg" alt="Spring Data R2DBC"></a>
    <a href="https://github.com/ReactiveIO/r2dbc-mysql"><img src="https://img.shields.io/badge/R2DBC%20MySQL-Support-blue.svg" alt="R2DBC MySQL"></a>
    <a href="https://github.com/Smallballoons01/R2DBC-Plus/actions/workflows/ci.yml"><img src="https://github.com/Smallballoons01/R2DBC-Plus/actions/workflows/ci.yml/badge.svg" alt="CI"></a>
    <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-red.svg" alt="License"></a>
</p>

R2DBC Plus 是一个响应式 R2DBC ORM 增强框架，灵感来源于 MyBatis-Plus，旨在为响应式 Spring 应用提供便捷的数据库访问能力。

## 特性

- **CRUD 操作**：内置通用 Mapper，支持单表 CRUD 操作，无需编写 SQL
- **Lambda 表达式**：使用 Lambda 表达式构建查询条件，类型安全
- **条件构造器**：强大的 QueryWrapper 和 UpdateWrapper，支持复杂查询
- **XML 映射**：支持 XML 映射文件，复杂 SQL 轻松实现
- **自动填充**：字段自动填充（创建时间、更新时间等）
- **逻辑删除**：内置逻辑删除支持，优雅实现软删除
- **乐观锁**：版本号机制，防止并发更新冲突
- **分页查询**：内置分页插件，支持高效分页查询
- **多数据源**：支持多数据源配置
- **响应式**：完全基于 Project Reactor，支持响应式编程

## 技术栈

- Java 17+（支持到 JDK 25）
- Spring Boot 3.5.x（默认）/ 4.1.x（已验证兼容）
- Spring Data R2DBC 3.5+ / 4.1+
- R2DBC MySQL Driver
- Project Reactor

## 兼容性矩阵

本项目在 CI 中对下列组合持续构建验证：

| Spring Boot | JDK | 状态 |
|-------------|-----|------|
| 3.5.x | 17 / 21 / 25 | ✅ 主支持 |
| 4.1.x（Spring Framework 7） | 17 / 21 | ✅ 兼容验证 |

本地可复现验证：

```bash
mvn clean verify                                 # 默认（Spring Boot 3.5.x）
mvn clean verify -Dspring-boot.version=4.1.1     # 验证 Spring Boot 4
```

> 编译使用 `maven.compiler.release=17`，因此在高版本 JDK 上构建出的字节码
> 仍只链接 Java 17 API，不会出现「编译通过但运行期 NoSuchMethodError」的情况。

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.scrm</groupId>
    <artifactId>r2dbc-plus</artifactId>
    <version>1.1.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置

框架通过 `AutoConfiguration.imports` 自动装配，引入依赖即生效，无需额外声明 `@Import`。

如需扫描自定义 Mapper，在启动类上添加 `@MapperScan`：

```java
@SpringBootApplication
@MapperScan("com.example.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

可选配置（在 `application.yml` 中）：

```yaml
r2dbc-plus:
  # XML 映射文件位置
  mapper-locations: classpath*:mapper/**/*.xml
  # 关闭 XML 解析（纯注解方式使用时可提速）
  xml-enabled: true
  # 分页单页最大条数保护
  max-page-size: 500
```

### 3. 定义实体类

```java
@Data
@TableName("user")
public class User {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;
    
    private Integer age;
    
    private String email;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
    
    @Version
    private Integer version;
}
```

### 4. 定义 Mapper

```java
public interface UserMapper extends BaseMapper<User> {
}
```

### 5. 使用

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    
    // 插入
    public Mono<User> addUser(User user) {
        return userMapper.insert(user).thenReturn(user);
    }
    
    // 根据ID查询
    public Mono<User> getUserById(Long id) {
        return userMapper.selectById(id);
    }
    
    // 条件查询
    public Flux<User> getUsersByName(String name) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>(User.class);
        wrapper.eq(User::getName, name);
        return userMapper.selectList(wrapper);
    }
    
    // 分页查询
    public Mono<Page<User>> getUserPage(int current, int size) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>(User.class);
        wrapper.orderByDesc(User::getCreateTime);
        return userMapper.selectPage(page, wrapper);
    }
    
    // 更新
    public Mono<Integer> updateUser(User user) {
        return userMapper.updateById(user);
    }
    
    // 删除
    public Mono<Integer> deleteUser(Long id) {
        return userMapper.deleteById(id);
    }
}
```

## 核心功能

### 注解

| 注解 | 说明 |
|------|------|
| `@TableName` | 实体类与数据库表映射 |
| `@TableId` | 主键字段标识 |
| `@TableField` | 普通字段映射 |
| `@TableLogic` | 逻辑删除字段 |
| `@Version` | 乐观锁版本字段 |
| `@Mapper` | Mapper 接口标识 |
| `@MapperScan` | Mapper 扫描配置 |

### BaseMapper 核心方法

```java
public interface BaseMapper<T> {
    // 插入
    Mono<Integer> insert(T entity);
    Mono<Integer> insertBatch(Collection<T> entityList);
    Mono<Integer> insertBatch(Collection<T> entityList, int batchSize);
    
    // 更新
    Mono<Integer> updateById(T entity);
    Mono<Integer> update(T entity, Wrapper<T> updateWrapper);
    Mono<Integer> updateBatchById(Collection<T> entityList);
    
    // 删除
    Mono<Integer> deleteById(Serializable id);
    Mono<Integer> deleteBatchIds(Collection<? extends Serializable> idList);
    Mono<Integer> delete(Wrapper<T> queryWrapper);
    
    // 查询
    Mono<T> selectById(Serializable id);
    Flux<T> selectBatchIds(Collection<? extends Serializable> idList);
    Mono<T> selectOne(Wrapper<T> queryWrapper);
    Mono<Long> selectCount(Wrapper<T> queryWrapper);
    Flux<T> selectList(Wrapper<T> queryWrapper);
    Flux<T> selectList();
    Mono<Boolean> exists(Wrapper<T> queryWrapper);
    Mono<Page<T>> selectPage(Page<T> page, Wrapper<T> queryWrapper);
}
```

### Lambda 条件构造器

```java
// 查询构造
LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>(User.class);
wrapper
    .eq(User::getName, "张三")           // 等于
    .ne(User::getAge, 20)               // 不等于
    .gt(User::getAge, 18)               // 大于
    .ge(User::getAge, 18)               // 大于等于
    .lt(User::getAge, 30)               // 小于
    .le(User::getAge, 30)               // 小于等于
    .like(User::getName, "张")          // 模糊匹配
    .likeLeft(User::getName, "张")      // 左模糊
    .likeRight(User::getName, "张")     // 右模糊
    .in(User::getId, Arrays.asList(1,2,3))  // IN查询
    .between(User::getAge, 18, 30)      // 范围查询
    .orderByDesc(User::getCreateTime)   // 排序
    .select(User::getId, User::getName); // 指定查询字段
Flux<User> users = userMapper.selectList(wrapper);

// 更新构造
LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>(User.class);
updateWrapper
    .set(User::getName, "李四")
    .set(User::getAge, 25)
    .eq(User::getId, 1);
Mono<Integer> result = userMapper.update(null, updateWrapper);
```

### XML 映射

在 `resources/mapper/` 目录下创建 XML 文件：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.mapper.UserMapper">
    
    <select id="selectCustomPage" resultType="com.example.entity.User">
        SELECT * FROM user 
        WHERE deleted = 0
        <if test="name != null and name != ''">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
        ORDER BY create_time DESC
        LIMIT #{offset}, #{size}
    </select>
    
    <select id="countByCondition" resultType="java.lang.Long">
        SELECT COUNT(*) FROM user 
        WHERE deleted = 0
        <if test="name != null and name != ''">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
    </select>
    
</mapper>
```

在 Mapper 接口中定义方法：

```java
public interface UserMapper extends BaseMapper<User> {
    
    Mono<Page<User>> selectCustomPage(Page<User> page, String name);
    
    Mono<Long> countByCondition(String name);
}
```

## 字段填充

实现 `MetaObjectHandler` 接口：

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
```

## 配置说明

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `r2dbc-plus.mapper-locations` | XML 映射文件位置 | `classpath*:mapper/**/*.xml` |
| `r2dbc-plus.xml-enabled` | 是否在启动时解析 XML | `true` |
| `r2dbc-plus.table-prefix` | 全局表前缀 | 无 |
| `r2dbc-plus.column-prefix` | 全局列前缀 | 无 |
| `r2dbc-plus.max-page-size` | 分页单页最大条数 | `500` |
| `r2dbc-plus.insert-batch-size` | 单次批量插入上限 | `1000` |
| `r2dbc-plus.default-batch-size` | 默认分批数量 | `1000` |
| `r2dbc-plus.query-timeout` | 操作超时，如 `5s` | 无限制 |

## 项目结构

```
src/main/java/com/scrm/r2dbc/plus
├── annotation/          # 注解定义
├── conditions/          # 条件构造器（query/update 子包）
├── config/              # 自动配置与 Mapper 扫描
├── enums/               # 枚举定义
├── factory/             # 运行时工厂
├── fill/                # 字段填充
├── function/            # Lambda 方法引用
├── generator/           # ID生成器
├── logic/               # 逻辑删除
├── mapper/              # Mapper 接口与实现
├── page/                # 分页
├── proxy/               # 动态代理
├── service/             # 服务基类
├── util/                # 工具类
├── version/             # 乐观锁
└── xml/                 # XML 映射解析与执行
```

自动配置入口位于
`src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。

## 构建与测试

```bash
# 编译
mvn clean compile

# 运行全部测试（默认使用 H2 内存库，无需任何外部依赖）
mvn clean verify

# 只跑快速测试
mvn test -Dgroups=fast

# 用真实 MySQL 跑测试（需自行准备表结构与数据）
export R2DBC_TEST_URL=r2dbc:mysql://localhost:3306/test_db
export R2DBC_TEST_USER=root
export R2DBC_TEST_PASSWORD=secret
mvn test -Dspring.profiles.active=mysql
```

## 常见问题排查

### 报 `NoSuchBeanDefinitionException: 找不到 XxxMapper 的 Bean`

绝大多数情况是缺少 `@MapperScan`。框架在这种情况下会打印一条 WARN 提示，
请在启动类上添加：

```java
@SpringBootApplication
@MapperScan("com.example.mapper")   // 指向 Mapper 接口所在包
public class Application { }
```

若日志提示「在包 xxx 下未找到带 @Mapper 注解的接口」，请检查：
1. Mapper 接口是否标注了 `@Mapper`
2. `basePackages` 是否指向正确的包路径

### 报 `NoDialectException: Cannot determine a dialect`

当前 R2DBC 连接类型没有被 Spring Data R2DBC 内置方言识别。
本项目已内置 ClickHouse 方言；其他数据库（如 MariaDB 变体、部分国产库）
需要自行实现 `R2dbcDialectProvider` 并通过 `spring.factories` 注册。

### 字段自动填充没生效

1. 确认实体字段标注了 `@TableField(fill = FieldFill.INSERT / INSERT_UPDATE)`
2. 确认实现了 `MetaObjectHandler` 并注册为 Bean
3. `defaultFill` 类中建议用 `strictInsertFill` / `strictUpdateFill`

### 逻辑删除没生效

检查字段是否标注了 `@TableLogic`，且对应列确实存在于数据库中。

### 批量插入返回的主键是 null

`IdType.AUTO` 依赖数据库自增，需确认：
1. 数据库列已设置自增（AUTO_INCREMENT / IDENTITY）
2. 驱动支持 `RETURNING` 或可读取自增值

若数据库不支持，改用 `IdType.ASSIGN_ID`（雪花算法）或 `ASSIGN_UUID`。

### 分页 size 被自动截断

为避免 `size` 过大拖垮数据库，默认单页上限为 500。
可在配置中调整或放开：

```yaml
r2dbc-plus:
  max-page-size: 1000
```

或在代码中 `page.setMaxPageSize(0)` 表示不限制。

### 启动报 `FieldFillProcessor` 或其他 Bean 装配失败

框架已全面采用构造器注入。若你扩展了框架内部类，
请不要使用反射写私有字段——参见 [CONTRIBUTING.md](./CONTRIBUTING.md) 的约定。

## 注意事项

1. 本框架仅支持响应式 Spring Boot 应用（WebFlux 或 R2DBC）
2. 需要配置 R2DBC 数据库连接
3. 逻辑删除字段需要在实体类中用 `@TableLogic` 标注
4. 乐观锁字段需要在实体类中用 `@Version` 标注
5. 内置 ClickHouse 方言（复用 Postgres 方言的 SQL 映射），复杂 ClickHouse 语法建议自定义方言
6. 框架内部的可变状态均使用并发容器，但自定义扩展请遵守同样的线程安全约定

## 参与贡献

请阅读 [CONTRIBUTING.md](./CONTRIBUTING.md)。本地跑通 `mvn clean verify` 后再提交，
可以避免 CI 上出现环境相关失败。

## 更新日志

见 [CHANGELOG.md](./CHANGELOG.md)。

## 许可证

[MIT License](./LICENSE)