package com.scrm.r2dbc.plus.test.mapper;

import com.scrm.r2dbc.plus.annotation.Param;
import com.scrm.r2dbc.plus.annotation.Mapper;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * @author dason
 * @since 2025-07-28
 *
 */
@Mapper
public interface TestAccountMapper extends BaseMapper<TestAccount> {

    // 基础的 CRUD 操作已经在 BaseMapper 中定义
    // 这里定义 XML 中的自定义方法

    /**
     * 根据复杂条件查询账号（对应 XML 中的 findAccountsByComplexCondition）
     */
    Flux<TestAccount> findAccountsByComplexCondition(Map<String, Object> conditions);

    /**
     * 获取账号统计信息（对应 XML 中的 getAccountStatistics）
     */
    Mono<Map<String, Object>> getAccountStatistics(@Param("uuid") String uuid);

    /**
     * 获取各状态账号数量（对应 XML 中的 countAccountsByStatus）
     */
    Flux<Map<String, Object>> countAccountsByStatus(@Param("uuid") String uuid);

    /**
     * 查询最近活跃的账号（对应 XML 中的 findRecentActiveAccounts）
     */
    Flux<TestAccount> findRecentActiveAccounts(@Param("uuid") String uuid, @Param("hours") Integer hours, @Param("limit") Integer limit);

    /**
     * 获取账号分组统计（对应 XML 中的 getAccountGroupStatistics）
     */
    Flux<Map<String, Object>> getAccountGroupStatistics(@Param("uuid") String uuid);

    /**
     * 根据优先级查询账号（对应 XML 中的 findAccountsByPriority）
     */
    Flux<TestAccount> findAccountsByPriority(@Param("uuid") String uuid, @Param("priority") String priority);

    /**
     * 批量更新账号状态（对应 XML 中的 batchUpdateAccountStatus）
     */
    Mono<Integer> batchUpdateAccountStatus(@Param("ids") java.util.List<Long> ids, @Param("status") Integer status, @Param("isOpen") Integer isOpen);

    /**
     * 批量插入账号
     */
    Mono<Integer> batchInsertAccounts(@Param("accounts") java.util.List<TestAccount> accounts);

    /**
     * 根据条件逻辑删除账号
     */
    Mono<Integer> logicDeleteByCondition(Map<String, Object> conditions);

    /**
     * 根据账号名查询
     */
    Mono<TestAccount> findByAccount(@Param("account") String account);

    /**
     * 根据UUID和状态查询
     */
    Flux<TestAccount> findByUuidAndStatus(@Param("uuid") String uuid, @Param("status") Integer status);

    /**
     * 统计UUID下的账号数量
     */
    Mono<Long> countByUuid(@Param("uuid") String uuid);

    /**
     * 查询排序号范围内的账号
     */
    Flux<TestAccount> findBySortNumRange(@Param("minSortNum") Long minSortNum, @Param("maxSortNum") Long maxSortNum);

    /**
     * 更新账号排序号
     */
    Mono<Integer> updateSortNum(@Param("id") Long id, @Param("sortNum") Long sortNum);

    /**
     * 查询需要清理的过期账号
     */
    Flux<TestAccount> findExpiredAccounts(@Param("days") Integer days, @Param("limit") Integer limit);

    /**
     * 复杂的联合查询示例
     */
    Flux<Map<String, Object>> findAccountsWithStatistics(@Param("uuid") String uuid);
}
