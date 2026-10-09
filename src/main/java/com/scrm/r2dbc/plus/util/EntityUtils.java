package com.scrm.r2dbc.plus.util;

import com.scrm.r2dbc.plus.annotation.TableField;
import com.scrm.r2dbc.plus.annotation.TableId;
import com.scrm.r2dbc.plus.annotation.TableName;
import com.scrm.r2dbc.plus.enums.IdType;
import org.springframework.data.relational.core.mapping.Table;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体工具类
 * 
 * @author dason
 */
public class EntityUtils {

    private static final Map<Class<?>, String> TABLE_NAME_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, String>> FIELD_COLUMN_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, IdFieldInfo> ID_FIELD_INFO_CACHE = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取表名
     */
    public static String getTableName(Class<?> entityClass) {
        return TABLE_NAME_CACHE.computeIfAbsent(entityClass, clazz -> {
            // 优先使用 R2dbc-Plus 的 @TableName 注解
            TableName r2TableName =
                clazz.getAnnotation(TableName.class);
            if (r2TableName != null && !r2TableName.value().isEmpty()) {
                return r2TableName.value();
            }

            // 其次通过反射检查 MyBatis-Plus 的 @TableName 注解（避免直接依赖）
            try {
                Class<?> mybatisPlusTableNameClass = Class.forName("com.scrm.r2dbc.plus.annotation.TableName");
                Object mybatisPlusTableName = clazz.getAnnotation((Class<? extends java.lang.annotation.Annotation>) mybatisPlusTableNameClass);
                if (mybatisPlusTableName != null) {
                    String value = (String) mybatisPlusTableNameClass.getMethod("value").invoke(mybatisPlusTableName);
                    if (value != null && !value.isEmpty()) {
                        return value;
                    }
                }
            } catch (Exception ignored) {
                // MyBatis-Plus 不在 classpath 中，忽略
            }

            // 再次使用 Spring Data 的 @Table 注解
            Table table = clazz.getAnnotation(Table.class);
            if (table != null && !table.value().isEmpty()) {
                return table.value();
            }

            // 默认使用类名转下划线
            return LambdaUtils.camelToUnderscore(clazz.getSimpleName());
        });
    }

    /**
     * 获取ID字段信息（带缓存）
     */
    public static IdFieldInfo getIdFieldInfo(Class<?> entityClass) {
        return ID_FIELD_INFO_CACHE.computeIfAbsent(entityClass, clazz -> {
            Field[] fields = getAllFields(clazz);

            // 查找 @TableId 注解
            for (Field field : fields) {
                TableId tableId = field.getAnnotation(TableId.class);
                if (tableId != null) {
                    field.setAccessible(true);
                    String columnName = tableId.value().isEmpty() ? 
                        LambdaUtils.camelToUnderscore(field.getName()) : tableId.value();
                    return new IdFieldInfo(field, tableId.type(), columnName);
                }
            }

            // 默认查找名为 id 的字段
            for (Field field : fields) {
                if ("id".equals(field.getName())) {
                    field.setAccessible(true);
                    String columnName = LambdaUtils.camelToUnderscore(field.getName());
                    return new IdFieldInfo(field, IdType.NONE, columnName);
                }
            }

            throw new RuntimeException("No ID field found in entity: " + clazz.getName());
        });
    }
    
    /**
     * 获取主键字段名
     */
    public static String getIdFieldName(Class<?> entityClass) {
        return getIdFieldInfo(entityClass).getField().getName();
    }

    /**
     * 获取主键列名
     */
    public static String getIdColumnName(Class<?> entityClass) {
        return getIdFieldInfo(entityClass).getColumnName();
    }

    /**
     * 获取实体对象的主键值
     */
    public static Object getIdValue(Object entity) {
        IdFieldInfo idFieldInfo = getIdFieldInfo(entity.getClass());
        try {
            return idFieldInfo.getField().get(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ID value", e);
        }
    }
    
    /**
     * 设置实体对象的主键值
     */
    public static void setIdValue(Object entity, Object idValue) {
        IdFieldInfo idFieldInfo = getIdFieldInfo(entity.getClass());
        try {
            idFieldInfo.getField().set(entity, idValue);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set ID value", e);
        }
    }

    /**
     * 获取字段缓存（带缓存）
     */
    public static Map<String, Field> getFieldCache(Class<?> entityClass) {
        return FIELD_CACHE.computeIfAbsent(entityClass, clazz -> {
            Map<String, Field> fieldMap = new HashMap<>();
            Field[] fields = getAllFields(clazz);

            for (Field field : fields) {
                field.setAccessible(true);
                fieldMap.put(field.getName(), field);
            }

            return fieldMap;
        });
    }

    /**
     * 获取字段（带缓存）
     */
    public static Field getField(Class<?> entityClass, String fieldName) throws NoSuchFieldException {
        Map<String, Field> fieldMap = getFieldCache(entityClass);
        Field field = fieldMap.get(fieldName);
        if (field == null) {
            throw new NoSuchFieldException("Field '" + fieldName + "' not found in class " + entityClass.getName());
        }
        return field;
    }

    /**
     * 获取字段到列名的映射
     */
    public static Map<String, String> getFieldColumnMapping(Class<?> entityClass) {
        return FIELD_COLUMN_CACHE.computeIfAbsent(entityClass, clazz -> {
            Map<String, String> mapping = new HashMap<>();
            Map<String, Field> fieldMap = getFieldCache(clazz);

            for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
                Field field = entry.getValue();
                // 过滤掉不应该映射到数据库的字段
                if (shouldIgnoreField(field)) {
                    continue;
                }

                String fieldName = field.getName();
                String columnName = LambdaUtils.camelToUnderscore(fieldName);
                mapping.put(fieldName, columnName);
            }

            return mapping;
        });
    }

    /**
     * 判断字段是否应该被忽略
     */
    private static boolean shouldIgnoreField(Field field) {
        int modifiers = field.getModifiers();

        // 忽略静态字段
        if (java.lang.reflect.Modifier.isStatic(modifiers)) {
            return true;
        }

        // 忽略 final 字段
        if (java.lang.reflect.Modifier.isFinal(modifiers)) {
            return true;
        }

        // 忽略合成字段
        if (field.isSynthetic()) {
            return true;
        }

        // 检查 @TableField(exist = false) 注解
        TableField tableField =
            field.getAnnotation(TableField.class);
        if (tableField != null && !tableField.exist()) {
            return true;
        }

        return false;
    }

    /**
     * 获取所有字段（包括父类）
     */
    public static Field[] getAllFields(Class<?> clazz) {
        java.util.List<Field> fieldList = new java.util.ArrayList<>();

        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                fieldList.add(field);
            }
            clazz = clazz.getSuperclass();
        }

        return fieldList.toArray(new Field[0]);
    }

    /**
     * 获取实体对象的字段值
     */
    public static Object getFieldValue(Object entity, String fieldName) {
        try {
            Field field = getField(entity.getClass(), fieldName);
            return field.get(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
    }

    /**
     * 设置实体对象的字段值
     */
    public static void setFieldValue(Object entity, String fieldName, Object value) {
        try {
            Field field = getField(entity.getClass(), fieldName);
            field.set(entity, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value: " + fieldName, e);
        }
    }
    
    /**
     * 判断实体类是否有自增ID字段
     */
    public static boolean hasAutoIdField(Class<?> entityClass) {
        return getIdFieldInfo(entityClass).isAutoId();
    }
    
    /**
     * 获取自增ID字段
     */
    public static Field getAutoIdField(Class<?> entityClass) {
        IdFieldInfo idFieldInfo = getIdFieldInfo(entityClass);
        return idFieldInfo.isAutoId() ? idFieldInfo.getField() : null;
    }
    
    /**
     * 获取自增ID字段的数据库列名
     */
    public static String getAutoIdColumnName(Class<?> entityClass) {
        IdFieldInfo idFieldInfo = getIdFieldInfo(entityClass);
        return idFieldInfo.isAutoId() ? idFieldInfo.getColumnName() : null;
    }
    
    /**
     * 设置自增ID值到实体对象
     */
    public static void setAutoIdValue(Object entity, Object idValue) {
        IdFieldInfo idFieldInfo = getIdFieldInfo(entity.getClass());
        if (idFieldInfo.isAutoId() && idValue != null) {
            try {
                idFieldInfo.getField().set(entity, idValue);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set auto-generated ID", e);
            }
        }
    }
    
    /**
     * ID 字段信息类
     */
    public static class IdFieldInfo {
        private final Field field;
        private final IdType idType;
        private final String columnName;
        
        public IdFieldInfo(Field field, IdType idType, String columnName) {
            this.field = field;
            this.idType = idType;
            this.columnName = columnName;
        }
        
        public Field getField() {
            return field;
        }
        
        public IdType getIdType() {
            return idType;
        }
        
        public String getColumnName() {
            return columnName;
        }
        
        public boolean isAutoId() {
            return idType == IdType.AUTO;
        }
    }
}
