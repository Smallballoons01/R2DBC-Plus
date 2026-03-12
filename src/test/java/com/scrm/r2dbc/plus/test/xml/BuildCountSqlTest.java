package com.scrm.r2dbc.plus.test.xml;

import com.scrm.r2dbc.plus.xml.XmlSqlExecutor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * buildCountSql方法测试类
 * 测试改进后的正则表达式是否能正确处理包含子查询的SQL
 */
@Slf4j
public class BuildCountSqlTest {

    private XmlSqlExecutor xmlSqlExecutor;

    public BuildCountSqlTest() {
        // 创建mock对象，仅用于测试私有方法
        DatabaseClient databaseClient = org.mockito.Mockito.mock(DatabaseClient.class);
        R2dbcEntityTemplate r2dbcEntityTemplate = org.mockito.Mockito.mock(R2dbcEntityTemplate.class);
        this.xmlSqlExecutor = new XmlSqlExecutor(databaseClient, r2dbcEntityTemplate);
    }

    /**
     * 通过反射调用私有方法buildCountSql
     */
    private String invokeBuildCountSql(String originalSql) throws Exception {
        Method method = XmlSqlExecutor.class.getDeclaredMethod("buildCountSql", String.class);
        method.setAccessible(true);
        return (String) method.invoke(xmlSqlExecutor, originalSql);
    }

    @Test
    @DisplayName("测试包含EXISTS子查询的SQL")
    public void testExistsSubquery() throws Exception {
        String originalSql = """
                SELECT id, name, uuid, plugin_id AS pluginId,
                       account_uuid AS accountUuid, status, create_time AS createTime,
                       update_time AS updateTime
                FROM cj_chat_service_plugin
                WHERE uuid = :uuid
                  AND (name LIKE CONCAT('%', :keyword, '%') OR description LIKE CONCAT('%', :keyword, '%'))
                  AND status = 1
                  AND EXISTS (
                      SELECT 1
                      FROM cj_chat_service_plugin_account
                      WHERE plugin_id = cj_chat_service_plugin.id
                        AND account_uuid = :accountUuid
                      ORDER BY create_time DESC
                  )
                ORDER BY create_time DESC limit 10
                """;

        String expectedCountSql = """
                SELECT COUNT(*) FROM cj_chat_service_plugin
                WHERE uuid = :uuid
                  AND (name LIKE CONCAT('%', :keyword, '%') OR description LIKE CONCAT('%', :keyword, '%'))
                  AND status = 1
                  AND EXISTS (
                      SELECT 1
                      FROM cj_chat_service_plugin_account
                      WHERE plugin_id = cj_chat_service_plugin.id
                        AND account_uuid = :accountUuid
                      ORDER BY create_time DESC
                  )
                """;


        String actualCountSql = invokeBuildCountSql(originalSql);
        log.info("原始SQL: {}", originalSql);
        log.info("期望SQL: {}", expectedCountSql);
        log.info("实际SQL: {}", actualCountSql);
    }

    @Test
    @DisplayName("测试包含嵌套子查询的SQL")
    public void testNestedSubquery() throws Exception {
        String originalSql = """
                SELECT u.id, u.name
                FROM user u
                WHERE u.id IN (
                    SELECT user_id
                    FROM orders
                    WHERE status = 1
                      AND EXISTS (
                          SELECT 1
                          FROM order_items
                          WHERE order_id = orders.id
                          ORDER BY price DESC
                      )
                    ORDER BY create_time DESC
                )
                ORDER BY u.create_time ASC limit 10
                """;

        String expectedCountSql = """
                SELECT COUNT(*) FROM user u
                WHERE u.id IN (
                    SELECT user_id
                    FROM orders
                    WHERE status = 1
                      AND EXISTS (
                          SELECT 1
                          FROM order_items
                          WHERE order_id = orders.id
                          ORDER BY price DESC
                      )
                    ORDER BY create_time DESC
                )
                """;

        String actualCountSql = invokeBuildCountSql(originalSql);
        log.info("原始SQL: {}", originalSql);
        log.info("期望SQL: {}", expectedCountSql);
        log.info("实际SQL: {}", actualCountSql);
    }

    @Test
    @DisplayName("测试包含GROUP BY的SQL")
    public void testGroupByQuery() throws Exception {
        String originalSql = """
                SELECT department, COUNT(*) as emp_count
                FROM employee
                WHERE status = 1
                GROUP BY department
                HAVING COUNT(*) > 5
                ORDER BY emp_count DESC limit 10
                """;

        String expectedCountSql = """
                SELECT COUNT(*) FROM (
                SELECT department, COUNT(*) as emp_count
                FROM employee
                WHERE status = 1
                GROUP BY department
                HAVING COUNT(*) > 5
                ) tmp_count
                """;

        String actualCountSql = invokeBuildCountSql(originalSql);
        log.info("原始SQL: {}", originalSql);
        log.info("期望SQL: {}", expectedCountSql);
        log.info("实际SQL: {}", actualCountSql);
    }

    @Test
    @DisplayName("测试简单查询")
    public void testSimpleQuery() throws Exception {
        String originalSql = """
                SELECT id, name, age
                FROM user
                WHERE status = 1
                ORDER BY create_time DESC limit 10
                """;

        String expectedCountSql = """
                SELECT COUNT(*) FROM user
                WHERE status = 1
                """;

        String actualCountSql = invokeBuildCountSql(originalSql);
        log.info("原始SQL: {}", originalSql);
        log.info("期望SQL: {}", expectedCountSql);
        log.info("实际SQL: {}", actualCountSql);
    }

    @Test
    @DisplayName("测试包含子查询中SELECT...FROM的SQL")
    public void testSubqueryWithSelectFrom() throws Exception {
        String originalSql = """
                SELECT p.id, p.name
                FROM product p
                WHERE p.price > (
                    SELECT AVG(price)
                    FROM product
                    WHERE category = p.category
                    ORDER BY price DESC
                )
                ORDER BY p.create_time ASC limi 10
                """;

        String expectedCountSql = """
                SELECT COUNT(*) FROM product p
                WHERE p.price > (
                    SELECT AVG(price)
                    FROM product
                    WHERE category = p.category
                    ORDER BY price DESC
                )
                """;

        String actualCountSql = invokeBuildCountSql(originalSql);
        log.info("原始SQL: {}", originalSql);
        log.info("期望SQL: {}", expectedCountSql);
        log.info("实际SQL: {}", actualCountSql);
    }
}