package com.scrm.r2dbc.plus.test.mapper;

import com.scrm.r2dbc.plus.annotation.Param;
import com.scrm.r2dbc.plus.annotation.Mapper;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * 测试用的Mapper接口
 * 对应 TestMapper.xml 中的SQL语句
 *
 * @author dason
 */
@Mapper
public interface TestMapper extends BaseMapper<Map<String, Object>> {

    /**
     * 根据ID查找
     */
    Mono<Map<String, Object>> findById(@Param("id") Long id);

    /**
     * 根据条件查找
     */
    Flux<Map<String, Object>> findByCondition(Map<String, Object> conditions);

    /**
     * 根据优先级查找
     */
    Flux<Map<String, Object>> findByPriority(@Param("priority") String priority);

    /**
     * 根据ID列表查找
     */
    Flux<Map<String, Object>> findByIds(@Param("ids") List<Long> ids);

    /**
     * 使用trim标签查找
     */
    Flux<Map<String, Object>> findWithTrim(@Param("name") String name, @Param("status") Integer status);

    /**
     * 插入记录
     */
    Mono<Integer> insertData(@Param("name") String name, @Param("status") Integer status);

    /**
     * 批量插入
     */
    Mono<Integer> batchInsert(@Param("items") List<Map<String, Object>> items);

    /**
     * 根据ID更新
     */
    Mono<Integer> updateById(@Param("id") Long id, @Param("name") String name, @Param("status") Integer status);

    /**
     * 批量更新
     */
    Mono<Integer> batchUpdate(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 根据ID删除
     */
    Mono<Integer> deleteById(@Param("id") Long id);

    /**
     * 根据条件删除
     */
    Mono<Integer> deleteByCondition(@Param("name") String name, @Param("status") Integer status);

    /**
     * 复杂查询
     */
    Flux<Map<String, Object>> complexQuery(Map<String, Object> params);

    /**
     * 按状态统计
     */
    Flux<Map<String, Object>> countByStatus(@Param("name") String name);
}
