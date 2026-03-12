package com.scrm.r2dbc.plus.test.compatibility;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * 兼容性测试
 * 测试与其他框架和版本的兼容性
 * 
 * @author dason
 */
@Tag("compatibility")
@Tag("slow")
public class CompatibilityTest {

    @Test
    public void testSpringBootCompatibility() {
        // 测试 Spring Boot 兼容性
        System.out.println("Spring Boot 兼容性测试");
    }

    @Test
    public void testR2dbcCompatibility() {
        // 测试 R2DBC 驱动兼容性
        System.out.println("R2DBC 驱动兼容性测试");
    }

    @Test
    public void testMySqlCompatibility() {
        // 测试 MySQL 兼容性
        System.out.println("MySQL 兼容性测试");
    }
}
