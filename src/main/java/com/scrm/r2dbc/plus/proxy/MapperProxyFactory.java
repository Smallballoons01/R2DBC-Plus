package com.scrm.r2dbc.plus.proxy;

import com.scrm.r2dbc.plus.annotation.*;
import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.mapper.impl.BaseMapperImpl;
import com.scrm.r2dbc.plus.page.Page;
import com.scrm.r2dbc.plus.xml.XmlSqlExecutor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Mapper 代理工厂，用于创建 Mapper 接口的代理实现
 * 
 * @author dason
 */
@Component
public class MapperProxyFactory {

    private final XmlSqlExecutor xmlSqlExecutor;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final DatabaseClient databaseClient;
    private final FieldFillProcessor fieldFillProcessor;
    private final LogicDeleteProcessor logicDeleteProcessor;

    private final Map<Class<?>, Object> mapperCache = new HashMap<>();
    
    // 用于多数据源的构造方法
    private final R2dbcEntityTemplate customEntityTemplate;
    private final DatabaseClient customDatabaseClient;
    
    /**
     * 默认构造方法 - 使用自动注入的组件
     */
    public MapperProxyFactory() {
        this.customEntityTemplate = null;
        this.customDatabaseClient = null;
        this.xmlSqlExecutor = null;
        this.r2dbcEntityTemplate = null;
        this.databaseClient = null;
        this.fieldFillProcessor = null;
        this.logicDeleteProcessor = null;
    }


    /**
     * 多数据源构造方法 - 使用指定的数据源组件、字段填充处理器和逻辑删除处理器
     */
    public MapperProxyFactory(R2dbcEntityTemplate entityTemplate, DatabaseClient databaseClient,
                             FieldFillProcessor fieldFillProcessor,
                              XmlSqlExecutor xmlSqlExecutor,
                              LogicDeleteProcessor logicDeleteProcessor) {
        this.customEntityTemplate = entityTemplate;
        this.customDatabaseClient = databaseClient;
        this.fieldFillProcessor = fieldFillProcessor;
        this.xmlSqlExecutor = xmlSqlExecutor;
        this.logicDeleteProcessor = logicDeleteProcessor;
        this.r2dbcEntityTemplate = entityTemplate;
        this.databaseClient = databaseClient;
    }


    /**
     * 创建 Mapper 代理实例
     */
    @SuppressWarnings("unchecked")
    public <T> T createMapperProxy(Class<T> mapperInterface) {
        return (T) mapperCache.computeIfAbsent(mapperInterface, this::doCreateMapperProxy);
    }

    /**
     * 实际创建代理对象
     */
    private Object doCreateMapperProxy(Class<?> mapperInterface) {
        // 获取实体类型
        Class<?> entityClass = getEntityClass(mapperInterface);

        // 选择正确的数据源组件
        R2dbcEntityTemplate entityTemplate = (customEntityTemplate != null) ? customEntityTemplate : r2dbcEntityTemplate;
        DatabaseClient dbClient = (customDatabaseClient != null) ? customDatabaseClient : databaseClient;

        // 创建 BaseMapper 实现
        BaseMapperImpl<?> baseMapperImpl = new BaseMapperImpl<>(entityClass, entityTemplate, dbClient);

        // 手动注入 fieldFillProcessor
        injectFieldFillProcessor(baseMapperImpl);

        // XML 执行器：优先使用外部注入的实例（含多数据源场景与测试替身），
        // 只有在未注入时才按当前数据源现场创建
        XmlSqlExecutor executor = this.xmlSqlExecutor;
        if (executor == null) {
            executor = new XmlSqlExecutor(dbClient, entityTemplate);
        }

        return Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class[]{mapperInterface},
                new MapperInvocationHandler(mapperInterface, baseMapperImpl, executor)
        );
    }

    /**
     * 从接口泛型中获取实体类型
     */
    private Class<?> getEntityClass(Class<?> mapperInterface) {
        ParameterizedType parameterizedType = (ParameterizedType) mapperInterface.getGenericInterfaces()[0];
        java.lang.reflect.Type actualType = parameterizedType.getActualTypeArguments()[0];

        // 处理不同类型的泛型参数
        if (actualType instanceof Class) {
            // 简单类型，如 BaseMapper<User>
            return (Class<?>) actualType;
        } else if (actualType instanceof ParameterizedType) {
            // 参数化类型，如 BaseMapper<Map<String, Object>>
            ParameterizedType paramType = (ParameterizedType) actualType;
            return (Class<?>) paramType.getRawType();
        } else {
            // 其他类型，抛出异常
            throw new UnsupportedOperationException("Unsupported generic type: " + actualType + " for mapper interface: " + mapperInterface.getName());
        }
    }

    /**
     * Mapper 调用处理器
     */
    private static class MapperInvocationHandler implements InvocationHandler {
        private final Class<?> mapperInterface;
        private final BaseMapperImpl<?> baseMapperImpl;
        private final XmlSqlExecutor xmlSqlExecutor;

        public MapperInvocationHandler(Class<?> mapperInterface, BaseMapperImpl<?> baseMapperImpl, XmlSqlExecutor xmlSqlExecutor) {
            this.mapperInterface = mapperInterface;
            this.baseMapperImpl = baseMapperImpl;
            this.xmlSqlExecutor = xmlSqlExecutor;
        }

        public MapperInvocationHandler(Class<?> mapperInterface, BaseMapperImpl<?> baseMapperImpl, 
                                     DatabaseClient databaseClient, R2dbcEntityTemplate r2dbcEntityTemplate) {
            this.mapperInterface = mapperInterface;
            this.baseMapperImpl = baseMapperImpl;
            this.xmlSqlExecutor = new XmlSqlExecutor(databaseClient, r2dbcEntityTemplate);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 如果是 BaseMapper 中的方法，直接调用 BaseMapperImpl
            if (isBaseMapperMethod(method)) {
                return method.invoke(baseMapperImpl, args);
            }

            // 检查是否有 SQL 注解
            if (hasAnnotationSql(method)) {
                return invokeAnnotationMethod(method, args);
            }

            // 如果是自定义方法，使用 XmlSqlExecutor 执行
            return invokeXmlMethod(method, args);
        }

        /**
         * 判断是否为 BaseMapper 中的方法
         */
        private boolean isBaseMapperMethod(Method method) {
            try {
                BaseMapper.class.getMethod(method.getName(), method.getParameterTypes());
                return true;
            } catch (NoSuchMethodException e) {
                return false;
            }
        }

        /**
         * 检查方法是否有 SQL 注解
         */
        private boolean hasAnnotationSql(Method method) {
            return method.isAnnotationPresent(Select.class) ||
                   method.isAnnotationPresent(Insert.class) ||
                   method.isAnnotationPresent(Update.class) ||
                   method.isAnnotationPresent(Delete.class);
        }

        /**
         * 执行带有 SQL 注解的方法
         */
        private Object invokeAnnotationMethod(Method method, Object[] args) {
            // 构建参数映射
            Map<String, Object> paramMap = buildParameterMap(method, args);

            // 获取 SQL 语句和操作类型
            String sql = null;
            String operationType = null;

            if (method.isAnnotationPresent(Select.class)) {
                sql = method.getAnnotation(Select.class).value();
                operationType = "SELECT";
            } else if (method.isAnnotationPresent(Insert.class)) {
                sql = method.getAnnotation(Insert.class).value();
                operationType = "INSERT";
            } else if (method.isAnnotationPresent(Update.class)) {
                sql = method.getAnnotation(Update.class).value();
                operationType = "UPDATE";
            } else if (method.isAnnotationPresent(Delete.class)) {
                sql = method.getAnnotation(Delete.class).value();
                operationType = "DELETE";
            }

            if (sql == null || sql.trim().isEmpty()) {
                throw new RuntimeException("SQL statement is empty in annotation for method: " + method.getName());
            }

            // 处理参数绑定
            sql = processParameterBinding(sql, paramMap);

            // 根据操作类型和返回类型执行 SQL
            return executeSqlByAnnotation(sql, paramMap, method, operationType);
        }

        /**
         * 处理参数绑定，将 #{param} 替换为 :param
         */
        private String processParameterBinding(String sql, Map<String, Object> paramMap) {
            // 简单的参数绑定处理，将 #{paramName} 替换为 :paramName
            String processedSql = sql;
            for (String paramName : paramMap.keySet()) {
                processedSql = processedSql.replaceAll("#\\{" + paramName + "\\}", ":" + paramName);
                processedSql = processedSql.replaceAll("\\$\\{" + paramName + "\\}", ":" + paramName);
            }
            return processedSql;
        }

        /**
         * 根据注解执行 SQL
         */
        private Object executeSqlByAnnotation(String sql, Map<String, Object> paramMap, Method method, String operationType) {
            Class<?> returnType = method.getReturnType();
            String returnTypeName = returnType.getName();

            // 过滤参数，只保留 SQL 中实际使用的参数
            Map<String, Object> filteredParams = filterUsedParameters(sql, paramMap);

            if ("SELECT".equals(operationType)) {
                if (returnTypeName.contains("Flux")) {
                    // 返回 Flux，查询列表
                    Class<?> elementType = getGenericType(method);
                    return xmlSqlExecutor.selectListBySql(sql, filteredParams, elementType);
                } else if (returnTypeName.contains("Mono")) {
                    // 返回 Mono，查询单个对象
                    Class<?> elementType = getGenericType(method);
                    return xmlSqlExecutor.selectOneBySql(sql, filteredParams, elementType);
                } else {
                    // 直接返回类型
                    return xmlSqlExecutor.selectOneBySql(sql, filteredParams, returnType);
                }
            } else {
                // INSERT, UPDATE, DELETE 操作
                if (returnTypeName.contains("Mono")) {
                    return xmlSqlExecutor.executeSql(sql, filteredParams);
                } else if (returnType == Integer.class || returnType == int.class) {
                    return xmlSqlExecutor.executeSql(sql, filteredParams).block();
                } else if (returnType == Boolean.class || returnType == boolean.class) {
                    return xmlSqlExecutor.executeSql(sql, filteredParams).map(count -> count > 0);
                } else {
                    return xmlSqlExecutor.executeSql(sql, filteredParams);
                }
            }
        }

        /**
         * 过滤参数，只保留 SQL 中实际使用的参数
         */
        private Map<String, Object> filterUsedParameters(String sql, Map<String, Object> paramMap) {
            Map<String, Object> filteredParams = new HashMap<>();
            for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
                String paramName = entry.getKey();
                // 检查 SQL 中是否包含该参数
                if (sql.contains(":" + paramName)) {
                    filteredParams.put(paramName, entry.getValue());
                }
            }
            return filteredParams;
        }

        /**
         * 执行 XML 中定义的方法
         */
        private Object invokeXmlMethod(Method method, Object[] args) {
            String statementId = mapperInterface.getName() + "." + method.getName();

            // 构建参数映射
            Map<String, Object> paramMap = buildParameterMap(method, args);

            // 根据返回类型选择执行方式
            Class<?> returnType = method.getReturnType();
            String returnTypeName = returnType.getName();

            if (returnTypeName.contains("Flux")) {
                // 返回 Flux，使用 selectList
                Class<?> elementType = getGenericType(method);
                return xmlSqlExecutor.selectList(statementId, paramMap, elementType);
            } else if (returnTypeName.contains("Mono")) {
                // 返回 Mono
                Class<?> elementType = getGenericType(method);
                if (elementType == Integer.class || elementType == Long.class) {
                    // 根据方法名判断操作类型
                    String methodName = method.getName().toLowerCase();
                    if (methodName.contains("insert") || methodName.contains("save")) {
                        // 插入操作
                        return xmlSqlExecutor.insert(statementId, paramMap);
                    } else if (methodName.contains("delete") || methodName.contains("remove")) {
                        // 删除操作
                        return xmlSqlExecutor.delete(statementId, paramMap);
                    } else if (methodName.contains("get") || methodName.contains("find") || 
                              methodName.contains("select") || methodName.contains("count") ||
                              methodName.contains("query") || methodName.contains("statistics")) {
                        // 查询操作，返回数值型结果（如 COUNT、SUM 等聚合函数）
                        return xmlSqlExecutor.selectOne(statementId, paramMap, elementType);
                    } else {
                        // 更新操作
                        return xmlSqlExecutor.update(statementId, paramMap);
                    }
                } else if (elementType != null && elementType == Page.class) {
                    // 处理分页查询 Mono<Page<T>>
                    return handlePageQuery(statementId, paramMap, method, args);
                } else {
                    // 查询单个对象
                    return xmlSqlExecutor.selectOne(statementId, paramMap, elementType);
                }
            } else if (returnType == String.class) {
                // 支持直接返回String类型
                return xmlSqlExecutor.selectOne(statementId, paramMap, String.class)
                        .map(Object::toString);
            } else if (returnType == Integer.class || returnType == int.class) {
                // 支持直接返回Integer类型
                return xmlSqlExecutor.update(statementId, paramMap);
            } else if (returnType == Long.class || returnType == long.class) {
                // 支持直接返回Long类型
                return xmlSqlExecutor.update(statementId, paramMap).map(Long::valueOf);
            } else if (returnType == Boolean.class || returnType == boolean.class) {
                // 支持直接返回Boolean类型
                return xmlSqlExecutor.update(statementId, paramMap).map(count -> count > 0);
            } else if (returnType.isPrimitive() || returnType.getPackage() != null && returnType.getPackage().getName().startsWith("java.lang")) {
                // 其他基本类型或包装类型，尝试作为单个对象查询
                return xmlSqlExecutor.selectOne(statementId, paramMap, returnType);
            }

            throw new UnsupportedOperationException("Unsupported return type: " + returnType);
        }

        /**
         * 构建参数映射
         */
        private Map<String, Object> buildParameterMap(Method method, Object[] args) {
            Map<String, Object> paramMap = new HashMap<>();

            if (args == null || args.length == 0) {
                return paramMap;
            }

            // 简单处理：如果只有一个参数且是 Map，直接使用
            if (args.length == 1 && args[0] instanceof Map) {
                return (Map<String, Object>) args[0];
            }

            // 获取方法参数信息
            java.lang.reflect.Parameter[] parameters = method.getParameters();

            for (int i = 0; i < args.length; i++) {
                String paramName = null;

                // 优先使用 @Param 注解的值
                if (i < parameters.length) {
                    java.lang.reflect.Parameter parameter = parameters[i];

                    // 检查是否有 @Param 注解
                    Param paramAnnotation =
                        parameter.getAnnotation(Param.class);
                    if (paramAnnotation != null) {
                        paramName = paramAnnotation.value();
                    } else if (parameter.isNamePresent()) {
                        // 如果没有 @Param 注解，尝试使用参数名
                        paramName = parameter.getName();
                    }
                }

                // 如果有参数名，使用参数名
                if (paramName != null && !paramName.isEmpty()) {
                    paramMap.put(paramName, args[i]);
                }

                // 同时也添加按位置的参数名，以兼容不同的使用方式
                paramMap.put("param" + (i + 1), args[i]);
            }

            return paramMap;
        }

        /**
         * 获取泛型类型
         */
        private Class<?> getGenericType(Method method) {
            java.lang.reflect.Type genericReturnType = method.getGenericReturnType();

            if (!(genericReturnType instanceof ParameterizedType)) {
                throw new UnsupportedOperationException("Method return type must be parameterized: " + method.getName());
            }

            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            java.lang.reflect.Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length == 0) {
                throw new UnsupportedOperationException("No generic type arguments found for method: " + method.getName());
            }

            java.lang.reflect.Type actualType = actualTypeArguments[0];

            // 处理泛型类型参数
            if (actualType instanceof Class) {
                return (Class<?>) actualType;
            } else if (actualType instanceof ParameterizedType) {
                // 如果是嵌套的泛型类型，获取原始类型
                return (Class<?>) ((ParameterizedType) actualType).getRawType();
            } else {
                throw new UnsupportedOperationException("Unsupported generic type: " + actualType + " for method: " + method.getName());
            }
        }

        /**
         * 处理分页查询 Mono<Page<T>>
         */
        @SuppressWarnings("unchecked")
        private Object handlePageQuery(String statementId, Map<String, Object> paramMap, Method method, Object[] args) {
            // 1. 从参数中提取 Page 对象
            Page<?> pageParam = extractPageParameter(args);
            if (pageParam == null) {
                throw new RuntimeException("Page parameter not found for method: " + method.getName());
            }

            // 2. 获取 Page 的泛型类型
            Class<?> elementType = getPageElementType(method);

                        // 3. 直接调用 XmlSqlExecutor 的 selectPage 方法
            return xmlSqlExecutor.selectPage(statementId, paramMap, (Page) pageParam, elementType);
        }

        /**
         * 从方法参数中提取 Page 对象
         */
        private Page<?> extractPageParameter(Object[] args) {
            if (args == null) {
                return null;
            }
            
            for (Object arg : args) {
                if (arg instanceof Page) {
                    return (Page<?>) arg;
                }
            }
            return null;
        }

        /**
         * 获取 Page 的元素类型
         */
        private Class<?> getPageElementType(Method method) {
            java.lang.reflect.Type genericReturnType = method.getGenericReturnType();
            
            if (genericReturnType instanceof java.lang.reflect.ParameterizedType) {
                java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) genericReturnType;
                java.lang.reflect.Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                
                if (actualTypeArguments.length > 0) {
                    java.lang.reflect.Type pageType = actualTypeArguments[0];
                    if (pageType instanceof java.lang.reflect.ParameterizedType) {
                        java.lang.reflect.ParameterizedType pageParameterizedType = (java.lang.reflect.ParameterizedType) pageType;
                        java.lang.reflect.Type[] pageTypeArguments = pageParameterizedType.getActualTypeArguments();
                        if (pageTypeArguments.length > 0) {
                            return (Class<?>) pageTypeArguments[0];
                        }
                    }
                }
            }
            
            return Object.class;
        }
    }
    
    /**
     * 将处理器依赖装配到 BaseMapperImpl 实例。
     *
     * <p>BaseMapperImpl 由本工厂以 {@code new} 创建，不受 Spring 管理，
     * 因此走其公开的 {@code withProcessors}，不再反射写私有字段。
     */
    private void injectFieldFillProcessor(BaseMapperImpl<?> mapper) {
        mapper.withProcessors(fieldFillProcessor, logicDeleteProcessor);
    }
}
