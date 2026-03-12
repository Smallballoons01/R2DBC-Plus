package com.scrm.r2dbc.plus.version;

import com.scrm.r2dbc.plus.annotation.Version;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 乐观锁版本号处理器
 * 专门处理乐观锁相关逻辑
 * 
 * @author dason
 */
@Component
public class VersionProcessor {

    /**
     * 版本字段信息缓存
     */
    private static final ConcurrentHashMap<Class<?>, VersionFieldInfo> VERSION_CACHE = new ConcurrentHashMap<>();

    /**
     * 版本信息缓存（对外暴露的对象）
     */
    private static final ConcurrentHashMap<Class<?>, VersionInfo> VERSION_INFO_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 处理插入时的版本号初始化
     */
    public void processInsert(Object entity) {
        if (entity == null) {
            return;
        }

        VersionFieldInfo fieldInfo = getVersionFieldInfo(entity.getClass());
        if (fieldInfo != null) {
            try {
                Object currentValue = fieldInfo.field.get(entity);
                if (currentValue == null) {
                    // 设置初始版本号
                    Object initialVersion = getInitialVersion(fieldInfo.field.getType());
                    fieldInfo.field.set(entity, initialVersion);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to set version field: " + fieldInfo.field.getName(), e);
            }
        }
    }
    
    /**
     * 处理更新时的版本号递增
     */
    public void processUpdate(Object entity) {
        if (entity == null) {
            return;
        }

        VersionFieldInfo fieldInfo = getVersionFieldInfo(entity.getClass());
        if (fieldInfo != null) {
            try {
                Object currentValue = fieldInfo.field.get(entity);
                if (currentValue != null) {
                    // 版本号递增
                    Object nextVersion = incrementVersion(currentValue, fieldInfo.field.getType());
                    fieldInfo.field.set(entity, nextVersion);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to increment version field: " + fieldInfo.field.getName(), e);
            }
        }
    }
    
    /**
     * 获取版本字段信息（带缓存）
     */
    public VersionInfo getVersionInfo(Class<?> entityClass) {
        return VERSION_INFO_CACHE.computeIfAbsent(entityClass, this::createVersionInfo);
    }

    /**
     * 创建版本信息对象
     */
    private VersionInfo createVersionInfo(Class<?> entityClass) {
        VersionFieldInfo fieldInfo = getVersionFieldInfo(entityClass);
        if (fieldInfo != null) {
            return new VersionInfo(fieldInfo.field.getName(), fieldInfo.field.getType());
        }
        return null;
    }
    
    /**
     * 获取实体的当前版本号
     */
    public Object getCurrentVersion(Object entity) {
        if (entity == null) {
            return null;
        }

        VersionFieldInfo fieldInfo = getVersionFieldInfo(entity.getClass());
        if (fieldInfo != null) {
            try {
                return fieldInfo.field.get(entity);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to get version field: " + fieldInfo.field.getName(), e);
            }
        }

        return null;
    }

    /**
     * 获取版本字段信息（带缓存）
     */
    private VersionFieldInfo getVersionFieldInfo(Class<?> entityClass) {
        return VERSION_CACHE.computeIfAbsent(entityClass, this::findVersionField);
    }

    /**
     * 查找版本字段
     */
    private VersionFieldInfo findVersionField(Class<?> entityClass) {
        Field[] fields = getAllFields(entityClass);

        for (Field field : fields) {
            Version version = field.getAnnotation(Version.class);
            if (version != null) {
                field.setAccessible(true);
                return new VersionFieldInfo(field);
            }
        }

        return null;
    }
    
    /**
     * 获取初始版本号
     */
    private Object getInitialVersion(Class<?> fieldType) {
        if (fieldType == Integer.class || fieldType == int.class) {
            return 1;
        } else if (fieldType == Long.class || fieldType == long.class) {
            return 1L;
        } else {
            return 1;
        }
    }
    
    /**
     * 版本号递增
     */
    private Object incrementVersion(Object currentVersion, Class<?> fieldType) {
        if (fieldType == Integer.class || fieldType == int.class) {
            return ((Integer) currentVersion) + 1;
        } else if (fieldType == Long.class || fieldType == long.class) {
            return ((Long) currentVersion) + 1L;
        } else {
            return currentVersion;
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
     * 版本字段信息（内部缓存使用）
     */
    private static class VersionFieldInfo {
        private final Field field;

        public VersionFieldInfo(Field field) {
            this.field = field;
        }
    }

    /**
     * 版本信息（对外暴露）
     */
    public static class VersionInfo {
        private final String fieldName;
        private final Class<?> fieldType;
        
        public VersionInfo(String fieldName, Class<?> fieldType) {
            this.fieldName = fieldName;
            this.fieldType = fieldType;
        }
        
        public String getFieldName() {
            return fieldName;
        }
        
        public Class<?> getFieldType() {
            return fieldType;
        }
    }
}
