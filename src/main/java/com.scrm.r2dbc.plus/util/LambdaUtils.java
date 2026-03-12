package com.scrm.r2dbc.plus.util;

import com.scrm.r2dbc.plus.function.SFunction;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lambda 工具类，用于解析 Lambda 表达式获取字段名
 * 
 * @author dason
 */
public class LambdaUtils {

    private static final Map<String, String> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取 Lambda 表达式对应的字段名
     *
     * @param function Lambda 表达式
     * @param <T> 实体类型
     * @return 字段名
     */
    public static <T> String getFieldName(SFunction<T, ?> function) {
        String key = function.getClass().getName();
        return FIELD_CACHE.computeIfAbsent(key, k -> {
            try {
                Method method = function.getClass().getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                SerializedLambda serializedLambda = (SerializedLambda) method.invoke(function);
                String methodName = serializedLambda.getImplMethodName();
                
                // 处理 getter 方法名
                if (methodName.startsWith("get")) {
                    methodName = methodName.substring(3);
                } else if (methodName.startsWith("is")) {
                    methodName = methodName.substring(2);
                }
                
                // 首字母小写
                return Character.toLowerCase(methodName.charAt(0)) + methodName.substring(1);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to get field name from lambda", e);
            }
        });
    }

    /**
     * 将驼峰命名转换为下划线命名
     *
     * @param camelCase 驼峰命名
     * @return 下划线命名
     */
    public static String camelToUnderscore(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 获取数据库字段名（驼峰转下划线）
     *
     * @param function Lambda 表达式
     * @param <T> 实体类型
     * @return 数据库字段名
     */
    public static <T> String getColumnName(SFunction<T, ?> function) {
        String fieldName = getFieldName(function);
        return camelToUnderscore(fieldName);
    }
}
