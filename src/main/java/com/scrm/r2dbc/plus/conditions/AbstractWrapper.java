package com.scrm.r2dbc.plus.conditions;

import com.scrm.r2dbc.plus.function.SFunction;
import com.scrm.r2dbc.plus.util.LambdaUtils;

import java.util.*;

/**
 * 抽象条件构造器
 * 类似于 MyBatis-Plus 的 AbstractWrapper
 * 
 * @param <T> 实体类型
 * @param <R> 子类类型，用于链式调用
 * @author dason
 */
public abstract class AbstractWrapper<T, R extends AbstractWrapper<T, R>> implements Wrapper<T>, ISqlSegment {

    protected final Class<T> entityClass;
    protected final StringBuilder sqlSegment = new StringBuilder();
    protected final Map<String, Object> paramMap = new HashMap<>();
    protected int paramIndex = 0;
    protected String tableName;

    public AbstractWrapper(Class<T> entityClass) {
        this.entityClass = entityClass;
        this.tableName = getTableNameFromClass(entityClass);
    }

    /**
     * 获取子类实例，用于链式调用
     */
    protected abstract R typedThis();

    /**
     * 等于条件
     */
    public R eq(String column, Object val) {
        return addCondition(column, "=", val);
    }

    /**
     * 等于条件 - Lambda 版本
     */
    public R eq(SFunction<T, ?> column, Object val) {
        return eq(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 等于条件（支持条件判断）
     */
    public R eq(boolean condition, String column, Object val) {
        return condition ? eq(column, val) : typedThis();
    }

    /**
     * 等于条件（支持条件判断）- Lambda 版本
     */
    public R eq(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? eq(column, val) : typedThis();
    }

    /**
     * 不等于条件
     */
    public R ne(String column, Object val) {
        return addCondition(column, "!=", val);
    }

    /**
     * 不等于条件 - Lambda 版本
     */
    public R ne(SFunction<T, ?> column, Object val) {
        return ne(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 不等于条件（支持条件判断）
     */
    public R ne(boolean condition, String column, Object val) {
        return condition ? ne(column, val) : typedThis();
    }

    /**
     * 不等于条件（支持条件判断）- Lambda 版本
     */
    public R ne(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? ne(column, val) : typedThis();
    }

    /**
     * 大于条件
     */
    public R gt(String column, Object val) {
        return addCondition(column, ">", val);
    }

    /**
     * 大于条件 - Lambda 版本
     */
    public R gt(SFunction<T, ?> column, Object val) {
        return gt(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 大于条件（支持条件判断）
     */
    public R gt(boolean condition, String column, Object val) {
        return condition ? gt(column, val) : typedThis();
    }

    /**
     * 大于条件（支持条件判断）- Lambda 版本
     */
    public R gt(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? gt(column, val) : typedThis();
    }

    /**
     * 大于等于条件
     */
    public R ge(String column, Object val) {
        return addCondition(column, ">=", val);
    }

    /**
     * 大于等于条件 - Lambda 版本
     */
    public R ge(SFunction<T, ?> column, Object val) {
        return ge(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 大于等于条件（支持条件判断）
     */
    public R ge(boolean condition, String column, Object val) {
        return condition ? ge(column, val) : typedThis();
    }

    /**
     * 大于等于条件（支持条件判断）- Lambda 版本
     */
    public R ge(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? ge(column, val) : typedThis();
    }

    /**
     * 小于条件
     */
    public R lt(String column, Object val) {
        return addCondition(column, "<", val);
    }

    /**
     * 小于条件 - Lambda 版本
     */
    public R lt(SFunction<T, ?> column, Object val) {
        return lt(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 小于条件（支持条件判断）
     */
    public R lt(boolean condition, String column, Object val) {
        return condition ? lt(column, val) : typedThis();
    }

    /**
     * 小于条件（支持条件判断）- Lambda 版本
     */
    public R lt(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? lt(column, val) : typedThis();
    }

    /**
     * 小于等于条件
     */
    public R le(String column, Object val) {
        return addCondition(column, "<=", val);
    }

    /**
     * 小于等于条件 - Lambda 版本
     */
    public R le(SFunction<T, ?> column, Object val) {
        return le(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 小于等于条件（支持条件判断）
     */
    public R le(boolean condition, String column, Object val) {
        return condition ? le(column, val) : typedThis();
    }

    /**
     * 小于等于条件（支持条件判断）- Lambda 版本
     */
    public R le(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? le(column, val) : typedThis();
    }

    /**
     * LIKE 条件
     */
    public R like(String column, Object val) {
        return addCondition(column, "LIKE", "%" + val + "%");
    }

    /**
     * LIKE 条件 - Lambda 版本
     */
    public R like(SFunction<T, ?> column, Object val) {
        return like(LambdaUtils.getColumnName(column), val);
    }

    /**
     * LIKE 条件（支持条件判断）
     */
    public R like(boolean condition, String column, Object val) {
        return condition ? like(column, val) : typedThis();
    }

    /**
     * LIKE 条件（支持条件判断）- Lambda 版本
     */
    public R like(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? like(column, val) : typedThis();
    }

    /**
     * NOT LIKE 条件
     */
    public R notLike(String column, Object val) {
        return addCondition(column, "NOT LIKE", "%" + val + "%");
    }

    /**
     * NOT LIKE 条件 - Lambda 版本
     */
    public R notLike(SFunction<T, ?> column, Object val) {
        return notLike(LambdaUtils.getColumnName(column), val);
    }

    /**
     * NOT LIKE 条件（支持条件判断）
     */
    public R notLike(boolean condition, String column, Object val) {
        return condition ? notLike(column, val) : typedThis();
    }

    /**
     * NOT LIKE 条件（支持条件判断）- Lambda 版本
     */
    public R notLike(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? notLike(column, val) : typedThis();
    }

    /**
     * IS NULL 条件
     */
    public R isNull(String column) {
        addToSql(column + " IS NULL");
        return typedThis();
    }

    /**
     * IS NULL 条件 - Lambda 版本
     */
    public R isNull(SFunction<T, ?> column) {
        return isNull(LambdaUtils.getColumnName(column));
    }

    /**
     * IS NULL 条件（支持条件判断）
     */
    public R isNull(boolean condition, String column) {
        return condition ? isNull(column) : typedThis();
    }

    /**
     * IS NULL 条件（支持条件判断）- Lambda 版本
     */
    public R isNull(boolean condition, SFunction<T, ?> column) {
        return condition ? isNull(column) : typedThis();
    }

    /**
     * IS NOT NULL 条件
     */
    public R isNotNull(String column) {
        addToSql(column + " IS NOT NULL");
        return typedThis();
    }

    /**
     * IS NOT NULL 条件 - Lambda 版本
     */
    public R isNotNull(SFunction<T, ?> column) {
        return isNotNull(LambdaUtils.getColumnName(column));
    }

    /**
     * IS NOT NULL 条件（支持条件判断）
     */
    public R isNotNull(boolean condition, String column) {
        return condition ? isNotNull(column) : typedThis();
    }

    /**
     * IS NOT NULL 条件（支持条件判断）- Lambda 版本
     */
    public R isNotNull(boolean condition, SFunction<T, ?> column) {
        return condition ? isNotNull(column) : typedThis();
    }

    /**
     * IN 条件
     */
    public R in(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return typedThis();
        }
        StringBuilder inClause = new StringBuilder(column + " IN (");
        List<String> placeholders = new ArrayList<>();
        for (Object value : values) {
            String paramName = "param" + (++paramIndex);
            placeholders.add(":" + paramName);
            paramMap.put(paramName, value);
        }
        inClause.append(String.join(", ", placeholders)).append(")");
        addToSql(inClause.toString());
        return typedThis();
    }

    /**
     * IN 条件 - Lambda 版本
     */
    public R in(SFunction<T, ?> column, Collection<?> values) {
        return in(LambdaUtils.getColumnName(column), values);
    }

    /**
     * IN 条件（支持条件判断）
     */
    public R in(boolean condition, String column, Collection<?> values) {
        return condition ? in(column, values) : typedThis();
    }

    /**
     * IN 条件（支持条件判断）- Lambda 版本
     */
    public R in(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        return condition ? in(column, values) : typedThis();
    }

    /**
     * NOT IN 条件
     */
    public R notIn(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return typedThis();
        }
        StringBuilder notInClause = new StringBuilder(column + " NOT IN (");
        List<String> placeholders = new ArrayList<>();
        for (Object value : values) {
            String paramName = "param" + (++paramIndex);
            placeholders.add(":" + paramName);
            paramMap.put(paramName, value);
        }
        notInClause.append(String.join(", ", placeholders)).append(")");
        addToSql(notInClause.toString());
        return typedThis();
    }

    /**
     * NOT IN 条件 - Lambda 版本
     */
    public R notIn(SFunction<T, ?> column, Collection<?> values) {
        return notIn(LambdaUtils.getColumnName(column), values);
    }

    /**
     * NOT IN 条件（支持条件判断）
     */
    public R notIn(boolean condition, String column, Collection<?> values) {
        return condition ? notIn(column, values) : typedThis();
    }

    /**
     * NOT IN 条件（支持条件判断）- Lambda 版本
     */
    public R notIn(boolean condition, SFunction<T, ?> column, Collection<?> values) {
        return condition ? notIn(column, values) : typedThis();
    }

    /**
     * BETWEEN 条件
     */
    public R between(String column, Object val1, Object val2) {
        String paramName1 = "param" + (++paramIndex);
        String paramName2 = "param" + (++paramIndex);
        addToSql(column + " BETWEEN :" + paramName1 + " AND :" + paramName2);
        paramMap.put(paramName1, val1);
        paramMap.put(paramName2, val2);
        return typedThis();
    }

    /**
     * BETWEEN 条件 - Lambda 版本
     */
    public R between(SFunction<T, ?> column, Object val1, Object val2) {
        return between(LambdaUtils.getColumnName(column), val1, val2);
    }

    /**
     * BETWEEN 条件（支持条件判断）
     */
    public R between(boolean condition, String column, Object val1, Object val2) {
        return condition ? between(column, val1, val2) : typedThis();
    }

    /**
     * BETWEEN 条件（支持条件判断）- Lambda 版本
     */
    public R between(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        return condition ? between(column, val1, val2) : typedThis();
    }

    /**
     * NOT BETWEEN 条件
     */
    public R notBetween(String column, Object val1, Object val2) {
        String paramName1 = "param" + (++paramIndex);
        String paramName2 = "param" + (++paramIndex);
        addToSql(column + " NOT BETWEEN :" + paramName1 + " AND :" + paramName2);
        paramMap.put(paramName1, val1);
        paramMap.put(paramName2, val2);
        return typedThis();
    }

    /**
     * NOT BETWEEN 条件 - Lambda 版本
     */
    public R notBetween(SFunction<T, ?> column, Object val1, Object val2) {
        return notBetween(LambdaUtils.getColumnName(column), val1, val2);
    }

    /**
     * NOT BETWEEN 条件（支持条件判断）
     */
    public R notBetween(boolean condition, String column, Object val1, Object val2) {
        return condition ? notBetween(column, val1, val2) : typedThis();
    }

    /**
     * NOT BETWEEN 条件（支持条件判断）- Lambda 版本
     */
    public R notBetween(boolean condition, SFunction<T, ?> column, Object val1, Object val2) {
        return condition ? notBetween(column, val1, val2) : typedThis();
    }

    /**
     * AND 连接
     */
    public R and() {
        if (sqlSegment.length() > 0) {
            sqlSegment.append(" AND ");
        }
        return typedThis();
    }

    /**
     * OR 连接
     */
    public R or() {
        if (sqlSegment.length() > 0) {
            sqlSegment.append(" OR ");
        }
        return typedThis();
    }

    /**
     * 拼接自定义 SQL 片段
     * 
     * @param applySql 自定义 SQL 片段，支持 {0}, {1}, {2} 等占位符
     * @param params 参数值，按顺序替换占位符
     * @return 当前实例，用于链式调用
     * 
     * @apiNote 注意：请务必使用占位符形式传递参数，避免 SQL 注入风险
     * 
     * 示例用法：
     * <pre>
     * // 正确用法 - 使用占位符
     * wrapper.apply("age > {0} AND name LIKE {1}", 18, "%test%");
     * 
     * // 错误用法 - 直接拼接字符串（有 SQL 注入风险）
     * wrapper.apply("age > " + age + " AND name LIKE '" + name + "'");
     * </pre>
     */
    public R apply(String applySql, Object... params) {
        if (applySql == null || applySql.trim().isEmpty()) {
            return typedThis();
        }

        String processedSql = applySql;
        
        // 处理占位符参数
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                String placeholder = "{" + i + "}";
                if (processedSql.contains(placeholder)) {
                    String paramName = "param" + (++paramIndex);
                    processedSql = processedSql.replace(placeholder, ":" + paramName);
                    paramMap.put(paramName, params[i]);
                }
            }
        }
        
        // 添加到 SQL 片段
        addToSql(processedSql);
        return typedThis();
    }

    /**
     * 拼接自定义 SQL 片段（带条件判断）
     * 
     * @param condition 是否执行该条件，为 false 时不添加此 SQL 片段
     * @param applySql 自定义 SQL 片段，支持 {0}, {1}, {2} 等占位符
     * @param params 参数值，按顺序替换占位符
     * @return 当前实例，用于链式调用
     * 
     * @apiNote 注意：请务必使用占位符形式传递参数，避免 SQL 注入风险
     * 
     * 示例用法：
     * <pre>
     * // 根据条件动态添加 SQL 片段
     * boolean hasAgeFilter = true;
     * wrapper.apply(hasAgeFilter, "age > {0}", 18);
     * </pre>
     */
    public R apply(boolean condition, String applySql, Object... params) {
        return condition ? apply(applySql, params) : typedThis();
    }

    /**
     * 添加条件
     */
    protected R addCondition(String columnName, String operator, Object value) {
        String paramName = "param" + (++paramIndex);
        addToSql(columnName + " " + operator + " :" + paramName);
        paramMap.put(paramName, value);
        return typedThis();
    }

    /**
     * 添加到 SQL 片段
     */
    protected void addToSql(String condition) {
        if (sqlSegment.length() > 0) {
            sqlSegment.append(" AND ");
        }
        sqlSegment.append(condition);
    }

    @Override
    public String getSqlSegment() {
        return sqlSegment.toString();
    }

    @Override
    public Map<String, Object> getParamNameValuePairs() {
        return paramMap;
    }

    @Override
    public Class<T> getEntityClass() {
        return entityClass;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    /**
     * 从类名获取表名（简单实现，可以后续扩展支持注解）
     */
    protected String getTableNameFromClass(Class<T> clazz) {
        if (clazz == null) {
            return null; // 当实体类为null时，返回null，表名将在后续使用时确定
        }
        String className = clazz.getSimpleName();
        return LambdaUtils.camelToUnderscore(className);
    }
}
