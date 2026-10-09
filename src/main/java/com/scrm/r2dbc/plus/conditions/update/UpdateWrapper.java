package com.scrm.r2dbc.plus.conditions.update;

import com.scrm.r2dbc.plus.conditions.AbstractWrapper;
import com.scrm.r2dbc.plus.conditions.query.QueryWrapper;
import com.scrm.r2dbc.plus.function.SFunction;
import com.scrm.r2dbc.plus.util.LambdaUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 更新条件构造器
 * 类似于 MyBatis-Plus 的 UpdateWrapper
 * 
 * @param <T> 实体类型
 * @author dason
 */
public class UpdateWrapper<T> extends AbstractWrapper<T, UpdateWrapper<T>> {

    private final StringBuilder setSegment = new StringBuilder();
    private final Map<String, Object> setParamMap = new HashMap<>();
    private int setParamIndex = 0;
    private final List<String> orderByColumns = new ArrayList<>();
    private String lastSql;
    private boolean updateNullValue = false;

    public UpdateWrapper() {
        super(null);
    }

    public UpdateWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    @Override
    protected UpdateWrapper<T> typedThis() {
        return this;
    }

    /**
     * 设置字段值
     */
    public UpdateWrapper<T> set(String column, Object val) {
        String paramName = "set_param" + (++setParamIndex);

        if (setSegment.length() > 0) {
            setSegment.append(", ");
        }
        setSegment.append(column).append(" = :").append(paramName);
        setParamMap.put(paramName, val);
        return this;
    }

    /**
     * 设置字段值 - Lambda 版本
     */
    public UpdateWrapper<T> set(SFunction<T, ?> column, Object val) {
        return set(LambdaUtils.getColumnName(column), val);
    }

    /**
     * 设置字段值（支持条件判断）
     */
    public UpdateWrapper<T> set(boolean condition, String column, Object val) {
        return condition ? set(column, val) : this;
    }

    /**
     * 设置字段值（支持条件判断）- Lambda 版本
     */
    public UpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? set(column, val) : this;
    }

    /**
     * 设置字段为 NULL
     */
    public UpdateWrapper<T> setNull(String column) {
        if (setSegment.length() > 0) {
            setSegment.append(", ");
        }
        setSegment.append(column).append(" = NULL");
        return this;
    }

    /**
     * 设置字段为 NULL - Lambda 版本
     */
    public UpdateWrapper<T> setNull(SFunction<T, ?> column) {
        return setNull(LambdaUtils.getColumnName(column));
    }

    /**
     * 设置字段为 NULL（支持条件判断）
     */
    public UpdateWrapper<T> setNull(boolean condition, String column) {
        return condition ? setNull(column) : this;
    }

    /**
     * 设置字段为 NULL（支持条件判断）- Lambda 版本
     */
    public UpdateWrapper<T> setNull(boolean condition, SFunction<T, ?> column) {
        return condition ? setNull(column) : this;
    }

    /**
     * 字段自增/自定义 SQL
     */
    public UpdateWrapper<T> setSql(String column, String sqlSet) {
        if (setSegment.length() > 0) {
            setSegment.append(", ");
        }
        setSegment.append(column).append(" = ").append(sqlSet);
        return this;
    }

    /**
     * 字段自增/自定义 SQL - Lambda 版本
     */
    public UpdateWrapper<T> setSql(SFunction<T, ?> column, String sqlSet) {
        return setSql(LambdaUtils.getColumnName(column), sqlSet);
    }

    /**
     * 字段自增/自定义 SQL（支持条件判断）
     */
    public UpdateWrapper<T> setSql(boolean condition, String column, String sqlSet) {
        return condition ? setSql(column, sqlSet) : this;
    }

    /**
     * 字段自增/自定义 SQL（支持条件判断）- Lambda 版本
     */
    public UpdateWrapper<T> setSql(boolean condition, SFunction<T, ?> column, String sqlSet) {
        return condition ? setSql(column, sqlSet) : this;
    }

    /**
     * 获取 SET 子句
     */
    public String getSetSegment() {
        return setSegment.toString();
    }

    /**
     * 获取 WHERE 子句
     */
    public String getWhereSegment() {
        return super.getSqlSegment();
    }

    /**
     * 排序 - 升序
     */
    public UpdateWrapper<T> orderByAsc(String column) {
        orderByColumns.add(column + " ASC");
        return this;
    }

    /**
     * 排序 - 升序 - Lambda 版本
     */
    public UpdateWrapper<T> orderByAsc(SFunction<T, ?> column) {
        orderByColumns.add(LambdaUtils.getColumnName(column) + " ASC");
        return this;
    }

    /**
     * 排序 - 降序
     */
    public UpdateWrapper<T> orderByDesc(String column) {
        orderByColumns.add(column + " DESC");
        return this;
    }

    /**
     * 排序 - 降序 - Lambda 版本
     */
    public UpdateWrapper<T> orderByDesc(SFunction<T, ?> column) {
        orderByColumns.add(LambdaUtils.getColumnName(column) + " DESC");
        return this;
    }

    /**
     * 排序 - 通用方法（参考 MyBatis-Plus）- 字符串版本
     */
    public UpdateWrapper<T> orderBy(boolean condition, boolean isAsc, String column) {
        if (condition) {
            orderByColumns.add(column + (isAsc ? " ASC" : " DESC"));
        }
        return this;
    }

    /**
     * 排序 - 通用方法（参考 MyBatis-Plus）- Lambda 版本
     */
    public UpdateWrapper<T> orderBy(boolean condition, boolean isAsc, SFunction<T, ?> column) {
        if (condition) {
            String columnName = LambdaUtils.getColumnName(column);
            orderByColumns.add(columnName + (isAsc ? " ASC" : " DESC"));
        }
        return this;
    }

    /**
     * 排序 - 通用方法（默认执行）- 字符串版本
     */
    public UpdateWrapper<T> orderBy(boolean isAsc, String column) {
        return orderBy(true, isAsc, column);
    }

    /**
     * 排序 - 通用方法（默认执行）- Lambda 版本
     */
    public UpdateWrapper<T> orderBy(boolean isAsc, SFunction<T, ?> column) {
        return orderBy(true, isAsc, column);
    }

    /**
     * 拼接在最后，例如：last("limit 1")
     */
    public UpdateWrapper<T> last(String lastSql) {
        this.lastSql = lastSql;
        return this;
    }

    /**
     * 获取 ORDER BY 子句
     */
    public String getOrderBySegment() {
        if (orderByColumns.isEmpty()) {
            return "";
        }
        return " ORDER BY " + String.join(", ", orderByColumns);
    }

    /**
     * 获取 last SQL 子句
     */
    public String getLastSql() {
        return lastSql != null ? " " + lastSql : "";
    }

    /**
     * 重写 getSqlSegment 方法，包含 ORDER BY 和 last 子句
     */
    @Override
    public String getSqlSegment() {
        String baseSql = super.getSqlSegment();
        String orderByClause = getOrderBySegment();
        String lastSqlClause = getLastSql();
        return baseSql + orderByClause + lastSqlClause;
    }

    @Override
    public Map<String, Object> getParamNameValuePairs() {
        Map<String, Object> allParams = new HashMap<>();
        allParams.putAll(setParamMap);
        allParams.putAll(paramMap);
        return allParams;
    }

    /**
     * 获取 SET 参数
     */
    public Map<String, Object> getSetParamNameValuePairs() {
        return setParamMap;
    }

    /**
     * 获取 WHERE 参数
     */
    public Map<String, Object> getWhereParamNameValuePairs() {
        return paramMap;
    }

    /**
     * 构建查询包装器（用于删除操作）
     * 将当前的 WHERE 条件转换为查询包装器
     */
    public QueryWrapper<T> buildQueryWrapper() {
        QueryWrapper<T> queryWrapper = new QueryWrapper<>(entityClass);

        // 如果有 WHERE 条件，则构建相应的查询包装器
        String whereSegment = getWhereSegment();
        if (!whereSegment.isEmpty()) {
            // 通过构造函数或工厂方法创建带条件的查询包装器
            queryWrapper = QueryWrapper.fromCondition(entityClass, whereSegment, getWhereParamNameValuePairs());
        }

        return queryWrapper;
    }

    /**
     * 返回当前实例，用于模拟 MyBatis-Plus 的 lambda() 方法
     * 现在 UpdateWrapper 本身就支持 Lambda 表达式，所以直接返回自身
     *
     * @return UpdateWrapper 当前实例
     */
    public UpdateWrapper<T> lambda() {
        return this;
    }

    /**
     * 设置更新null值标志
     * 当设置为true时，实体对象中的null字段也会被更新到数据库
     *
     * @return UpdateWrapper 当前实例
     */
    public UpdateWrapper<T> updateNullValue() {
        this.updateNullValue = true;
        return this;
    }

    /**
     * 获取更新null值标志
     *
     * @return 是否更新null值
     */
    public boolean isUpdateNullValue() {
        return updateNullValue;
    }
}
