package com.scrm.r2dbc.plus.xml;

import org.w3c.dom.Element;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 语句封装类
 *
 * @author dason
 */
public class SqlStatement {

    private String id;
    private String sql;
    private String resultType;
    private String parameterType;
    private StatementType statementType;
    private Element sqlElement; // 保存原始 XML 元素用于动态 SQL 处理
    private boolean isDynamic = false; // 是否包含动态 SQL

    private static final Pattern DYNAMIC_PATTERN = Pattern.compile("<(if|choose|when|otherwise|where|set|foreach|trim)\\b");

    public enum StatementType {
        SELECT, INSERT, UPDATE, DELETE
    }
    
    public SqlStatement() {}
    
    public SqlStatement(String id, String sql, StatementType statementType) {
        this.id = id;
        this.sql = sql;
        this.statementType = statementType;
        this.isDynamic = checkIfDynamic(sql);
    }

    public SqlStatement(String id, Element sqlElement, StatementType statementType) {
        this.id = id;
        this.sqlElement = sqlElement;
        this.statementType = statementType;
        this.isDynamic = true; // XML 元素默认认为是动态的
    }

    /**
     * 检查 SQL 是否包含动态标签
     */
    private boolean checkIfDynamic(String sql) {
        if (sql == null) return false;
        Matcher matcher = DYNAMIC_PATTERN.matcher(sql);
        return matcher.find();
    }

    /**
     * 处理参数占位符和动态 SQL
     */
    public String processParameters(Map<String, Object> parameters) {
        String processedSql;

        if (isDynamic && sqlElement != null) {
            // 处理动态 SQL
            processedSql = DynamicSqlProcessor.processDynamicSql(sqlElement, parameters);
        } else {
            // 使用静态 SQL
            processedSql = sql;
        }

        // 处理参数占位符，支持嵌套参数
        if (parameters != null) {
            processedSql = processParameterPlaceholders(processedSql, parameters);
        }

        return processedSql.trim();
    }

    /**
     * 处理参数占位符，支持嵌套参数
     */
    private String processParameterPlaceholders(String sql, Map<String, Object> parameters) {
        // 使用正则表达式找到所有的 #{...} 占位符
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("#\\{([^}]+)\\}");
        java.util.regex.Matcher matcher = pattern.matcher(sql);

        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String paramPath = matcher.group(1); // 例如 "user.id"

            // 检查参数是否存在（支持嵌套参数）
            if (parameterExists(parameters, paramPath)) {
                String replacement = ":" + paramPath.replace(".", "_"); // 转换为 ":user_id"
                matcher.appendReplacement(result, replacement);
            } else {
                // 参数不存在，保持原样
                matcher.appendReplacement(result, "#{" + paramPath + "}");
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 检查参数是否存在，支持嵌套参数路径
     */
    private boolean parameterExists(Map<String, Object> parameters, String paramPath) {
        if (parameters == null || paramPath == null) {
            return false;
        }

        // 如果是简单参数（不包含点号）
        if (!paramPath.contains(".")) {
            return parameters.containsKey(paramPath);
        }

        // 处理嵌套参数，例如 "dto.pluginId"
        String[] parts = paramPath.split("\\.");
        Object current = parameters;

        for (String part : parts) {
            if (current instanceof Map) {
                Map<String, Object> currentMap = (Map<String, Object>) current;
                if (!currentMap.containsKey(part)) {
                    return false;
                }
                current = currentMap.get(part);
            } else {
                // 支持对象属性访问
                try {
                    if (current == null) {
                        return false;
                    }
                    
                    // 尝试通过getter方法获取属性值
                    String getterName = "get" + Character.toUpperCase(part.charAt(0)) + part.substring(1);
                    java.lang.reflect.Method getter = current.getClass().getMethod(getterName);
                    current = getter.invoke(current);
                    // 属性存在，继续检查下一级
                } catch (Exception e) {
                    // 如果无法通过反射访问属性，返回false
                    return false;
                }
            }
        }

        return true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSql() {
        return sql;
    }
    
    public void setSql(String sql) {
        this.sql = sql;
    }
    
    public String getResultType() {
        return resultType;
    }
    
    public void setResultType(String resultType) {
        this.resultType = resultType;
    }
    
    public String getParameterType() {
        return parameterType;
    }
    
    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }
    
    public StatementType getStatementType() {
        return statementType;
    }
    
    public void setStatementType(StatementType statementType) {
        this.statementType = statementType;
    }

    public Element getSqlElement() {
        return sqlElement;
    }

    public void setSqlElement(Element sqlElement) {
        this.sqlElement = sqlElement;
        this.isDynamic = true;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

    public void setDynamic(boolean dynamic) {
        isDynamic = dynamic;
    }

    @Override
    public String toString() {
        return "SqlStatement{" +
                "id='" + id + '\'' +
                ", statementType=" + statementType +
                ", isDynamic=" + isDynamic +
                ", sql='" + (sql != null ? sql.substring(0, Math.min(sql.length(), 50)) + "..." : "null") + '\'' +
                '}';
    }
}
