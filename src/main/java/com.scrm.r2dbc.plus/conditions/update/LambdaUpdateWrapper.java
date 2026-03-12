package com.scrm.r2dbc.plus.conditions.update;

import com.scrm.r2dbc.plus.conditions.AbstractLambdaWrapper;
import com.scrm.r2dbc.plus.conditions.query.LambdaQueryWrapper;
import com.scrm.r2dbc.plus.function.SFunction;
import com.scrm.r2dbc.plus.util.LambdaUtils;

import java.util.*;

/**
 * Lambda 更新条件构造器
 *
 * @param <T> 实体类型
 * @author dason
 */
public class LambdaUpdateWrapper<T> extends AbstractLambdaWrapper<T, LambdaUpdateWrapper<T>> {

    private final StringBuilder setSegment = new StringBuilder();
    private final Map<String, Object> setParamMap = new HashMap<>();
    private int setParamIndex = 0;
    private final List<String> orderByColumns = new ArrayList<>();
    private String lastSql;
    private boolean updateNullValue = false;

    public LambdaUpdateWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    public LambdaUpdateWrapper() {
        super(null);
    }

    @Override
    protected LambdaUpdateWrapper<T> typedThis() {
        return this;
    }

    /**
     * 设置字段值
     */
    public LambdaUpdateWrapper<T> set(SFunction<T, ?> column, Object val) {
        String columnName = LambdaUtils.getColumnName(column);
        String paramName = "set_param" + (++setParamIndex);

        if (setSegment.length() > 0) {
            setSegment.append(", ");
        }
        setSegment.append(columnName).append(" = :").append(paramName);
        setParamMap.put(paramName, val);
        return this;
    }

    /**
     * 设置字段值（支持条件判断）
     */
    public LambdaUpdateWrapper<T> set(boolean condition, SFunction<T, ?> column, Object val) {
        return condition ? set(column, val) : this;
    }

    /**
     * 设置字段为 NULL
     */
    public LambdaUpdateWrapper<T> setNull(SFunction<T, ?> column) {
        String columnName = LambdaUtils.getColumnName(column);
        if (setSegment.length() > 0) {
            setSegment.append(", ");
        }
        setSegment.append(columnName).append(" = NULL");
        return this;
    }

    /**
     * 设置字段为 NULL（支持条件判断）
     */
    public LambdaUpdateWrapper<T> setNull(boolean condition, SFunction<T, ?> column) {
        return condition ? setNull(column) : this;
    }

    /**
     * 字段自增/自定义 SQL
     */
    public LambdaUpdateWrapper<T> setSql(SFunction<T, ?> column, String sqlSet) {
        String columnName = LambdaUtils.getColumnName(column);
        if (setSegment.length() > 0) {
            setSegment.append(", ");
        }
        setSegment.append(columnName).append(" = ").append(sqlSet);
        return this;
    }

    /**
     * 字段自增/自定义 SQL（支持条件判断）
     */
    public LambdaUpdateWrapper<T> setSql(boolean condition, SFunction<T, ?> column, String sqlSet) {
        return condition ? setSql(column, sqlSet) : this;
    }

    /**
     * 排序 - 升序
     */
    public LambdaUpdateWrapper<T> orderByAsc(SFunction<T, ?> column) {
        orderByColumns.add(LambdaUtils.getColumnName(column) + " ASC");
        return this;
    }

    /**
     * 排序 - 降序
     */
    public LambdaUpdateWrapper<T> orderByDesc(SFunction<T, ?> column) {
        orderByColumns.add(LambdaUtils.getColumnName(column) + " DESC");
        return this;
    }

    /**
     * 排序 - 通用方法（参考 MyBatis-Plus）
     *
     * @param condition 是否执行排序
     * @param isAsc 是否升序，true=升序，false=降序
     * @param column 排序字段
     * @return LambdaUpdateWrapper
     */
    public LambdaUpdateWrapper<T> orderBy(boolean condition, boolean isAsc, SFunction<T, ?> column) {
        if (condition) {
            String columnName = LambdaUtils.getColumnName(column);
            orderByColumns.add(columnName + (isAsc ? " ASC" : " DESC"));
        }
        return this;
    }

    /**
     * 排序 - 通用方法（默认执行）
     *
     * @param isAsc 是否升序，true=升序，false=降序
     * @param column 排序字段
     * @return LambdaUpdateWrapper
     */
    public LambdaUpdateWrapper<T> orderBy(boolean isAsc, SFunction<T, ?> column) {
        return orderBy(true, isAsc, column);
    }

    /**
     * 拼接在最后，例如：last("limit 1")
     *
     * @param lastSql 最后拼接的 SQL
     * @return LambdaUpdateWrapper
     */
    public LambdaUpdateWrapper<T> last(String lastSql) {
        this.lastSql = lastSql;
        return this;
    }

    /**
     * 获取排序列
     */
    public List<String> getOrderByColumns() {
        return orderByColumns;
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
    public LambdaQueryWrapper<T> buildQueryWrapper() {
        LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>(entityClass);

        // 如果有 WHERE 条件，则构建相应的查询包装器
        String whereSegment = getWhereSegment();
        if (!whereSegment.isEmpty()) {
            // 通过构造函数或工厂方法创建带条件的查询包装器
            queryWrapper = LambdaQueryWrapper.fromCondition(entityClass, whereSegment, getWhereParamNameValuePairs());
        }

        return queryWrapper;
    }

    /**
     * 设置更新null值标志
     * 当设置为true时，实体对象中的null字段也会被更新到数据库
     *
     * @return LambdaUpdateWrapper 当前实例
     */
    public LambdaUpdateWrapper<T> updateNullValue() {
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
