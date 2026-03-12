package com.scrm.r2dbc.plus.test.mapper;

import com.scrm.r2dbc.plus.annotation.*;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.test.entity.TestAccount;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 注解测试 Mapper
 * 用于测试新添加的 @Select、@Insert、@Update、@Delete 注解功能
 * 
 * @author dason
 */
@Mapper
public interface AnnotationTestMapper extends BaseMapper<TestAccount> {

    /**
     * 使用 @Select 注解查询单个账号
     */
    @Select("SELECT * FROM test_account WHERE id = #{id}")
    Mono<TestAccount> selectAccountById(@Param("id") Long id);

    /**
     * 使用 @Select 注解查询账号列表
     */
    @Select("SELECT * FROM test_account WHERE uuid = #{uuid} AND status = #{status}")
    Flux<TestAccount> selectAccountsByUuidAndStatus(@Param("uuid") String uuid, @Param("status") Integer status);

    /**
     * 使用 @Select 注解统计数量
     */
    @Select("SELECT COUNT(*) FROM test_account WHERE uuid = #{uuid}")
    Mono<Long> countAccountsByUuid(@Param("uuid") String uuid);

    /**
     * 使用 @Insert 注解插入账号
     */
    @Insert("INSERT INTO test_account (uuid, account, password, status, is_open, sort_num, gmt_create, gmt_modified) " +
            "VALUES (#{uuid}, #{account}, #{password}, #{status}, #{isOpen}, #{sortNum}, NOW(), NOW())")
    Mono<Integer> insertAccount(@Param("uuid") String uuid, 
                               @Param("account") String account,
                               @Param("password") String password, 
                               @Param("status") Integer status,
                               @Param("isOpen") Integer isOpen, 
                               @Param("sortNum") Long sortNum);

    /**
     * 使用 @Update 注解更新账号状态
     */
    @Update("UPDATE test_account SET status = #{status}, gmt_modified = NOW() WHERE id = #{id}")
    Mono<Integer> updateAccountStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 使用 @Update 注解批量更新账号状态
     */
    @Update("UPDATE test_account SET status = #{status}, gmt_modified = NOW() WHERE uuid = #{uuid}")
    Mono<Integer> updateAccountStatusByUuid(@Param("uuid") String uuid, @Param("status") Integer status);

    /**
     * 使用 @Delete 注解删除账号
     */
    @Delete("DELETE FROM test_account WHERE id = #{id}")
    Mono<Integer> deleteAccountById(@Param("id") Long id);

    /**
     * 使用 @Delete 注解批量删除账号
     */
    @Delete("DELETE FROM test_account WHERE uuid = #{uuid} AND status = #{status}")
    Mono<Integer> deleteAccountsByUuidAndStatus(@Param("uuid") String uuid, @Param("status") Integer status);

    /**
     * 使用 @Select 注解进行复杂查询
     */
    @Select("SELECT a.*, COUNT(b.id) as related_count " +
            "FROM test_account a " +
            "LEFT JOIN test_account b ON a.uuid = b.uuid AND b.id != a.id " +
            "WHERE a.uuid = #{uuid} " +
            "GROUP BY a.id " +
            "ORDER BY a.sort_num")
    Flux<TestAccount> selectAccountsWithRelatedCount(@Param("uuid") String uuid);

    /**
     * 使用 @Select 注解查询账号名是否存在
     */
    @Select("SELECT COUNT(*) > 0 FROM test_account WHERE account = #{account}")
    Mono<Boolean> existsByAccount(@Param("account") String account);
}
