package com.scrm.r2dbc.plus.conditions.query;

import com.scrm.r2dbc.plus.conditions.AbstractLambdaWrapper;
import com.scrm.r2dbc.plus.function.SFunction;
import com.scrm.r2dbc.plus.util.LambdaUtils;

import java.util.*;

/**
 * Lambda 查询条件构造器
 *
 * @param <T> 实体类型
 * @author dason
 */
public class LambdaQueryWrapper<T> extends AbstractLambdaWrapper<T, LambdaQueryWrapper<T>> {

    private final List<String> selectColumns = new ArrayList<>();
    private final List<String> orderByColumns = new ArrayList<>();
    private Long limit;
    private Long offset;
    private String lastSql;

    public LambdaQueryWrapper(Class<T> entityClass) {
        super(entityClass);
    }

    public LambdaQueryWrapper() {
        super(null);
    }

    @Override
    protected LambdaQueryWrapper<T> typedThis() {
        return this;
    }



    /**
     * 左 LIKE 条件
     */
    public LambdaQueryWrapper<T> likeLeft(SFunction<T, ?> column, Object val) {
        return super.addCondition(LambdaUtils.getColumnName(column), "LIKE", "%" + val);
    }

    /**
     * 右 LIKE 条件
     */
    public LambdaQueryWrapper<T> likeRight(SFunction<T, ?> column, Object val) {
        return super.addCondition(LambdaUtils.getColumnName(column), "LIKE", val + "%");
    }



    /**
     * 选择字段
     */
    @SafeVarargs
    public final LambdaQueryWrapper<T> select(SFunction<T, ?>... columns) {
        for (SFunction<T, ?> column : columns) {
            selectColumns.add(LambdaUtils.getColumnName(column));
        }
        return this;
    }

    /**
     * 排序 - 升序
     */
    public LambdaQueryWrapper<T> orderByAsc(SFunction<T, ?> column) {
        orderByColumns.add(LambdaUtils.getColumnName(column) + " ASC");
        return this;
    }

    /**
     * 排序 - 降序
     */
    public LambdaQueryWrapper<T> orderByDesc(SFunction<T, ?> column) {
        orderByColumns.add(LambdaUtils.getColumnName(column) + " DESC");
        return this;
    }

    /**
     * 排序 - 通用方法（参考 MyBatis-Plus）
     *
     * @param condition 是否执行排序
     * @param isAsc 是否升序，true=升序，false=降序
     * @param column 排序字段
     * @return LambdaQueryWrapper
     */
    public LambdaQueryWrapper<T> orderBy(boolean condition, boolean isAsc, SFunction<T, ?> column) {
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
     * @return LambdaQueryWrapper
     */
    public LambdaQueryWrapper<T> orderBy(boolean isAsc, SFunction<T, ?> column) {
        return orderBy(true, isAsc, column);
    }

    /**
     * 限制查询结果数量
     */
    public LambdaQueryWrapper<T> limit(long limit) {
        this.limit = limit;
        return this;
    }

    /**
     * 设置偏移量
     */
    public LambdaQueryWrapper<T> offset(long offset) {
        this.offset = offset;
        return this;
    }

    /**
     * 分页查询
     */
    public LambdaQueryWrapper<T> page(long current, long size) {
        this.offset = (current - 1) * size;
        this.limit = size;
        return this;
    }

    /**
     * 拼接在最后，例如：last("limit 1")
     *
     * @param lastSql 最后拼接的 SQL
     * @return LambdaQueryWrapper
     */
    public LambdaQueryWrapper<T> last(String lastSql) {
        this.lastSql = lastSql;
        return this;
    }

    /**
     * 添加条件
     */
    protected LambdaQueryWrapper<T> addCondition(String columnName, String operator, Object value) {
        String paramName = "param" + (++paramIndex);
        addToSql(columnName + " " + operator + " :" + paramName);
        paramMap.put(paramName, value);
        return this;
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
     * 创建一个用于计数的查询包装器（不包含分页和排序）
     */
    public LambdaQueryWrapper<T> forCount() {
        LambdaQueryWrapper<T> countWrapper = new LambdaQueryWrapper<>(entityClass);
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
    public static <T> LambdaQueryWrapper<T> fromCondition(Class<T> entityClass, String conditionSegment, Map<String, Object> params) {
        LambdaQueryWrapper<T> wrapper = new LambdaQueryWrapper<>(entityClass);
        if (conditionSegment != null && !conditionSegment.isEmpty()) {
            wrapper.sqlSegment.append(conditionSegment);
            wrapper.paramMap.putAll(params);
        }
        return wrapper;
    }
}
