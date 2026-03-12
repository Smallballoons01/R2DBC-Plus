package com.scrm.r2dbc.plus.conditions.query;

import com.scrm.r2dbc.plus.conditions.AbstractWrapper;
import com.scrm.r2dbc.plus.function.SFunction;
import com.scrm.r2dbc.plus.util.LambdaUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询条件构造器
 * 类似于 MyBatis-Plus 的 QueryWrapper
 * 
 * @param <T> 实体类型
 * @author dason
 */
public class QueryWrapper<T> extends AbstractWrapper<T, QueryWrapper<T>> {

    private final List<String> selectColumns = new ArrayList<>();
    private final List<String> orderByColumns = new ArrayList<>();
    private Long limit;
    private Long offset;
    private String lastSql;

    public QueryWrapper() {
        super(null);
    }

    public QueryWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    @Override
    protected QueryWrapper<T> typedThis() {
        return this;
    }

    // ========== QueryWrapper 特有的 Lambda 方法 ==========

    /**
     * 选择字段
     */
    public QueryWrapper<T> select(String... columns) {
        for (String column : columns) {
            selectColumns.add(column);
        }
        return this;
    }

    /**
     * 选择字段 - Lambda 版本
     */
    @SafeVarargs
    public final QueryWrapper<T> select(SFunction<T, ?>... columns) {
        for (SFunction<T, ?> column : columns) {
            selectColumns.add(LambdaUtils.getColumnName(column));
        }
        return this;
    }

    /**
     * 排序 - 升序
     */
    public QueryWrapper<T> orderByAsc(String column) {
        orderByColumns.add(column + " ASC");
        return this;
    }

    /**
     * 排序 - 升序 - Lambda 版本
     */
    public QueryWrapper<T> orderByAsc(SFunction<T, ?> column) {
        orderByColumns.add(LambdaUtils.getColumnName(column) + " ASC");
        return this;
    }

    /**
     * 排序 - 降序
     */
    public QueryWrapper<T> orderByDesc(String column) {
        orderByColumns.add(column + " DESC");
        return this;
    }

    /**
     * 排序 - 降序 - Lambda 版本
     */
    public QueryWrapper<T> orderByDesc(SFunction<T, ?> column) {
        orderByColumns.add(LambdaUtils.getColumnName(column) + " DESC");
        return this;
    }

    /**
     * 排序 - 通用方法（参考 MyBatis-Plus）- 字符串版本
     *
     * @param condition 是否执行排序
     * @param isAsc 是否升序，true=升序，false=降序
     * @param column 排序字段
     * @return QueryWrapper
     */
    public QueryWrapper<T> orderBy(boolean condition, boolean isAsc, String column) {
        if (condition) {
            orderByColumns.add(column + (isAsc ? " ASC" : " DESC"));
        }
        return this;
    }

    /**
     * 排序 - 通用方法（默认执行）- 字符串版本
     *
     * @param isAsc 是否升序，true=升序，false=降序
     * @param column 排序字段
     * @return QueryWrapper
     */
    public QueryWrapper<T> orderBy(boolean isAsc, String column) {
        return orderBy(true, isAsc, column);
    }

    /**
     * 排序 - 通用方法（参考 MyBatis-Plus）- Lambda 版本
     *
     * @param condition 是否执行排序
     * @param isAsc 是否升序，true=升序，false=降序
     * @param column 排序字段
     * @return QueryWrapper
     */
    public QueryWrapper<T> orderBy(boolean condition, boolean isAsc, SFunction<T, ?> column) {
        if (condition) {
            String columnName = LambdaUtils.getColumnName(column);
            orderByColumns.add(columnName + (isAsc ? " ASC" : " DESC"));
        }
        return this;
    }

    /**
     * 排序 - 通用方法（默认执行）- Lambda 版本
     *
     * @param isAsc 是否升序，true=升序，false=降序
     * @param column 排序字段
     * @return QueryWrapper
     */
    public QueryWrapper<T> orderBy(boolean isAsc, SFunction<T, ?> column) {
        return orderBy(true, isAsc, column);
    }

    /**
     * 限制查询结果数量
     */
    public QueryWrapper<T> limit(long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * 设置偏移量
     */
    public QueryWrapper<T> offset(long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * 分页查询
     */
    public QueryWrapper<T> page(long current, long size) {
        this.offset = (current - 1) * size;
        this.limit = size;
        return this;
    }

    /**
     * 拼接在最后，例如：last("limit 1")
     *
     * @param lastSql 最后拼接的 SQL
     * @return QueryWrapper
     */
    public QueryWrapper<T> last(String lastSql) {
        this.lastSql = lastSql;
        return this;
    }

    /**
     * 获取选择的字段
     */
    public String getSelectColumns() {
        if (selectColumns.isEmpty()) {
            return "*";
        }
        return String.join(", ", selectColumns);
    }

    /**
     * 获取排序子句
     */
    public String getOrderBy() {
        if (orderByColumns.isEmpty()) {
            return "";
        }
        return " ORDER BY " + String.join(", ", orderByColumns);
    }

    /**
     * 获取 LIMIT 子句
     */
    public String getLimitClause() {
        StringBuilder clause = new StringBuilder();
        if (limit != null) {
            clause.append(" LIMIT ").append(limit);
        }
        if (offset != null) {
            clause.append(" OFFSET ").append(offset);
        }
        return clause.toString();
    }

    /**
     * 获取 LIMIT 值
     */
    public Long getLimit() {
        return limit;
    }

    /**
     * 获取 OFFSET 值
     */
    public Long getOffset() {
        return offset;
    }

    /**
     * 创建一个用于计数的查询包装器（不包含分页和排序）
     */
    public QueryWrapper<T> forCount() {
        QueryWrapper<T> countWrapper = new QueryWrapper<>(entityClass);
        // 只复制查询条件，不复制排序和分页
        countWrapper.sqlSegment.append(this.sqlSegment);
        countWrapper.paramMap.putAll(this.paramMap);
        countWrapper.paramIndex = this.paramIndex;
        return countWrapper;
    }

    /**
     * 获取不包含分页的 SQL 片段（用于 COUNT 查询）
     */
    public String getSqlSegmentForCount() {
        return sqlSegment.toString();
    }

    /**
     * 从现有条件创建查询包装器（静态工厂方法）
     */
    public static <T> QueryWrapper<T> fromCondition(Class<T> entityClass, String conditionSegment, java.util.Map<String, Object> params) {
        QueryWrapper<T> wrapper = new QueryWrapper<>(entityClass);
        if (conditionSegment != null && !conditionSegment.isEmpty()) {
            wrapper.sqlSegment.append(conditionSegment);
            wrapper.paramMap.putAll(params);
        }
        return wrapper;
    }

    /**
     * 获取 last SQL 子句
     */
    public String getLastSql() {
        return lastSql != null ? " " + lastSql : "";
    }

    /**
     * 重写 getSqlSegment 方法，只返回WHERE条件，不包含 last 子句
     * last 子句应该在 ORDER BY 之后添加
     */
    @Override
    public String getSqlSegment() {
        return super.getSqlSegment();
    }

    /**
     * 获取包含WHERE条件的SQL片段（用于向后兼容）
     */
    public String getWhereSegment() {
        return super.getSqlSegment();
    }

    /**
     * 返回当前实例，用于模拟 MyBatis-Plus 的 lambda() 方法
     * 现在 QueryWrapper 本身就支持 Lambda 表达式，所以直接返回自身
     *
     * @return QueryWrapper 当前实例
     */
    public QueryWrapper<T> lambda() {
        return this;
    }
}
