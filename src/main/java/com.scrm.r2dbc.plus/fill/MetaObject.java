package com.scrm.r2dbc.plus.fill;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 元对象，用于字段填充
 * 
 * @author dason
 */
public class MetaObject {
    
    private final Object originalObject;
    private final Map<String, Field> fieldMap;
    
    public MetaObject(Object object) {
        this.originalObject = object;
        this.fieldMap = new HashMap<>();
        initFieldMap(object.getClass());
    }
    
    private void initFieldMap(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return;
        }
        
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            fieldMap.put(field.getName(), field);
        }
        
        // 递归处理父类
        initFieldMap(clazz.getSuperclass());
    }
    
    /**
     * 获取字段值
     */
    public Object getValue(String fieldName) {
        Field field = fieldMap.get(fieldName);
        if (field == null) {
            return null;
        }
        
        try {
            return field.get(originalObject);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to get field value: " + fieldName, e);
        }
    }
    
    /**
     * 设置字段值
     */
    public void setValue(String fieldName, Object value) {
        Field field = fieldMap.get(fieldName);
        if (field == null) {
            return;
        }
        
        try {
            field.set(originalObject, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set field value: " + fieldName, e);
        }
    }
    
    /**
     * 检查是否有指定字段的 getter
     */
    public boolean hasGetter(String fieldName) {
        return fieldMap.containsKey(fieldName);
    }
    
    /**
     * 检查是否有指定字段的 setter
     */
    public boolean hasSetter(String fieldName) {
        return fieldMap.containsKey(fieldName);
    }
    
    /**
     * 获取原始对象
     */
    public Object getOriginalObject() {
        return originalObject;
    }
    
    /**
     * 获取对象类型
     */
    public Class<?> getObjectType() {
        return originalObject.getClass();
    }
}
