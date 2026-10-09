package com.scrm.r2dbc.plus.xml;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动态 SQL 处理器，支持 if、choose、where、foreach 等标签
 * 
 * @author dason
 */
public class DynamicSqlProcessor {
    
    private static final ExpressionParser parser = new SpelExpressionParser();
    private static final Pattern PARAM_PATTERN = Pattern.compile("#\\{([^}]+)\\}");
    
    /**
     * 处理动态 SQL
     */
    public static String processDynamicSql(Element element, Map<String, Object> parameters) {
        StringBuilder sql = new StringBuilder();
        processNode(element, parameters, sql);
        return sql.toString().trim();
    }
    
    /**
     * 递归处理节点
     */
    private static void processNode(Node node, Map<String, Object> parameters, StringBuilder sql) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getTextContent();
            if (text != null && !text.trim().isEmpty()) {
                sql.append(text);
            }
            return;
        }
        
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }
        
        Element element = (Element) node;
        String tagName = element.getTagName().toLowerCase();
        
        switch (tagName) {
            case "if":
                processIfTag(element, parameters, sql);
                break;
            case "choose":
                processChooseTag(element, parameters, sql);
                break;
            case "when":
                processWhenTag(element, parameters, sql);
                break;
            case "otherwise":
                processOtherwiseTag(element, parameters, sql);
                break;
            case "where":
                processWhereTag(element, parameters, sql);
                break;
            case "set":
                processSetTag(element, parameters, sql);
                break;
            case "foreach":
                processForeachTag(element, parameters, sql);
                break;
            case "trim":
                processTrimTag(element, parameters, sql);
                break;
            default:
                // 对于其他标签，直接处理子节点
                processChildNodes(element, parameters, sql);
                break;
        }
    }
    
    /**
     * 处理 if 标签
     */
    private static void processIfTag(Element element, Map<String, Object> parameters, StringBuilder sql) {
        String test = element.getAttribute("test");
        if (evaluateCondition(test, parameters)) {
            processChildNodes(element, parameters, sql);
        }
    }
    
    /**
     * 处理 choose 标签
     */
    private static void processChooseTag(Element element, Map<String, Object> parameters, StringBuilder sql) {
        NodeList children = element.getChildNodes();
        boolean matched = false;
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String tagName = childElement.getTagName().toLowerCase();
                
                if ("when".equals(tagName) && !matched) {
                    String test = childElement.getAttribute("test");
                    if (evaluateCondition(test, parameters)) {
                        processChildNodes(childElement, parameters, sql);
                        matched = true;
                    }
                } else if ("otherwise".equals(tagName) && !matched) {
                    processChildNodes(childElement, parameters, sql);
                    matched = true;
                }
            }
        }
    }
    
    /**
     * 处理 when 标签（在 choose 中处理）
     */
    private static void processWhenTag(Element element, Map<String, Object> parameters, StringBuilder sql) {
        // 在 choose 标签中处理
    }
    
    /**
     * 处理 otherwise 标签（在 choose 中处理）
     */
    private static void processOtherwiseTag(Element element, Map<String, Object> parameters, StringBuilder sql) {
        // 在 choose 标签中处理
    }
    
    /**
     * 处理 where 标签
     */
    private static void processWhereTag(Element element, Map<String, Object> parameters, StringBuilder sql) {
        StringBuilder whereContent = new StringBuilder();
        processChildNodes(element, parameters, whereContent);
        
        String content = whereContent.toString().trim();
        if (!content.isEmpty()) {
            // 移除开头的 AND 或 OR
            content = content.replaceFirst("^\\s*(AND|OR)\\s+", "");
            if (!content.isEmpty()) {
                sql.append(" WHERE ").append(content);
            }
        }
    }
    
    /**
     * 处理 set 标签
     */
    private static void processSetTag(Element element, Map<String, Object> parameters, StringBuilder sql) {
        StringBuilder setContent = new StringBuilder();
        processChildNodes(element, parameters, setContent);
        
        String content = setContent.toString().trim();
        if (!content.isEmpty()) {
            // 移除末尾的逗号
            content = content.replaceAll(",\\s*$", "");
            if (!content.isEmpty()) {
                sql.append(" SET ").append(content);
            }
        }
    }
    
    /**
     * 处理 foreach 标签
     */
    private static void processForeachTag(Element element, Map<String, Object> parameters, StringBuilder sql) {
        String collection = element.getAttribute("collection");
        String item = element.getAttribute("item");
        String index = element.getAttribute("index");
        String open = element.getAttribute("open");
        String close = element.getAttribute("close");
        String separator = element.getAttribute("separator");
        
        Object collectionObj = getParameterValue(collection, parameters);
        if (collectionObj == null) {
            return;
        }
        
        Collection<?> items;
        if (collectionObj instanceof Collection) {
            items = (Collection<?>) collectionObj;
        } else if (collectionObj instanceof Object[]) {
            items = Arrays.asList((Object[]) collectionObj);
        } else {
            return;
        }
        
        if (items.isEmpty()) {
            return;
        }
        
        if (open != null && !open.isEmpty()) {
            sql.append(open);
        }
        
        boolean first = true;
        int idx = 0;
        for (Object itemValue : items) {
            if (!first && separator != null && !separator.isEmpty()) {
                sql.append(separator);
            }
            
            // 创建新的参数上下文
            Map<String, Object> foreachParams = new HashMap<>(parameters);
            if (item != null && !item.isEmpty()) {
                // 为每个迭代项生成唯一的参数名
                String uniqueItemKey = item + "_" + idx;
                foreachParams.put(uniqueItemKey, itemValue);
                // 保持原参数名以支持现有逻辑
                foreachParams.put(item, itemValue);
                
                // 将唯一参数名添加到主参数映射中，确保在SQL执行时能够正确绑定
                parameters.put(uniqueItemKey, itemValue);
            }
            if (index != null && !index.isEmpty()) {
                foreachParams.put(index, idx);
                // 同样将索引参数添加到主参数映射中
                String uniqueIndexKey = index + "_" + idx;
                parameters.put(uniqueIndexKey, idx);
            }
            
            // 处理 foreach 内容
            StringBuilder foreachContent = new StringBuilder();
            processChildNodes(element, foreachParams, foreachContent);
            
            // 替换参数名为唯一的参数名
            String content = foreachContent.toString();
            if (item != null && !item.isEmpty()) {
                String uniqueItemKey = item + "_" + idx;
                content = content.replace("#{" + item + "}", "#{" + uniqueItemKey + "}");
            }
            sql.append(content);
            
            first = false;
            idx++;
        }
        
        if (close != null && !close.isEmpty()) {
            sql.append(close);
        }
    }
    
    /**
     * 处理 trim 标签
     */
    private static void processTrimTag(Element element, Map<String, Object> parameters, StringBuilder sql) {
        String prefix = element.getAttribute("prefix");
        String suffix = element.getAttribute("suffix");
        String prefixOverrides = element.getAttribute("prefixOverrides");
        String suffixOverrides = element.getAttribute("suffixOverrides");
        
        StringBuilder trimContent = new StringBuilder();
        processChildNodes(element, parameters, trimContent);
        
        String content = trimContent.toString().trim();
        if (!content.isEmpty()) {
            // 处理前缀覆盖
            if (prefixOverrides != null && !prefixOverrides.isEmpty()) {
                String[] overrides = prefixOverrides.split("\\|");
                for (String override : overrides) {
                    String pattern = "^\\s*" + Pattern.quote(override.trim()) + "\\s*";
                    content = content.replaceFirst(pattern, "");
                }
            }
            
            // 处理后缀覆盖
            if (suffixOverrides != null && !suffixOverrides.isEmpty()) {
                String[] overrides = suffixOverrides.split("\\|");
                for (String override : overrides) {
                    String pattern = "\\s*" + Pattern.quote(override.trim()) + "\\s*$";
                    content = content.replaceFirst(pattern, "");
                }
            }
            
            // 添加前缀和后缀
            if (prefix != null && !prefix.isEmpty()) {
                content = prefix + " " + content;
            }
            if (suffix != null && !suffix.isEmpty()) {
                content = content + " " + suffix;
            }
            
            sql.append(content);
        }
    }
    
    /**
     * 处理子节点
     */
    private static void processChildNodes(Element element, Map<String, Object> parameters, StringBuilder sql) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            processNode(children.item(i), parameters, sql);
        }
    }
    
    /**
     * 评估条件表达式
     */
    private static boolean evaluateCondition(String condition, Map<String, Object> parameters) {
        if (condition == null || condition.trim().isEmpty()) {
            return true;
        }
        
        try {
            // 简单的条件处理
            condition = condition.trim();
            
            // 处理 null 检查 (支持嵌套参数如 qo.name)
            if (condition.matches("[\\w.]+\\s*!=\\s*null")) {
                String paramName = condition.replaceAll("\\s*!=\\s*null", "");
                Object value = getParameterValue(paramName, parameters);
                return value != null;
            }

            if (condition.matches("[\\w.]+\\s*==\\s*null")) {
                String paramName = condition.replaceAll("\\s*==\\s*null", "");
                Object value = getParameterValue(paramName, parameters);
                return value == null;
            }

            // 处理简单的存在性检查 (支持嵌套参数如 qo.name)
            if (condition.matches("[\\w.]+")) {
                Object value = getParameterValue(condition, parameters);
                if (value == null) return false;
                if (value instanceof Boolean) return (Boolean) value;
                if (value instanceof String) return !((String) value).isEmpty();
                if (value instanceof Collection) return !((Collection<?>) value).isEmpty();
                return true;
            }
            
            // 处理复合条件，如 "uuid != null and uuid != ''"
            if (condition.contains(" and ") || condition.contains(" or ")) {
                return evaluateComplexCondition(condition, parameters);
            }

            // 使用 SpEL 表达式解析器处理简单条件
            StandardEvaluationContext context = new StandardEvaluationContext();
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                context.setVariable(entry.getKey(), entry.getValue());
            }

            // 只替换参数名，不替换关键字
            String spelExpression = replaceParameterNames(condition, parameters.keySet());
            Expression expression = parser.parseExpression(spelExpression);
            Object result = expression.getValue(context);

            return result instanceof Boolean ? (Boolean) result : (result != null);
        } catch (Exception e) {
            // 如果解析失败，返回 false
            return false;
        }
    }

    /**
     * 评估复合条件，如 "uuid != null and uuid != ''"
     */
    private static boolean evaluateComplexCondition(String condition, Map<String, Object> parameters) {
        try {
            // 处理 and 连接的条件
            if (condition.contains(" and ")) {
                String[] parts = condition.split(" and ");
                for (String part : parts) {
                    if (!evaluateSimpleCondition(part.trim(), parameters)) {
                        return false;
                    }
                }
                return true;
            }

            // 处理 or 连接的条件
            if (condition.contains(" or ")) {
                String[] parts = condition.split(" or ");
                for (String part : parts) {
                    if (evaluateSimpleCondition(part.trim(), parameters)) {
                        return true;
                    }
                }
                return false;
            }

            return evaluateSimpleCondition(condition, parameters);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 评估简单条件，如 "uuid != null" 或 "uuid != ''"
     */
    private static boolean evaluateSimpleCondition(String condition, Map<String, Object> parameters) {
        condition = condition.trim();

        // 处理 != null (支持嵌套参数如 qo.name)
        if (condition.matches("[\\w.]+\\s*!=\\s*null")) {
            String paramName = condition.replaceAll("\\s*!=\\s*null", "");
            Object value = getParameterValue(paramName, parameters);
            return value != null;
        }

        // 处理 == null (支持嵌套参数如 qo.name)
        if (condition.matches("[\\w.]+\\s*==\\s*null")) {
            String paramName = condition.replaceAll("\\s*==\\s*null", "");
            Object value = getParameterValue(paramName, parameters);
            return value == null;
        }

        // 处理 != '' (支持嵌套参数如 qo.name)
        if (condition.matches("[\\w.]+\\s*!=\\s*''")) {
            String paramName = condition.replaceAll("\\s*!=\\s*''", "");
            Object value = getParameterValue(paramName, parameters);
            return value != null && !value.toString().isEmpty();
        }

        // 处理 == '' (支持嵌套参数如 qo.name)
        if (condition.matches("[\\w.]+\\s*==\\s*''")) {
            String paramName = condition.replaceAll("\\s*==\\s*''", "");
            Object value = getParameterValue(paramName, parameters);
            return value == null || value.toString().isEmpty();
        }

        // 处理简单的存在性检查
        if (condition.matches("\\w+")) {
            Object value = getParameterValue(condition, parameters);
            if (value == null) return false;
            if (value instanceof Boolean) return (Boolean) value;
            if (value instanceof String) return !((String) value).isEmpty();
            if (value instanceof java.util.Collection) return !((java.util.Collection<?>) value).isEmpty();
            return true;
        }

        return false;
    }

    /**
     * 替换参数名为SpEL变量引用，但保留关键字
     */
    private static String replaceParameterNames(String condition, java.util.Set<String> paramNames) {
        String result = condition;
        for (String paramName : paramNames) {
            // 使用单词边界确保完整匹配参数名
            result = result.replaceAll("\\b" + paramName + "\\b", "#" + paramName);
        }
        return result;
    }

    /**
     * 获取参数值，支持嵌套属性
     */
    private static Object getParameterValue(String paramName, Map<String, Object> parameters) {
        if (paramName == null || paramName.trim().isEmpty()) {
            return null;
        }
        
        paramName = paramName.trim();
        
        // 处理简单参数
        if (!paramName.contains(".")) {
            return parameters.get(paramName);
        }
        
        // 处理嵌套参数（如 user.name）
        String[] parts = paramName.split("\\.");
        Object current = parameters.get(parts[0]);
        
        for (int i = 1; i < parts.length && current != null; i++) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(parts[i]);
            } else {
                // 使用反射获取属性值
                try {
                    String fieldName = parts[i];
                    String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
                    current = current.getClass().getMethod(getterName).invoke(current);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        
        return current;
    }
}
