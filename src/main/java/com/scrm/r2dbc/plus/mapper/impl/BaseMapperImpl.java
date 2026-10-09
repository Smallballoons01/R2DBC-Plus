package com.scrm.r2dbc.plus.mapper.impl;

import com.scrm.r2dbc.plus.conditions.*;
import com.scrm.r2dbc.plus.conditions.query.LambdaQueryWrapper;
import com.scrm.r2dbc.plus.conditions.query.QueryWrapper;
import com.scrm.r2dbc.plus.conditions.update.LambdaUpdateWrapper;
import com.scrm.r2dbc.plus.conditions.update.UpdateWrapper;
import com.scrm.r2dbc.plus.fill.FieldFillProcessor;
import com.scrm.r2dbc.plus.logic.LogicDeleteProcessor;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.page.Page;
import com.scrm.r2dbc.plus.util.EntityUtils;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;

/**
 * BaseMapper 默认实现
 *
 * @param <T> 实体类型
 * @author dason
 */
public class BaseMapperImpl<T> implements BaseMapper<T> {

    /**
     * 以下依赖由创建者装配：本类通常经MapperProxyFactory 以 {@code new} 创建，
     * 不作为 Spring Bean 管理，故不依赖 {@code @Autowired}。
     */
    protected R2dbcEntityTemplate r2dbcEntityTemplate;

    protected DatabaseClient databaseClient;

    protected FieldFillProcessor fieldFillProcessor;

    protected LogicDeleteProcessor logicDeleteProcessor;


    private final Class<T> entityClass;

    public BaseMapperImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * 装配处理器依赖。
     *
     * <p>本类通常由 {@link com.scrm.r2dbc.plus.proxy.MapperProxyFactory} 以
     * {@code new} 的方式创建（非 Spring Bean），无法走容器注入，
     * 因此提供显式装配入口，取代原先的反射字段写入。
     *
     * @param fieldFillProcessor    字段填充处理器，可为 null
     * @param logicDeleteProcessor  逻辑删除处理器，可为 null
     * @return 自身，便于链式调用
     */
    public BaseMapperImpl<T> withProcessors(FieldFillProcessor fieldFillProcessor,
                                           LogicDeleteProcessor logicDeleteProcessor) {
        if (fieldFillProcessor != null) {
            this.fieldFillProcessor = fieldFillProcessor;
        }
        if (logicDeleteProcessor != null) {
            this.logicDeleteProcessor = logicDeleteProcessor;
        }
        return this;
    }
    /**
     * 绑定参数的辅助方法
     */
    private DatabaseClient.GenericExecuteSpec bindParameters(DatabaseClient.GenericExecuteSpec executeSpec, Map<String, Object> parameters) {
        if (parameters != null && !parameters.isEmpty()) {
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                Object value = entry.getValue();
                if (value == null) {
                    executeSpec = executeSpec.bindNull(entry.getKey(), Object.class);
                } else {
                    executeSpec = executeSpec.bind(entry.getKey(), value);
                }
            }
        }
        return executeSpec;
    }

    /**
     * 构建包含逻辑删除条件的WHERE子句
     */
    private String buildWhereClauseWithLogicDelete(Class<T> entityClass, String additionalCondition) {
        String logicDeleteCondition = "";
        if (logicDeleteProcessor != null) {
            logicDeleteCondition = logicDeleteProcessor.buildLogicDeleteCondition(entityClass);
        }

        if (!logicDeleteCondition.isEmpty() && !additionalCondition.isEmpty()) {
            return additionalCondition + " AND " + logicDeleteCondition;
        } else if (!logicDeleteCondition.isEmpty()) {
            return logicDeleteCondition;
        } else {
            return additionalCondition;
        }
    }



    public BaseMapperImpl(Class<T> entityClass, R2dbcEntityTemplate r2dbcEntityTemplate, DatabaseClient databaseClient) {
        this.entityClass = entityClass;
        this.r2dbcEntityTemplate = r2dbcEntityTemplate;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Integer> insert(T entity) {
        // 处理字段填充和ID生成
        if (fieldFillProcessor != null) {
            fieldFillProcessor.processInsert(entity);
        }

        String tableName = EntityUtils.getTableName(entityClass);
        Map<String, String> fieldColumnMapping = EntityUtils.getFieldColumnMapping(entityClass);

        StringJoiner columns = new StringJoiner(", ");
        StringJoiner placeholders = new StringJoiner(", ");
        Map<String, Object> params = new HashMap<>();

        // 检查是否有自增ID字段
        boolean hasAutoId = EntityUtils.hasAutoIdField(entityClass);

        // 只处理映射中存在的字段（已经过滤了不需要的字段）
        for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            String fieldName = entry.getKey();
            String columnName = entry.getValue();

            try {
                Field field = EntityUtils.getField(entityClass, fieldName);
                Object value = field.get(entity);

                if (value != null) {
                    columns.add(columnName);
                    placeholders.add(":" + fieldName);
                    params.put(fieldName, value);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to access field: " + fieldName, e);
            }
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                tableName, columns.toString(), placeholders.toString());

        DatabaseClient.GenericExecuteSpec executeSpec = bindParameters(databaseClient.sql(sql), params);

        if (hasAutoId) {
            // 如果有自增ID，需要返回生成的键值并回填到实体
            return executeSpec
                    .filter(statement -> statement.returnGeneratedValues())
                    .fetch()
                    .first()
                    .map(result -> {
                        // 回填自增ID到实体
                        String idColumnName = EntityUtils.getAutoIdColumnName(entityClass);
                        Object generatedId = null;
                        
                        // 尝试多种可能的键名
                        if (idColumnName != null && result.containsKey(idColumnName)) {
                            generatedId = result.get(idColumnName);
                        } else if (result.containsKey("id")) {
                            generatedId = result.get("id");
                        } else if (result.containsKey("ID")) {
                            generatedId = result.get("ID");
                        } else if (!result.isEmpty()) {
                            // 如果没有找到明确的ID字段，取第一个值（通常是自增ID）
                            generatedId = result.values().iterator().next();
                        }
                        
                        // 使用 EntityUtils 设置自增ID值
                        EntityUtils.setAutoIdValue(entity, generatedId);
                        
                        return 1; // 插入成功返回1
                    });
        } else {
            // 没有自增ID，正常返回受影响行数
            return executeSpec
                    .fetch()
                    .rowsUpdated().map(Long::intValue);
        }
    }

    @Override
    public Mono<Integer> insertBatch(Collection<T> entityList, int batchSize) {
        if (entityList == null || entityList.isEmpty()) {
            throw new IllegalArgumentException("insertBatch Entity list cannot be empty");
        }
        if (batchSize <= 0) {
            batchSize = 1000;
        }
        return Flux.fromIterable(entityList)
                .buffer(batchSize)
                .concatMap(this::insertBatch)
                .reduce(0, Integer::sum);
    }

    @Override
    public Mono<Integer> insertBatch(Collection<T> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            throw new IllegalArgumentException("insertBatch Entity list cannot be empty");
        }

        // 处理字段填充和ID生成
        if (fieldFillProcessor != null) {
            entityList.forEach(fieldFillProcessor::processInsert);
        }

        boolean hasAutoId = EntityUtils.hasAutoIdField(entityClass);


        String tableName = EntityUtils.getTableName(entityClass);
        Map<String, String> fieldColumnMapping = EntityUtils.getFieldColumnMapping(entityClass);

        // 获取所有字段名和列名
        List<String> fieldNames = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            fieldNames.add(entry.getKey());
            columnNames.add(entry.getValue());
        }


        // 对于有自增ID的情况，使用专门的批量插入方法
        if (hasAutoId) {
            return insertBatchWithAutoId(entityList, tableName, fieldColumnMapping);
        }

        // 对于没有自增ID的情况，构建批量插入SQL
        StringJoiner columnsJoiner = new StringJoiner(", ");
        columnNames.forEach(columnsJoiner::add);

        StringJoiner valuesJoiner = new StringJoiner(", ");
        Map<String, Object> allParams = new HashMap<>();
        int entityIndex = 0;

        for (T entity : entityList) {
            StringJoiner singleValueJoiner = new StringJoiner(", ");

            for (String fieldName : fieldNames) {
                try {
                    Field field = EntityUtils.getField(entityClass, fieldName);
                    field.setAccessible(true);
                    Object value = field.get(entity);

                    String paramName = fieldName + "_" + entityIndex;
                    singleValueJoiner.add(":" + paramName);
                    allParams.put(paramName, value);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to access field: " + fieldName, e);
                }
            }

            valuesJoiner.add("(" + singleValueJoiner.toString() + ")");
            entityIndex++;
        }

        String sql = String.format("INSERT INTO %s (%s) VALUES %s",
                tableName, columnsJoiner.toString(), valuesJoiner.toString());

        return bindParameters(databaseClient.sql(sql), allParams)
                .fetch()
                .rowsUpdated().map(Long::intValue);
    }

    /**
     * 批量插入（处理自增ID回填）
     */
    private Mono<Integer> insertBatchWithAutoId(Collection<T> entityList, String tableName, Map<String, String> fieldColumnMapping) {
        String idColumnName = EntityUtils.getAutoIdColumnName(entityClass);
        String idFieldName = EntityUtils.getIdFieldName(entityClass);
        List<T> entityListArray = entityList instanceof List ? (List<T>) entityList : new ArrayList<>(entityList);

        return databaseClient.inConnection(connection -> {
            // 构建单条插入SQL模板，过滤掉自增ID字段
            StringJoiner singleColumnsJoiner = new StringJoiner(", ");
            StringJoiner placeholdersJoiner = new StringJoiner(", ");
            List<String> nonIdFieldNames = new ArrayList<>();

            for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
                String fieldName = entry.getKey();
                String columnName = entry.getValue();

                // 跳过自增ID字段
                if (fieldName.equals(idFieldName)) {
                    continue;
                }

                singleColumnsJoiner.add(columnName);
                placeholdersJoiner.add("?");
                nonIdFieldNames.add(fieldName);
            }

            String singleInsertSql = String.format("INSERT INTO %s (%s) VALUES (%s)",
                    tableName, singleColumnsJoiner.toString(), placeholdersJoiner.toString());

            // 创建Statement并绑定所有实体的参数
            var statement = connection.createStatement(singleInsertSql);
            if (idColumnName != null) {
                statement = statement.returnGeneratedValues(idColumnName);
            } else {
                statement = statement.returnGeneratedValues();
            }

            // 为每个实体绑定参数
            for (int entityIndex = 0; entityIndex < entityListArray.size(); entityIndex++) {
                T entity = entityListArray.get(entityIndex);
                int paramIndex = 0;

                for (String fieldName : nonIdFieldNames) {
                    try {
                        Field field = EntityUtils.getField(entityClass, fieldName);
                        field.setAccessible(true);
                        Object value = field.get(entity);
                        if (value == null) {
                            statement = statement.bindNull(paramIndex++, field.getType());
                        } else {
                            statement = statement.bind(paramIndex++, value);
                        }

                    } catch (Exception e) {
                        throw new RuntimeException("Failed to access field: " + fieldName, e);
                    }
                }

                // 除了最后一个实体，都需要调用add()
                if (entityIndex < entityListArray.size() - 1) {
                    statement = statement.add();
                }
            }

            // 执行并获取结果
            return Flux.from(statement.execute())
                    .flatMap(result -> result.map((row, metadata) -> {
                        // 获取生成的ID
                        Object generatedId = null;
                        if (idColumnName != null && row.get(idColumnName) != null) {
                            generatedId = row.get(idColumnName);
                        } else {
                            // 尝试获取第一列的值
                            generatedId = row.get(0);
                        }
                        return generatedId;
                    }))
                    .collectList()
                    .map(generatedIds -> {
                        // 将生成的ID按顺序回填到实体列表中
                        for (int i = 0; i < generatedIds.size() && i < entityListArray.size(); i++) {
                            Object generatedId = generatedIds.get(i);
                            if (generatedId != null) {
                                EntityUtils.setAutoIdValue(entityListArray.get(i), generatedId);
                            }
                        }
                        return entityListArray.size(); // 返回插入的记录数
                    });
        });
    }

    @Override
    public Mono<Integer> updateBatchById(Collection<T> entityList) {
        if (entityList == null || entityList.isEmpty()) {
            throw new IllegalArgumentException("updateBatchById entityList cannot be empty");
        }

        // 处理字段填充
        if (fieldFillProcessor != null) {
            entityList.forEach(fieldFillProcessor::processUpdate);
        }

        String tableName = EntityUtils.getTableName(entityClass);
        Map<String, String> fieldColumnMapping = EntityUtils.getFieldColumnMapping(entityClass);
        String idFieldName = EntityUtils.getIdFieldName(entityClass);
        String idColumnName = EntityUtils.getIdColumnName(entityClass);

        // 构建批量更新的 CASE WHEN 语句
        Map<String, StringJoiner> caseWhenBuilders = new HashMap<>();
        Map<String, Object> allParams = new HashMap<>();
        Set<Object> idSet = new HashSet<>();

        int entityIndex = 0;
        for (T entity : entityList) {
            try {
                // 获取ID值
                Field idField = EntityUtils.getField(entityClass, idFieldName);
                idField.setAccessible(true);
                Object idValue = idField.get(entity);
                if (idValue == null) {
                    continue; // 跳过没有ID的实体
                }

                String idParamName = "id_" + entityIndex;
                allParams.put(idParamName, idValue);
                idSet.add(idValue);

                // 为每个字段构建 CASE WHEN 语句
                for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
                    String fieldName = entry.getKey();
                    String columnName = entry.getValue();

                    if (fieldName.equals(idFieldName)) {
                        continue; // 跳过ID字段
                    }

                    Field field = EntityUtils.getField(entityClass, fieldName);
                    Object value = field.get(entity);

                    if (value != null) {
                        String valueParamName = fieldName + "_" + entityIndex;
                        allParams.put(valueParamName, value);

                        caseWhenBuilders.computeIfAbsent(columnName, k -> new StringJoiner(" "))
                                .add("WHEN " + idColumnName + " = :" + idParamName + " THEN :" + valueParamName);
                    }
                }

                entityIndex++;
            } catch (Exception e) {
                throw new RuntimeException("Failed to process entity for batch update", e);
            }
        }

        if (idSet.isEmpty()) {
            throw new IllegalArgumentException("updateBatchById idSet is empty");
        }

        // 构建UPDATE语句
        StringJoiner setClause = new StringJoiner(", ");
        for (Map.Entry<String, StringJoiner> entry : caseWhenBuilders.entrySet()) {
            String columnName = entry.getKey();
            String caseWhen = entry.getValue().toString();
            setClause.add(columnName + " = CASE " + caseWhen + " ELSE " + columnName + " END");
        }

        // 构建IN子句的参数
        StringJoiner inClause = new StringJoiner(", ");
        int idIndex = 0;
        for (Object id : idSet) {
            String inParamName = "in_id_" + idIndex++;
            inClause.add(":" + inParamName);
            allParams.put(inParamName, id);
        }

        String sql = String.format("UPDATE %s SET %s WHERE %s IN (%s)",
                tableName, setClause.toString(), idColumnName, inClause.toString());

        return bindParameters(databaseClient.sql(sql), allParams)
                .fetch()
                .rowsUpdated().map(Long::intValue);
    }

    @Override
    public Mono<Integer> deleteById(Serializable id) {
        String tableName = EntityUtils.getTableName(entityClass);
        String idColumnName = EntityUtils.getIdColumnName(entityClass);
        
        String sql = String.format("DELETE FROM %s WHERE %s = :id", tableName, idColumnName);
        
        return databaseClient.sql(sql)
                .bind("id", id)
                .fetch()
                .rowsUpdated().map(Long::intValue);
    }

    @Override
    public Mono<Integer> deleteBatchIds(Collection<? extends Serializable> idList) {
        if (idList == null || idList.isEmpty()) {
            throw new IllegalArgumentException("deleteBatchIds idList cannot be empty");
        }
        
        String tableName = EntityUtils.getTableName(entityClass);
        String idColumnName = EntityUtils.getIdColumnName(entityClass);
        
        StringJoiner placeholders = new StringJoiner(", ");
        Map<String, Object> params = new HashMap<>();
        int index = 0;
        for (Serializable id : idList) {
            String paramName = "id" + index++;
            placeholders.add(":" + paramName);
            params.put(paramName, id);
        }
        
        String sql = String.format("DELETE FROM %s WHERE %s IN (%s)", 
                tableName, idColumnName, placeholders.toString());
        
        return bindParameters(databaseClient.sql(sql), params)
                .fetch()
                .rowsUpdated().map(Long::intValue);
    }



    @Override
    public Mono<Integer> updateById(T entity) {
        // 处理字段填充
        if (fieldFillProcessor != null) {
            fieldFillProcessor.processUpdate(entity);
        }
        
        String tableName = EntityUtils.getTableName(entityClass);
        String idColumnName = EntityUtils.getIdColumnName(entityClass);
        Object idValue = EntityUtils.getIdValue(entity);
        
        Map<String, String> fieldColumnMapping = EntityUtils.getFieldColumnMapping(entityClass);
        StringJoiner setClause = new StringJoiner(", ");
        Map<String, Object> params = new HashMap<>();

        String idFieldName = EntityUtils.getIdFieldName(entityClass);

        // 只处理映射中存在的字段（已经过滤了不需要的字段）
        for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
            String fieldName = entry.getKey();
            String columnName = entry.getValue();

            // 跳过主键字段
            if (fieldName.equals(idFieldName)) {
                continue;
            }

            try {
                Field field = EntityUtils.getField(entityClass, fieldName);
                Object value = field.get(entity);

                if (value != null) {
                    setClause.add(columnName + " = :" + fieldName);
                    params.put(fieldName, value);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to access field: " + fieldName, e);
            }
        }
        
        if (setClause.length() == 0) {
            throw new IllegalArgumentException("updateById setClause is empty");
        }
        
        String sql = String.format("UPDATE %s SET %s WHERE %s = :id", 
                tableName, setClause.toString(), idColumnName);
        params.put("id", idValue);
        
        return bindParameters(databaseClient.sql(sql), params)
                .fetch()
                .rowsUpdated().map(Long::intValue);
    }



    @Override
    public Mono<T> selectById(Serializable id) {
        if (id == null) {
            return Mono.error(new IllegalArgumentException("ID parameter cannot be null"));
        }

        String tableName = EntityUtils.getTableName(entityClass);
        String idColumnName = EntityUtils.getIdColumnName(entityClass);

        // 构建包含逻辑删除条件的WHERE子句
        String idCondition = idColumnName + " = :id";
        String whereClause = buildWhereClauseWithLogicDelete(entityClass, idCondition);

        String sql = String.format("SELECT * FROM %s WHERE %s", tableName, whereClause);

        return databaseClient.sql(sql)
                .bind("id", id)
                .map((row, metadata) -> r2dbcEntityTemplate.getConverter().read(entityClass, row, metadata))
                .one();
    }

    @Override
    public Flux<T> selectBatchIds(Collection<? extends Serializable> idList) {
        if (idList == null || idList.isEmpty()) {
            return Flux.empty();
        }

        String tableName = EntityUtils.getTableName(entityClass);
        String idColumnName = EntityUtils.getIdColumnName(entityClass);

        StringJoiner placeholders = new StringJoiner(", ");
        Map<String, Object> params = new HashMap<>();
        int index = 0;
        for (Serializable id : idList) {
            String paramName = "id" + index++;
            placeholders.add(":" + paramName);
            params.put(paramName, id);
        }

        // 构建包含逻辑删除条件的WHERE子句
        String idCondition = idColumnName + " IN (" + placeholders.toString() + ")";
        String whereClause = buildWhereClauseWithLogicDelete(entityClass, idCondition);

        String sql = String.format("SELECT * FROM %s WHERE %s", tableName, whereClause);

        return bindParameters(databaseClient.sql(sql), params)
                .map((row, metadata) -> r2dbcEntityTemplate.getConverter().read(entityClass, row, metadata))
                .all();
    }







    @Override
    public Flux<T> selectList() {
        String tableName = EntityUtils.getTableName(entityClass);

        // 构建包含逻辑删除条件的WHERE子句
        String whereClause = buildWhereClauseWithLogicDelete(entityClass, "");

        String sql = String.format("SELECT * FROM %s", tableName);
        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        return databaseClient.sql(sql)
                .map((row, metadata) -> r2dbcEntityTemplate.getConverter().read(entityClass, row, metadata))
                .all();
    }



    @Override
    public Mono<Page<T>> selectPage(Page<T> page, Wrapper<T> queryWrapper) {
        // 分页参数规范化 + 上限保护，避免 size<=0 或超大 size 拼出非法 SQL。
        // 此处与 ServiceImpl.page 保持一致，直接调用 Mapper 的用户同样受保护。
        if (page == null) {
            return Mono.error(new IllegalArgumentException("分页参数 Page 不能为 null"));
        }
        page.normalize();

        // 如果 queryWrapper 为 null，创建一个默认的 QueryWrapper 用于分页
        final Wrapper<T> dataWrapper;
        if (queryWrapper == null) {
            dataWrapper = new QueryWrapper<T>(entityClass)
                    .page(page.getCurrent(), page.getSize());
        } else {
            dataWrapper = queryWrapper;
        }
        
        // 创建用于计数的查询包装器（不包含分页和排序）
        Wrapper<T> countWrapper = queryWrapper;
        if (queryWrapper instanceof LambdaQueryWrapper) {
            countWrapper = ((LambdaQueryWrapper<T>) queryWrapper).forCount();
        } else if (queryWrapper instanceof QueryWrapper) {
            countWrapper = ((QueryWrapper<T>) queryWrapper).forCount();
        }

        // 先查询总数
        return selectCount(countWrapper)
                .flatMap(total -> {
                    page.setTotal(total);
                    if (total == 0) {
                        page.setRecords(java.util.Collections.emptyList());
                        return Mono.just(page);
                    }

                    // 设置分页参数（如果原始 queryWrapper 不为 null）
                    if (queryWrapper != null) {
                        if (dataWrapper instanceof LambdaQueryWrapper) {
                            ((LambdaQueryWrapper<T>) dataWrapper).page(page.getCurrent(), page.getSize());
                        } else if (dataWrapper instanceof QueryWrapper) {
                            ((QueryWrapper<T>) dataWrapper).page(page.getCurrent(), page.getSize());
                        }
                    }
                    // 如果 queryWrapper 为 null，dataWrapper 已经在创建时设置了分页参数

                    // 查询数据
                    return selectList(dataWrapper)
                            .collectList()
                            .map(records -> {
                                page.setRecords(records);
                                return page;
                            });
                });
    }



    @Override
    public Mono<Integer> delete(Wrapper<T> queryWrapper) {
        String tableName = EntityUtils.getTableName(entityClass);
        String whereClause = (queryWrapper != null) ? queryWrapper.getSqlSegment() : "";

        String sql = String.format("DELETE FROM %s", tableName);
        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        return bindParameters(databaseClient.sql(sql), (queryWrapper != null) ? queryWrapper.getParamNameValuePairs() : null)
                .fetch()
                .rowsUpdated().map(Long::intValue);
    }

    @Override
    public Mono<Integer> update(T entity, Wrapper<T> updateWrapper) {
        String tableName = EntityUtils.getTableName(entityClass);

        // 获取 SET、WHERE、ORDER BY 和 last 子句
        String setSegment = "";
        String whereSegment = "";
        String orderBySegment = "";
        String lastSegment = "";

        // 根据 Wrapper 类型获取各个子句
        if (updateWrapper instanceof LambdaUpdateWrapper) {
            LambdaUpdateWrapper<T> lambdaWrapper = (LambdaUpdateWrapper<T>) updateWrapper;
            setSegment = lambdaWrapper.getSetSegment();
            whereSegment = lambdaWrapper.getWhereSegment();
            orderBySegment = lambdaWrapper.getOrderBySegment();
            lastSegment = lambdaWrapper.getLastSql();
        } else if (updateWrapper instanceof UpdateWrapper) {
            UpdateWrapper<T> updateWrapperImpl = (UpdateWrapper<T>) updateWrapper;
            setSegment = updateWrapperImpl.getSetSegment();
            whereSegment = updateWrapperImpl.getWhereSegment();
            orderBySegment = updateWrapperImpl.getOrderBySegment();
            lastSegment = updateWrapperImpl.getLastSql();
        } else {
            // 对于其他类型的Wrapper，使用getSqlSegment作为WHERE子句
            whereSegment = updateWrapper.getSqlSegment();
        }

        Map<String, Object> allParams = new HashMap<>();

        // 如果没有设置 SET 子句，从实体对象中构建
        if (setSegment.isEmpty() && entity != null) {
            // 处理字段填充
            if (fieldFillProcessor != null) {
                fieldFillProcessor.processUpdate(entity);
            }

            // 检查是否需要更新null值
            boolean shouldUpdateNullValue = false;
            if (updateWrapper instanceof LambdaUpdateWrapper) {
                shouldUpdateNullValue = ((LambdaUpdateWrapper<T>) updateWrapper).isUpdateNullValue();
            } else if (updateWrapper instanceof UpdateWrapper) {
                shouldUpdateNullValue = ((UpdateWrapper<T>) updateWrapper).isUpdateNullValue();
            }

            Map<String, String> fieldColumnMapping = EntityUtils.getFieldColumnMapping(entityClass);
            StringJoiner setClause = new StringJoiner(", ");
            String idFieldName = EntityUtils.getIdFieldName(entityClass);


            for (Map.Entry<String, String> entry : fieldColumnMapping.entrySet()) {
                String fieldName = entry.getKey();
                String columnName = entry.getValue();

                // 跳过主键字段
                if (fieldName.equals(idFieldName)) {
                    continue;
                }

                try {
                    Field field = EntityUtils.getField(entityClass, fieldName);
                    Object value = field.get(entity);
                    if (value != null || shouldUpdateNullValue) {
                        setClause.add(columnName + " = :" + fieldName);
                        allParams.put(fieldName, value);
                    }
                } catch (ReflectiveOperationException ignored) {
                }
            }
            setSegment = setClause.toString();
        }

        if (setSegment.isEmpty()) {
            throw new IllegalArgumentException("update setSegment is empty");
        }

        String sql = String.format("UPDATE %s SET %s", tableName, setSegment);
        if (!whereSegment.isEmpty()) {
            sql += " WHERE " + whereSegment;
        }
        sql += orderBySegment;
        sql += lastSegment;

        // 将Wrapper的参数put到allParams
        allParams.putAll(updateWrapper.getParamNameValuePairs());
        return bindParameters(databaseClient.sql(sql), allParams)
                .fetch()
                .rowsUpdated().map(Long::intValue);
    }



    @Override
    public Mono<T> selectOne(Wrapper<T> queryWrapper) {
        return selectList(queryWrapper).next();
    }



    @Override
    public Mono<Long> selectCount(Wrapper<T> queryWrapper) {
        String tableName = EntityUtils.getTableName(entityClass);
        String whereClause = (queryWrapper != null) ? queryWrapper.getSqlSegment() : "";

        // 构建包含逻辑删除条件的WHERE子句
        whereClause = buildWhereClauseWithLogicDelete(entityClass, whereClause);

        String sql = String.format("SELECT COUNT(*) FROM %s", tableName);
        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        return bindParameters(databaseClient.sql(sql), (queryWrapper != null) ? queryWrapper.getParamNameValuePairs() : null)
                .map(row -> row.get(0, Long.class))
                .one();
    }



    @Override
    public Flux<T> selectList(Wrapper<T> queryWrapper) {
        String tableName = EntityUtils.getTableName(entityClass);
        String whereClause = (queryWrapper != null) ? queryWrapper.getSqlSegment() : "";

        // 构建包含逻辑删除条件的WHERE子句
        whereClause = buildWhereClauseWithLogicDelete(entityClass, whereClause);

        // 获取查询列、排序信息和last语句
        String selectColumns = "*";
        String orderBy = "";
        String limitClause = "";
        String lastSql = "";

        if (queryWrapper instanceof LambdaQueryWrapper) {
            LambdaQueryWrapper<T> lambdaWrapper = (LambdaQueryWrapper<T>) queryWrapper;
            selectColumns = lambdaWrapper.getSelectColumns();
            orderBy = lambdaWrapper.getOrderBy();
            limitClause = lambdaWrapper.getLimitClause();
            lastSql = lambdaWrapper.getLastSql();
        } else if (queryWrapper instanceof QueryWrapper) {
            QueryWrapper<T> queryWrapperImpl = (QueryWrapper<T>) queryWrapper;
            selectColumns = queryWrapperImpl.getSelectColumns();
            orderBy = queryWrapperImpl.getOrderBy();
            limitClause = queryWrapperImpl.getLimitClause();
            lastSql = queryWrapperImpl.getLastSql();
        }

        String sql = String.format("SELECT %s FROM %s", selectColumns, tableName);
        if (!whereClause.isEmpty()) {
            sql += " WHERE " + whereClause;
        }
        sql += orderBy;
        sql += limitClause;
        sql += lastSql;  // last语句放在最后

        return bindParameters(databaseClient.sql(sql), (queryWrapper != null) ? queryWrapper.getParamNameValuePairs() : null)
                .map((row, metadata) -> r2dbcEntityTemplate.getConverter().read(entityClass, row, metadata))
                .all();
    }

    @Override
    public Mono<Boolean> exists(Wrapper<T> queryWrapper) {
        return selectCount(queryWrapper).map(count -> count > 0);
    }


}
