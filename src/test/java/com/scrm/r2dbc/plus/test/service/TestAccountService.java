package com.scrm.r2dbc.plus.test.service;

import com.scrm.r2dbc.plus.page.Page;
import com.scrm.r2dbc.plus.service.impl.ServiceImpl;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import com.scrm.r2dbc.plus.test.mapper.TestAccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author dason
 * @since 2025-07-28
 */
@Service
public class TestAccountService extends ServiceImpl<TestAccountMapper, TestAccount> {

    /**
     * 根据 UUID 查询启用的客服账号
     */
    public Flux<TestAccount> findEnabledAccountsByUuid(String uuid) {
        return list(lambdaQuery()
                .eq(TestAccount::getUuid, uuid)
                .eq(TestAccount::getIsOpen, 0)  // 0 表示启用
                .eq(TestAccount::getIsDel, 0)   // 0 表示未删除
                .orderByDesc(TestAccount::getGmtCreate).getWrapper());
    }

    /**
     * 根据账号名模糊查询
     */
    public Flux<TestAccount> findAccountsByAccountLike(String account) {
        return list(lambdaQuery()
                .like(TestAccount::getAccount, account)
                .eq(TestAccount::getIsDel, 0)
                .orderByAsc(TestAccount::getAccount).getWrapper());
    }

    /**
     * 分页查询客服账号
     */
    public Mono<Page<TestAccount>> pageAccounts(long current, long size, String uuid, String account, Integer status) {
        Page<TestAccount> page = new Page<>(current, size);

        return page(page, lambdaQuery()
                .eq(TestAccount::getUuid, uuid)
                .like(account != null, TestAccount::getAccount, account)
                .eq(status != null, TestAccount::getStatus, status)
                .eq(TestAccount::getIsDel, 0)
                .orderByDesc(TestAccount::getGmtCreate).getWrapper());
    }

    /**
     * 批量更新账号状态
     */
    public Mono<Boolean> batchUpdateStatus(java.util.List<Long> ids, Integer status) {
        return lambdaUpdate()
                .set(TestAccount::getStatus, status)
                .set(TestAccount::getGmtModified, LocalDateTime.now())
                .in(TestAccount::getId, ids).update();
    }

    /**
     * 根据分组ID查询账号数量
     */
    public Mono<Long> countByGroupId(Long groupId) {
        return count(lambdaQuery()
                .eq(TestAccount::getGroupId, groupId)
                .eq(TestAccount::getIsDel, 0).getWrapper());
    }

    /**
     * 软删除账号
     */
    public Mono<Boolean> softDeleteById(Long id) {
        return lambdaUpdate()
                .set(TestAccount::getIsDel, 1)
                .set(TestAccount::getGmtModified, LocalDateTime.now())
                .eq(TestAccount::getId, id).update();
    }

    /**
     * 批量软删除
     */
    public Mono<Boolean> batchSoftDelete(java.util.List<Long> ids) {
        return lambdaUpdate()
                .set(TestAccount::getIsDel, 1)
                .set(TestAccount::getGmtModified, LocalDateTime.now())
                .in(TestAccount::getId, ids).update();
    }

    /**
     * 根据国家查询账号
     */
    public Flux<TestAccount> findAccountsByCountry(String country) {
        return list(lambdaQuery()
                .eq(TestAccount::getCountry, country)
                .eq(TestAccount::getIsDel, 0)
                .isNotNull(TestAccount::getCountry)
                .orderByDesc(TestAccount::getGmtCreate).getWrapper());
    }

    /**
     * 查询在线状态的账号
     */
    public Flux<TestAccount> findOnlineAccounts(String uuid) {
        return lambdaQuery()
                .eq(TestAccount::getUuid, uuid)
                .eq(TestAccount::getStatus, 1)  // 假设 1 表示在线
                .eq(TestAccount::getIsOpen, 0)
                .eq(TestAccount::getIsDel, 0)
                .orderByDesc(TestAccount::getGmtModified).list();
    }

    /**
     * 更新账号最后登录时间
     */
    public Mono<Boolean> updateLastLoginTime(Long id) {
        return lambdaUpdate()
                .set(TestAccount::getGmtModified, LocalDateTime.now())
                .eq(TestAccount::getId, id).update();
    }

    /**
     * 根据昵称查询账号
     */
    public Flux<TestAccount> findAccountsByNickname(String nickname) {
        return lambdaQuery()
                .like(TestAccount::getNickname, nickname)
                .eq(TestAccount::getIsDel, 0)
                .orderByAsc(TestAccount::getNickname).list();
    }

    /**
     * 检查账号是否存在
     */
    public Mono<Boolean> existsByAccount(String account) {
        return lambdaQuery()
                .eq(TestAccount::getAccount, account)
                .eq(TestAccount::getIsDel, 0).count()
                .map(count -> count > 0);
    }

    // ========== 调用 Mapper 的自定义方法 ==========

    /**
     * 使用 XML 查询复杂条件的账号
     */
    public Flux<TestAccount> findAccountsByComplexCondition(Map<String, Object> conditions) {
        return baseMapper.findAccountsByComplexCondition(conditions);
    }

    /**
     * 获取账号统计信息
     */
    public Mono<Map<String, Object>> getAccountStatistics(String uuid) {
        return baseMapper.getAccountStatistics(uuid);
    }

    /**
     * 获取各状态账号数量
     */
    public Flux<Map<String, Object>> getAccountStatusCount(String uuid) {
        return baseMapper.countAccountsByStatus(uuid);
    }

    /**
     * 查询最近活跃的账号
     */
    public Flux<TestAccount> findRecentActiveAccounts(String uuid, Integer hours, Integer limit) {
        return baseMapper.findRecentActiveAccounts(uuid, hours, limit);
    }

    /**
     * 获取账号分组统计
     */
    public Flux<Map<String, Object>> getAccountGroupStatistics(String uuid) {
        return baseMapper.getAccountGroupStatistics(uuid);
    }

    /**
     * 根据优先级查询账号
     */
    public Flux<TestAccount> findAccountsByPriority(String uuid, String priority) {
        return baseMapper.findAccountsByPriority(uuid, priority);
    }

    /**
     * 批量更新账号状态
     */
    public Mono<Integer> batchUpdateAccountStatus(java.util.List<Long> ids, Integer status) {
        return baseMapper.batchUpdateAccountStatus(ids, status, null);
    }
    
    /**
     * 验证 baseMapper 是否被正确注入（测试用）
     */
    public boolean isBaseMapperInjected() {
        return baseMapper != null;
    }
    
    /**
     * 验证 entityClass 是否被正确设置（测试用）
     */
    public Class<TestAccount> getEntityClassForTest() {
        return (Class<TestAccount>) getEntityClass();
    }
}
