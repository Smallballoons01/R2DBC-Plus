package com.scrm.r2dbc.plus.test.mapper;

import com.scrm.r2dbc.plus.annotation.Mapper;
import com.scrm.r2dbc.plus.mapper.BaseMapper;
import com.scrm.r2dbc.plus.test.entity.TestUser;

/**
 * 测试用户Mapper接口
 * 用于测试BaseMapper的各种功能
 */
@Mapper
public interface TestUserMapper extends BaseMapper<TestUser> {


}