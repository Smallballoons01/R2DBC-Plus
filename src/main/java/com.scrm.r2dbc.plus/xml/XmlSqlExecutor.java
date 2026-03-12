package com.scrm.r2dbc.plus.xml;

import com.scrm.r2dbc.plus.page.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * XML SQL 执行器
 *
 * @author dason
 */
@Component
public class XmlSqlExecutor {

    private final DatabaseClient databaseClient;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    /**
     * 默认构造方法 - 使用@Autowired注入
     */
    @Autowired
    public XmlSqlExecutor(DatabaseClient databaseClient, R2dbcEntityTemplate r2dbcEntityTemplate) {
        this.databaseClient = databaseClient;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
    }



    /**
     * 执行查询语句，返回单个结果
     */
    public <T> Mono<T> selectOne(String statementId, Map<String, Object> parameters, Class<T> resultType) {
        SqlStatement sqlStatement = XmlMapperParser.getSqlStatement(statementId);
        if (sqlStatement == null) {
            throw new RuntimeException("SQL statement not found: " + statementId);
        }

        if (sqlStatement.getStatementType() != SqlStatement.StatementType.SELECT) {
            throw new RuntimeException("Statement is not a SELECT: " + statementId);
        }

        String sql = sqlStatement.processParameters(parameters);

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);
        if (parameters != null) {
            // 只绑定SQL中实际存在的参数
            executeSpec = bindActualParameters(executeSpec, sql, parameters);
        }

        return executeSpec
                .map((row, metadata) -> mapRowToResult(row, metadata, resultType))
                .one();
    }

    /**
     * 执行查询语句，返回列表结果
     */
    public <T> Flux<T> selectList(String statementId, Map<String, Object> parameters, Class<T> resultType) {
        SqlStatement sqlStatement = XmlMapperParser.getSqlStatement(statementId);
        if (sqlStatement == null) {
            throw new RuntimeException("SQL statement not found: " + statementId);
        }

        if (sqlStatement.getStatementType() != SqlStatement.StatementType.SELECT) {
            throw new RuntimeException("Statement is not a SELECT: " + statementId);
        }

        String sql = sqlStatement.processParameters(parameters);

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);
        if (parameters != null) {
            // 只绑定SQL中实际存在的参数
            executeSpec = bindActualParameters(executeSpec, sql, parameters);
        }

        return executeSpec
                .map((row, metadata) -> mapRowToResult(row, metadata, resultType))
                .all();
    }

    /**
     * 执行插入语句
     */
    public Mono<Integer> insert(String statementId, Map<String, Object> parameters) {
        return executeUpdate(statementId, parameters, SqlStatement.StatementType.INSERT);
    }

    /**
     * 执行更新语句
     */
    public Mono<Integer> update(String statementId, Map<String, Object> parameters) {
        return executeUpdate(statementId, parameters, SqlStatement.StatementType.UPDATE);
    }

    /**
     * 执行删除语句
     */
    public Mono<Integer> delete(String statementId, Map<String, Object> parameters) {
        return executeUpdate(statementId, parameters, SqlStatement.StatementType.DELETE);
    }

    /**
     * 执行更新类型的语句（INSERT、UPDATE、DELETE）
     */
    private Mono<Integer> executeUpdate(String statementId, Map<String, Object> parameters, SqlStatement.StatementType expectedType) {
        SqlStatement sqlStatement = XmlMapperParser.getSqlStatement(statementId);
        if (sqlStatement == null) {
            throw new RuntimeException("SQL statement not found: " + statementId);
        }

        if (sqlStatement.getStatementType() != expectedType) {
            throw new RuntimeException("Statement type mismatch. Expected: " + expectedType + ", Actual: " + sqlStatement.getStatementType());
        }

        String sql = sqlStatement.processParameters(parameters);

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);
        if (parameters != null) {
            // 只绑定SQL中实际存在的参数
            executeSpec = bindActualParameters(executeSpec, sql, parameters);
        }

        return executeSpec
                .fetch()
                .rowsUpdated().map(Long::intValue);
    }

    /**
     * 执行任意 SQL 语句
     */
    public Mono<Integer> execute(String statementId, Map<String, Object> parameters) {
        SqlStatement sqlStatement = XmlMapperParser.getSqlStatement(statementId);
        if (sqlStatement == null) {
            throw new RuntimeException("SQL statement not found: " + statementId);
        }

        String sql = sqlStatement.processParameters(parameters);

        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);
        if (parameters != null) {
            // 只绑定SQL中实际存在的参数
            executeSpec = bindActualParameters(executeSpec, sql, parameters);
        }

        return executeSpec
                .fetch()
                .rowsUpdated().map(Long::intValue);
    }

    /**
     * 执行原生 SQL 查询
     */
    public <T> Flux<T> selectListBySql(String sql, Map<String, Object> parameters, Class<T> resultType) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);
        if (parameters != null) {
            // 只绑定SQL中实际存在的参数
            executeSpec = bindActualParameters(executeSpec, sql, parameters);
        }

        return executeSpec
                .map((row, metadata) -> r2dbcEntityTemplate.getConverter().read(resultType, row, metadata))
                .all();
    }

    /**
     * 执行原生 SQL 查询，返回单个结果
     */
    public <T> Mono<T> selectOneBySql(String sql, Map<String, Object> parameters, Class<T> resultType) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);
        if (parameters != null) {
            // 只绑定SQL中实际存在的参数
            executeSpec = bindActualParameters(executeSpec, sql, parameters);
        }

        return executeSpec
                .map((row, metadata) -> mapRowToResult(row, metadata, resultType))
                .one();
    }

    /**
     * 执行分页查询
     */
    public <T> Mono<Page<T>> selectPage(String statementId, Map<String, Object> parameters, Page<T> page, Class<T> resultType) {
        SqlStatement sqlStatement = XmlMapperParser.getSqlStatement(statementId);
        if (sqlStatement == null) {
            throw new RuntimeException("SQL statement not found: " + statementId);
        }

        if (sqlStatement.getStatementType() != SqlStatement.StatementType.SELECT) {
            throw new RuntimeException("Statement is not a SELECT: " + statementId);
        }

        String originalSql = sqlStatement.processParameters(parameters);

        // 构建 COUNT 查询 SQL
        String countSql = buildCountSql(originalSql);

        // 构建分页数据查询 SQL
        String dataSql = buildPageDataSql(originalSql, page);

        // 扁平化参数
        Map<String, Object> flattenedParams = flattenParameters(parameters);

        // 先执行 COUNT 查询
        return selectOneBySql(countSql, flattenedParams, Long.class)
                .flatMap(total -> {
                    page.setTotal(total);

                    if (total == 0) {
                        page.setRecords(java.util.Collections.emptyList());
                        return Mono.just(page);
                    }

                    // 执行数据查询
                    return selectListBySql(dataSql, flattenedParams, resultType)
                            .collectList()
                            .map(records -> {
                                page.setRecords(records);
                                return page;
                            });
                });
    }

    /**
     * 构建 COUNT 查询 SQL
     */
    private String buildCountSql(String originalSql) {
        String countSql = removeMainQueryOrderByAndLimit(originalSql);
        
        // 如果有 GROUP BY，需要包装为子查询
        if (countSql.toLowerCase().contains("group by")) {
            return "SELECT COUNT(*) FROM (" + countSql + ") tmp_count";
        } else {
            // 使用新的方法精确定位外层 SELECT 到 FROM 的范围
            int[] range = findSelectToFromRange(countSql);
            if (range != null) {
                // 替换 SELECT...FROM 为 SELECT COUNT(*) FROM
                StringBuilder result = new StringBuilder();
                result.append(countSql.substring(0, range[0]));
                result.append("SELECT COUNT(*) FROM");
                result.append(countSql.substring(range[1]));
                countSql = result.toString();
            }
        }

        return countSql;
    }

    /**
     * 查找外层 SELECT 到 FROM 的范围
     * 借鉴 removeMainQueryOrderByAndLimit 的逻辑，确保只匹配主查询层的 SELECT...FROM
     */
    private int[] findSelectToFromRange(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return null;
        }
        
        String upperSql = sql.toUpperCase();
        char[] chars = sql.toCharArray();
        int length = chars.length;
        
        int parenthesesDepth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        
        int selectStart = -1;
        int fromStart = -1;
        
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            
            // 处理引号状态
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                continue;
            }
            if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                continue;
            }
            
            // 在引号内，跳过所有字符
            if (inSingleQuote || inDoubleQuote) {
                continue;
            }
            
            // 处理括号深度
            if (c == '(') {
                parenthesesDepth++;
                continue;
            }
            if (c == ')') {
                parenthesesDepth--;
                continue;
            }
            
            // 只在主查询层（括号深度为0）查找关键字
            if (parenthesesDepth == 0) {
                // 查找 SELECT 关键字
                if (selectStart == -1 && isKeywordAt(upperSql, i, "SELECT")) {
                    selectStart = i;
                    i += 5; // 跳过 "SELECT" 的剩余字符
                    continue;
                }
                
                // 查找 FROM 关键字（必须在找到 SELECT 之后）
                if (selectStart != -1 && fromStart == -1 && isKeywordAt(upperSql, i, "FROM")) {
                    fromStart = i;
                    break; // 找到第一个主查询层的 FROM，结束搜索
                }
            }
        }
        
        // 如果找到了 SELECT 和 FROM，返回范围
        if (selectStart != -1 && fromStart != -1) {
            return new int[]{selectStart, fromStart + 4}; // fromStart + "FROM".length()
        }
        
        return null;
    }

    /**
     * 移除主查询层的 ORDER BY 和 LIMIT 子句
     */
    private String removeMainQueryOrderByAndLimit(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }
        
        StringBuilder result = new StringBuilder();
        String upperSql = sql.toUpperCase();
        char[] chars = sql.toCharArray();
        int length = chars.length;
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        
        for (int i = 0; i < length; i++) {
            char c = chars[i];
            
            if (!inSingleQuote && !inDoubleQuote) {
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                } else if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (depth == 0) {
                    // 检查是否是 ORDER BY 关键字（只在主查询层级）
                    if (isKeywordAt(upperSql, i, "ORDER BY")) {
                        // 跳过整个 ORDER BY 子句直到遇到 LIMIT 或结束
                        i = skipToNextMainClause(upperSql, i + 8); // 8 是 "ORDER BY" 的长度
                        continue;
                    }
                    // 检查是否是 LIMIT 关键字（只在主查询层级）
                    if (isKeywordAt(upperSql, i, "LIMIT")) {
                        // 跳过整个 LIMIT 子句直到结束
                        i = length; // 直接跳到结束
                        break;
                    }
                }
            } else if (inSingleQuote && c == '\'') {
                inSingleQuote = false;
            } else if (inDoubleQuote && c == '"') {
                inDoubleQuote = false;
            }
            
            result.append(c);
        }
        
        return result.toString().trim();
    }
    
    /**
     * 检查指定位置是否是指定的关键字
     */
    private boolean isKeywordAt(String upperSql, int pos, String keyword) {
        // 检查位置是否有效
        if (pos + keyword.length() > upperSql.length()) {
            return false;
        }
        
        // 检查关键字是否匹配
        if (!upperSql.substring(pos, pos + keyword.length()).equals(keyword)) {
            return false;
        }
        
        // 检查前面是否是单词边界（空白字符或开头）
        if (pos > 0 && Character.isLetterOrDigit(upperSql.charAt(pos - 1))) {
            return false;
        }
        
        // 检查后面是否是单词边界（空白字符或结尾）
        int endPos = pos + keyword.length();
        if (endPos < upperSql.length() && Character.isLetterOrDigit(upperSql.charAt(endPos))) {
            return false;
        }
        
        return true;
    }
    
    private int skipToNextMainClause(String upperSql, int pos) {
        int depth = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        
        for (int i = pos; i < upperSql.length(); i++) {
            char c = upperSql.charAt(i);
            
            if (!inSingleQuote && !inDoubleQuote) {
                if (c == '(') {
                    depth++;
                } else if (c == ')') {
                    depth--;
                } else if (c == '\'') {
                    inSingleQuote = true;
                } else if (c == '"') {
                    inDoubleQuote = true;
                } else if (depth == 0) {
                    // 检查是否遇到 LIMIT 关键字
                    if (isKeywordAt(upperSql, i, "LIMIT")) {
                        return i - 1; // 返回 LIMIT 前一个位置
                    }
                }
            } else if (inSingleQuote && c == '\'') {
                inSingleQuote = false;
            } else if (inDoubleQuote && c == '"') {
                inDoubleQuote = false;
            }
        }
        
        return upperSql.length() - 1;
    }
    


    /**
     * 构建分页数据查询 SQL
     */
    private String buildPageDataSql(String originalSql, Page<?> page) {
        long offset = page.getOffset();
        long limit = page.getSize();

        return originalSql + " LIMIT " + limit + " OFFSET " + offset;
    }

    /**
     * 执行原生 SQL 更新
     */
    public Mono<Integer> executeSql(String sql, Map<String, Object> parameters) {
        DatabaseClient.GenericExecuteSpec executeSpec = databaseClient.sql(sql);
        if (parameters != null) {
            // 使用bind方法逐个绑定参数
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                executeSpec = executeSpec.bind(entry.getKey(), entry.getValue());
            }
        }

        return executeSpec
                .fetch()
                .rowsUpdated().map(Long::intValue);
    }

    /**
     * 只绑定SQL中实际存在的参数，避免绑定不存在的参数导致错误
     */
    private DatabaseClient.GenericExecuteSpec bindActualParameters(
            DatabaseClient.GenericExecuteSpec executeSpec,
            String sql,
            Map<String, Object> parameters) {

        // 从SQL中提取实际存在的参数名
        java.util.Set<String> actualParams = extractParameterNames(sql);

        // 检查参数是否已经扁平化
        Map<String, Object> flattenedParams;
        if (isAlreadyFlattened(parameters)) {
            flattenedParams = parameters;
        } else {
            // 扁平化嵌套参数
            flattenedParams = flattenParameters(parameters);
        }

        // 只绑定SQL中实际存在的参数
        for (Map.Entry<String, Object> entry : flattenedParams.entrySet()) {
            if (actualParams.contains(entry.getKey())) {
                if (entry.getValue() == null) {
                    executeSpec = executeSpec.bindNull(entry.getKey(), Object.class);
                } else {
                    executeSpec = executeSpec.bind(entry.getKey(), entry.getValue());
                }

            }
        }

        return executeSpec;
    }

    /**
     * 检查参数是否已经扁平化
     */
    private boolean isAlreadyFlattened(Map<String, Object> parameters) {
        // 检查是否存在嵌套对象类型的参数，如果存在说明需要扁平化
        // foreach生成的item_*参数不应该影响这个判断
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            Object value = entry.getValue();
            // 如果存在非基本类型的复杂对象，说明需要扁平化
            if (value != null && !isPrimitiveOrWrapper(value.getClass()) 
                && !value.getClass().equals(String.class)
                && !(value instanceof Collection)
                && !(value instanceof Map)) {
                return false; // 需要扁平化
            }
        }
        return true; // 已经扁平化或者只包含基本类型
    }

    /**
     * 从SQL语句中提取参数名
     */
    private java.util.Set<String> extractParameterNames(String sql) {
        java.util.Set<String> paramNames = new java.util.HashSet<>();
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(":(\\w+)");
        java.util.regex.Matcher matcher = pattern.matcher(sql);

        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }

        return paramNames;
    }

    /**
     * 扁平化嵌套参数
     * 例如：{"user": {"id": 1, "name": "张三"}} -> {"user_id": 1, "user_name": "张三"}
     * 或者：{"dto": dtoObject} -> {"dto_pluginId": 1L, "dto_account": "test"}
     */
    public Map<String, Object> flattenParameters(Map<String, Object> parameters) {
        Map<String, Object> flattened = new HashMap<>();

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                // 处理嵌套Map
                @SuppressWarnings("unchecked")
                Map<String, Object> nestedMap = (Map<String, Object>) value;
                for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
                    String flatKey = key + "_" + nestedEntry.getKey();
                    flattened.put(flatKey, nestedEntry.getValue());
                }
            } else if (value != null && !isPrimitiveOrWrapper(value.getClass()) && !value.getClass().equals(String.class)) {
                // 跳过 Page 对象，不进行扁平化
                if (value instanceof Page) {
                    // Page 对象不需要扁平化，直接跳过
                    continue;
                }

                // 处理对象属性，通过反射获取所有getter方法
                try {
                    java.lang.reflect.Method[] methods = value.getClass().getMethods();
                    for (java.lang.reflect.Method method : methods) {
                        String methodName = method.getName();
                        // 检查是否是getter方法
                        if (methodName.startsWith("get") && methodName.length() > 3 &&
                                method.getParameterCount() == 0 && !methodName.equals("getClass")) {

                            // 提取属性名：getPluginId -> pluginId
                            String propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                            String flatKey = key + "_" + propertyName;

                            Object propertyValue = method.invoke(value);
                            flattened.put(flatKey, propertyValue);
                        }
                    }
                } catch (Exception e) {
                    // 如果反射失败，直接添加原始对象
                    flattened.put(key, value);
                }
            } else {
                // 普通参数直接添加
                flattened.put(key, value);
            }
        }

        return flattened;
    }

    /**
     * 检查是否是基本类型或包装类型
     */
    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive() ||
                type.equals(Boolean.class) || type.equals(Character.class) ||
                type.equals(Byte.class) || type.equals(Short.class) ||
                type.equals(Integer.class) || type.equals(Long.class) ||
                type.equals(Float.class) || type.equals(Double.class);
    }

    /**
     * 将数据库行映射到结果对象
     */
    @SuppressWarnings("unchecked")
    private <T> T mapRowToResult(io.r2dbc.spi.Row row, io.r2dbc.spi.RowMetadata metadata, Class<T> resultType) {
        // 如果是Map类型，手动构建Map
        if (java.util.Map.class.isAssignableFrom(resultType)) {
            java.util.Map<String, Object> result = new java.util.HashMap<>();

            // 遍历所有列
            for (io.r2dbc.spi.ColumnMetadata columnMetadata : metadata.getColumnMetadatas()) {
                String columnName = columnMetadata.getName();
                Object value = row.get(columnName);
                result.put(columnName, value);
            }

            return (T) result;
        }

        // 对于其他类型，使用R2DBC的转换器
        try {
            return r2dbcEntityTemplate.getConverter().read(resultType, row, metadata);
        } catch (Exception e) {
            // 如果转换失败，尝试基本类型转换
            if (resultType.isPrimitive() || isWrapperType(resultType)) {
                // 对于基本类型，取第一列的值
                Object value = row.get(0);
                return convertToType(value, resultType);
            }
            throw new RuntimeException("Failed to map row to result type: " + resultType.getName(), e);
        }
    }

    /**
     * 判断是否为包装类型
     */
    private boolean isWrapperType(Class<?> type) {
        return type == Boolean.class || type == Byte.class || type == Character.class ||
                type == Short.class || type == Integer.class || type == Long.class ||
                type == Float.class || type == Double.class || type == String.class;
    }

    /**
     * 将值转换为指定类型
     */
    @SuppressWarnings("unchecked")
    private <T> T convertToType(Object value, Class<T> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return (T) value;
        }

        // 基本类型转换
        if (targetType == String.class) {
            return (T) value.toString();
        } else if (targetType == Integer.class || targetType == int.class) {
            return (T) Integer.valueOf(value.toString());
        } else if (targetType == Long.class || targetType == long.class) {
            return (T) Long.valueOf(value.toString());
        } else if (targetType == Boolean.class || targetType == boolean.class) {
            // 智能 Boolean 转换：支持数字和字符串
            if (value instanceof Number) {
                return (T) Boolean.valueOf(((Number) value).intValue() != 0);
            } else {
                String strValue = value.toString().toLowerCase();
                return (T) Boolean.valueOf("true".equals(strValue) || "1".equals(strValue));
            }
        } else if (targetType == Double.class || targetType == double.class) {
            return (T) Double.valueOf(value.toString());
        } else if (targetType == Float.class || targetType == float.class) {
            return (T) Float.valueOf(value.toString());
        }

        return (T) value;
    }
}
