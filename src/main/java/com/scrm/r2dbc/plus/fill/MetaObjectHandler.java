package com.scrm.r2dbc.plus.fill;

import com.scrm.r2dbc.plus.annotation.TableField;
import com.scrm.r2dbc.plus.enums.FieldFill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * 元对象字段填充控制器抽象类
 *
 * @author dason
 */
public interface MetaObjectHandler {

    Logger log = LoggerFactory.getLogger(MetaObjectHandler.class);

    /**
     * 插入元对象字段填充（用于插入时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    void insertFill(MetaObject metaObject);

    /**
     * 更新元对象字段填充（用于更新时对公共字段的填充）
     *
     * @param metaObject 元对象
     */
    void updateFill(MetaObject metaObject);

    /**
     * 通用填充
     *
     * @param fieldName  字段名
     * @param fieldVal   字段值
     * @param metaObject 元对象
     * @return this
     */
    default MetaObjectHandler setFieldValByName(String fieldName, Object fieldVal, MetaObject metaObject) {
        metaObject.setValue(fieldName, fieldVal);
        return this;
    }

    /**
     * 插入时填充
     *
     * @param fieldName  字段名
     * @param fieldVal   字段值
     * @param metaObject 元对象
     * @return this
     */
    default MetaObjectHandler setInsertFieldValByName(String fieldName, Object fieldVal, MetaObject metaObject) {
        if (!metaObject.hasGetter(fieldName) || metaObject.getValue(fieldName) == null) {
            metaObject.setValue(fieldName, fieldVal);
        }
        return this;
    }

    /**
     * 更新时填充
     *
     * @param fieldName  字段名
     * @param fieldVal   字段值
     * @param metaObject 元对象
     * @return this
     */
    default MetaObjectHandler setUpdateFieldValByName(String fieldName, Object fieldVal, MetaObject metaObject) {
        metaObject.setValue(fieldName, fieldVal);
        return this;
    }

    /**
     * 严格的插入填充
     * 根据字段类型和注解进行验证，只有标记了 INSERT 或 INSERT_UPDATE 的字段才会被填充
     *
     * @param metaObject 元对象
     * @param fieldName  字段名
     * @param fieldType  字段类型
     * @param fieldVal   字段值
     * @param <T>        字段值类型
     * @return this
     */
    default <T> MetaObjectHandler strictInsertFill(MetaObject metaObject, String fieldName, Class<T> fieldType, T fieldVal) {
        return strictFillStrategy(metaObject, fieldName, fieldType, fieldVal, FieldFill.INSERT);
    }

    /**
     * 严格的更新填充
     * 根据字段类型和注解进行验证，只有标记了 UPDATE 或 INSERT_UPDATE 的字段才会被填充
     *
     * @param metaObject 元对象
     * @param fieldName  字段名
     * @param fieldType  字段类型
     * @param fieldVal   字段值
     * @param <T>        字段值类型
     * @return this
     */
    default <T> MetaObjectHandler strictUpdateFill(MetaObject metaObject, String fieldName, Class<T> fieldType, T fieldVal) {
        return strictFillStrategy(metaObject, fieldName, fieldType, fieldVal, FieldFill.UPDATE);
    }

    /**
     * 严格填充策略的核心实现
     *
     * @param metaObject 元对象
     * @param fieldName  字段名
     * @param fieldType  字段类型
     * @param fieldVal   字段值
     * @param fillType   填充类型
     * @param <T>        字段值类型
     * @return this
     */
    default <T> MetaObjectHandler strictFillStrategy(MetaObject metaObject, String fieldName, Class<T> fieldType, T fieldVal, FieldFill fillType) {
        if (metaObject == null || fieldName == null || fieldType == null) {
            return this;
        }

        // 检查字段是否存在
        if (!metaObject.hasGetter(fieldName)) {
            return this;
        }

        // 获取字段信息进行验证
        Field field = getFieldFromMetaObject(metaObject, fieldName);
        if (field == null) {
            return this;
        }

        // 验证字段类型
        if (!isFieldTypeCompatible(field.getType(), fieldType)) {
            if (log.isWarnEnabled()) {
                log.warn("严格填充类型不匹配：字段 {} 期望类型 {}，实际类型 {}",
                    fieldName, fieldType.getName(), field.getType().getName());
            }
            return this;
        }

        // 验证字段填充注解
        if (!isFieldFillable(field, fillType)) {
            return this;
        }

        // 执行填充逻辑
        return doStrictFill(metaObject, fieldName, fieldVal, fillType);
    }

    /**
     * 执行严格填充
     *
     * @param metaObject 元对象
     * @param fieldName  字段名
     * @param fieldVal   字段值
     * @param fillType   填充类型
     * @param <T>        字段值类型
     * @return this
     */
    default <T> MetaObjectHandler doStrictFill(MetaObject metaObject, String fieldName, T fieldVal, FieldFill fillType) {
        Object currentValue = metaObject.getValue(fieldName);

        // 根据填充类型决定填充策略
        boolean shouldFill = false;
        switch (fillType) {
            case INSERT:
                // 插入时只有当前值为空才填充
                shouldFill = (currentValue == null);
                break;
            case UPDATE:
            case INSERT_UPDATE:
                // 更新时总是填充（覆盖现有值）
                shouldFill = true;
                break;
            case DEFAULT:
            default:
                // 默认不填充
                shouldFill = false;
                break;
        }

        if (shouldFill) {
            metaObject.setValue(fieldName, fieldVal);
        }

        return this;
    }

    /**
     * 从元对象中获取字段反射信息
     *
     * @param metaObject 元对象
     * @param fieldName  字段名
     * @return 字段反射对象，如果不存在则返回 null
     */
    default Field getFieldFromMetaObject(MetaObject metaObject, String fieldName) {
        try {
            Class<?> objectType = metaObject.getObjectType();
            return getFieldFromClass(objectType, fieldName);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("获取字段反射信息失败：{}", e.getMessage());
            }
            return null;
        }
    }

    /**
     * 从类中递归查找字段（包括父类）
     *
     * @param clazz     类
     * @param fieldName 字段名
     * @return 字段反射对象，如果不存在则返回 null
     */
    default Field getFieldFromClass(Class<?> clazz, String fieldName) {
        if (clazz == null || clazz == Object.class) {
            return null;
        }

        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            // 在父类中查找
            return getFieldFromClass(clazz.getSuperclass(), fieldName);
        }
    }

    /**
     * 检查字段类型是否兼容
     *
     * @param actualType   实际字段类型
     * @param expectedType 期望的字段类型
     * @return 是否兼容
     */
    default boolean isFieldTypeCompatible(Class<?> actualType, Class<?> expectedType) {
        if (actualType == null || expectedType == null) {
            return false;
        }

        // 完全相同
        if (actualType.equals(expectedType)) {
            return true;
        }

        // 检查是否为包装类型和基本类型的关系
        if (isWrapperAndPrimitive(actualType, expectedType) || isWrapperAndPrimitive(expectedType, actualType)) {
            return true;
        }

        // 检查继承关系
        return expectedType.isAssignableFrom(actualType);
    }

    /**
     * 检查是否为包装类型和基本类型的关系
     *
     * @param type1 类型1
     * @param type2 类型2
     * @return 是否为包装类型和基本类型的关系
     */
    default boolean isWrapperAndPrimitive(Class<?> type1, Class<?> type2) {
        if (type1 == Integer.class && type2 == int.class) return true;
        if (type1 == Long.class && type2 == long.class) return true;
        if (type1 == Double.class && type2 == double.class) return true;
        if (type1 == Float.class && type2 == float.class) return true;
        if (type1 == Boolean.class && type2 == boolean.class) return true;
        if (type1 == Byte.class && type2 == byte.class) return true;
        if (type1 == Short.class && type2 == short.class) return true;
        if (type1 == Character.class && type2 == char.class) return true;
        return false;
    }

    /**
     * 检查字段是否可以进行指定类型的填充
     *
     * @param field    字段反射对象
     * @param fillType 填充类型
     * @return 是否可以填充
     */
    default boolean isFieldFillable(Field field, FieldFill fillType) {
        TableField tableField = field.getAnnotation(TableField.class);
        if (tableField == null) {
            // 没有 @TableField 注解，默认不填充
            return false;
        }

        FieldFill fieldFill = tableField.fill();

        // 检查填充策略是否匹配
        switch (fillType) {
            case INSERT:
                return fieldFill == FieldFill.INSERT || fieldFill == FieldFill.INSERT_UPDATE;
            case UPDATE:
                return fieldFill == FieldFill.UPDATE || fieldFill == FieldFill.INSERT_UPDATE;
            case INSERT_UPDATE:
                return fieldFill == FieldFill.INSERT_UPDATE;
            case DEFAULT:
            default:
                return fieldFill != FieldFill.DEFAULT;
        }
    }
}
