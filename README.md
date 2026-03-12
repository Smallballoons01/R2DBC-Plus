# R2DBC Plus

<p>
    <a href="https://maven.apache.org/"><img src="https://img.shields.io/badge/Maven-3.6+-orange.svg" alt="Maven"></a>
    <a href="https://www.oracle.com/java/technologies/downloads/"><img src="https://img.shields.io/badge/JDK-8+-green.svg" alt="JDK"></a>
    <a href="https://spring.io/projects/spring-data-r2dbc"><img src="https://img.shields.io/badge/Spring Data R2DBC-3.x-blue.svg" alt="Spring Data R2DBC"></a>
    <a href="https://github.com/ReactiveIO/r2dbc-mysql"><img src="https://img.shields.io/badge/R2DBC MySQL-Support-blue.svg" alt="R2DBC MySQL"></a>
    <a href="https://opensource.org/licenses/MIT"><img src="https://img.shields.io/badge/License-MIT-red.svg" alt="License"></a>
</p>

> English | [中文](./README.md)

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

- Java 8+
- Spring Boot 3.x
- Spring Data R2DBC
- R2DBC MySQL Driver
- Project Reactor

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.scrm</groupId>
    <artifactId>r2dbc-plus</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 配置

在 Spring Boot 启动类上添加 MapperScan 注解：

```java
@SpringBootApplication
@MapperScan("com.example.mapper")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

或在配置文件中添加自动配置：

```yaml
r2dbc-plus:
  mapper-locations: classpath*:mapper/**/*.xml
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
| `r2dbc-plus.table-prefix` | 全局表前缀 | 无 |
| `r2dbc-plus.column-prefix` | 全局列前缀 | 无 |

## 项目结构

```
r2dbc-plus
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.scrm.r2dbc.plus
│   │   │       ├── annotation/          # 注解定义
│   │   │       ├── conditions/          # 条件构造器
│   │   │       ├── config/              # 自动配置
│   │   │       ├── enums/               # 枚举定义
│   │   │       ├── fill/                # 字段填充
│   │   │       ├── generator/           # ID生成器
│   │   │       ├── logic/               # 逻辑删除
│   │   │       ├── mapper/              # Mapper接口
│   │   │       ├── page/                # 分页
│   │   │       ├── proxy/               # 代理工厂
│   │   │       ├── service/             # 服务基类
│   │   │       ├── util/                # 工具类
│   │   │       ├── version/             # 乐观锁
│   │   │       └── xml/                 # XML解析
│   │   └── resources/
│   └── test                              # 测试代码
├── pom.xml
└── README.md
```

## 测试运行

```bash
# 运行所有测试
mvn test -Dtest=AllTestSuite

# 运行核心功能测试
mvn test -Dtest=CoreTestSuite

# 运行快速测试
mvn test -Dtest=QuickTestSuite

# 运行特定测试类
mvn test -Dtest=com.scrm.r2dbc.plus.test.annotation.AnnotationTest
```

详细测试说明请参考 [test-runner.md](./test-runner.md)

## 注意事项

1. 本框架仅支持响应式 Spring Boot 应用
2. 需要配置 R2DBC 数据库连接
3. 逻辑删除字段需要在实体类中用 `@TableLogic` 标注
4. 乐观锁字段需要在实体类中用 `@Version` 标注

## 参与贡献

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License