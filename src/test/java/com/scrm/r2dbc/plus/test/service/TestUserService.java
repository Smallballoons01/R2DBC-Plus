package com.scrm.r2dbc.plus.test.service;

import com.scrm.r2dbc.plus.service.impl.ServiceImpl;
import com.scrm.r2dbc.plus.test.entity.TestUser;
import com.scrm.r2dbc.plus.test.mapper.TestUserMapper;
import org.springframework.stereotype.Service;

/**
 * @author dason
 * @since 2025-07-30
 */
@Service
public class TestUserService extends ServiceImpl<TestUserMapper, TestUser> {
}
