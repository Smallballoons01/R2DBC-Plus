package com.scrm.r2dbc.plus.logic;

import com.scrm.r2dbc.plus.annotation.TableLogic;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 逻辑删除处理器
 * 专门处理逻辑删除相关逻辑
 * 
 * @author dason
 */
@Component
public class LogicDeleteProcessor {

    /**
     * 逻辑删除字段信息缓存
     */
    private static final ConcurrentHashMap<Class<?>, LogicDeleteFieldInfo> LOGIC_DELETE_CACHE = new ConcurrentHashMap<>();

    /**
     * 逻辑删除信息缓存（对外暴露的对象）
     */
    private static final ConcurrentHashMap<Class<?>, LogicDeleteInfo> LOGIC_DELETE_INFO_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 处理插入时的逻辑删除字段初始化
     */
    public void processInsert(Object entity) {
        if (entity == null) {
            return;
        }

        LogicDeleteFieldInfo fieldInfo = getLogicDeleteFieldInfo(entity.getClass());
        if (fieldInfo != null) {
            try {
                Object currentValue = fieldInfo.field.get(entity);
                if (currentValue == null) {
                    // 设置为未删除值
                    Object notDeletedValue = convertValue(fieldInfo.notDeletedValue, fieldInfo.field.getType());
                    fieldInfo.field.set(entity, notDeletedValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to set logic delete field: " + fieldInfo.field.getName(), e);
            }
        }
    }
    
    /**
     * 获取逻辑删除字段信息（带缓存）
     */
    public LogicDeleteInfo getLogicDeleteInfo(Class<?> entityClass) {
        return LOGIC_DELETE_INFO_CACHE.computeIfAbsent(entityClass, this::createLogicDeleteInfo);
    }

    /**
     * 构建逻辑删除的WHERE条件
     * @param entityClass 实体类
     * @return 逻辑删除WHERE条件，如果没有逻辑删除字段则返回空字符串
     */
    public String buildLogicDeleteCondition(Class<?> entityClass) {
        LogicDeleteFieldInfo fieldInfo = getLogicDeleteFieldInfo(entityClass);
        if (fieldInfo != null) {
            String columnName = getColumnName(fieldInfo.field);
            return columnName + " = " + fieldInfo.notDeletedValue;
        }
        return "";
    }

    /**
     * 获取逻辑删除字段的列名
     */
    private String getColumnName(Field field) {
        // 这里可以根据@TableField注解获取列名，如果没有则使用字段名转下划线
        com.scrm.r2dbc.plus.annotation.TableField tableField = field.getAnnotation(com.scrm.r2dbc.plus.annotation.TableField.class);
        if (tableField != null && !tableField.value().isEmpty()) {
            return tableField.value();
        }
        // 将驼峰命名转换为下划线命名
        return camelToUnderscore(field.getName());
    }

    /**
     * 驼峰命名转下划线命名
     */
    private String camelToUnderscore(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(camelCase.charAt(0)));
        for (int i = 1; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append('_').append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 创建逻辑删除信息对象
     */
    private LogicDeleteInfo createLogicDeleteInfo(Class<?> entityClass) {
        LogicDeleteFieldInfo fieldInfo = getLogicDeleteFieldInfo(entityClass);
        if (fieldInfo != null) {
            return new LogicDeleteInfo(
                fieldInfo.field.getName(),
                fieldInfo.notDeletedValue,
                fieldInfo.deletedValue
            );
        }
        return null;
    }

    /**
     * 获取逻辑删除字段信息（带缓存）
     */
    private LogicDeleteFieldInfo getLogicDeleteFieldInfo(Class<?> entityClass) {
        return LOGIC_DELETE_CACHE.computeIfAbsent(entityClass, this::findLogicDeleteField);
    }

    /**
     * 查找逻辑删除字段
     */
    private LogicDeleteFieldInfo findLogicDeleteField(Class<?> entityClass) {
        Field[] fields = getAllFields(entityClass);

        for (Field field : fields) {
            TableLogic tableLogic = field.getAnnotation(TableLogic.class);
            if (tableLogic != null) {
                field.setAccessible(true);
                return new LogicDeleteFieldInfo(
                    field,
                    tableLogic.value(),
                    tableLogic.delval()
                );
            }
        }

        return null;
    }
    
    /**
     * 将字符串值转换为字段类型对应的值
     */
    private Object convertValue(String value, Class<?> fieldType) {
        if (fieldType == String.class) {
            return value;
        } else if (fieldType == Integer.class || fieldType == int.class) {
            return Integer.valueOf(value);
        } else if (fieldType == Long.class || fieldType == long.class) {
            return Long.valueOf(value);
        } else if (fieldType == Boolean.class || fieldType == boolean.class) {
            return Boolean.valueOf(value);
        } else {
            return value;
        }
    }
    
    /**
     * 获取所有字段（包括父类）
     */
    private Field[] getAllFields(Class<?> clazz) {
        java.util.List<Field> fieldList = new java.util.ArrayList<>();
        
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers()) && 
                    !field.isSynthetic()) {
                    fieldList.add(field);
                }
            }
            clazz = clazz.getSuperclass();
        }
        
        return fieldList.toArray(new Field[0]);
    }
    
    /**
     * 逻辑删除字段信息（内部缓存使用）
     */
    private static class LogicDeleteFieldInfo {
        private final Field field;
        private final String notDeletedValue;
        private final String deletedValue;

        public LogicDeleteFieldInfo(Field field, String notDeletedValue, String deletedValue) {
            this.field = field;
            this.notDeletedValue = notDeletedValue;
            this.deletedValue = deletedValue;
        }
    }

    /**
     * 逻辑删除信息（对外暴露）
     */
    public static class LogicDeleteInfo {
        private final String fieldName;
        private final String notDeletedValue;
        private final String deletedValue;
        
        public LogicDeleteInfo(String fieldName, String notDeletedValue, String deletedValue) {
            this.fieldName = fieldName;
            this.notDeletedValue = notDeletedValue;
            this.deletedValue = deletedValue;
        }
        
        public String getFieldName() {
            return fieldName;
        }
        
        public String getNotDeletedValue() {
            return notDeletedValue;
        }
        
        public String getDeletedValue() {
            return deletedValue;
        }
    }
}
