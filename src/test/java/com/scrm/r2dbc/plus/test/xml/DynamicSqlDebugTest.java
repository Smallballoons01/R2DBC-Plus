package com.scrm.r2dbc.plus.test.xml;

import com.scrm.r2dbc.plus.xml.DynamicSqlProcessor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态SQL调试测试
 * 
 * @author dason
 */
@Tag("core")
@Tag("fast")
@Tag("xml")
public class DynamicSqlDebugTest {

    @Test
    public void testComplexConditionProcessing() throws Exception {
        // 模拟XML内容
        String xmlContent = """
            <select>
                SELECT * FROM cj_chat_service_account
                <where>
                    is_del = 0
                    <if test="uuid != null and uuid != ''">
                        AND uuid = #{uuid}
                    </if>
                    <if test="account != null and account != ''">
                        AND account LIKE CONCAT('%', #{account}, '%')
                    </if>
                    <if test="status != null">
                        AND status = #{status}
                    </if>
                </where>
                ORDER BY gmt_create DESC
                <if test="limit != null">
                    LIMIT #{limit}
                </if>
            </select>
            """;

        // 解析XML
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(xmlContent.getBytes()));
        Element root = doc.getDocumentElement();

        // 准备参数
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", "test-uuid-001");
        parameters.put("account", "test");
        parameters.put("status", 1);
        parameters.put("limit", 5);

        System.out.println("=== 输入参数 ===");
        parameters.forEach((k, v) -> System.out.println(k + " = " + v));

        // 处理动态SQL
        String processedSql = DynamicSqlProcessor.processDynamicSql(root, parameters);
        
        System.out.println("\n=== 处理后的SQL ===");
        System.out.println(processedSql);

        // 检查SQL中包含的参数
        System.out.println("\n=== SQL中的参数 ===");
        if (processedSql.contains("#{uuid}")) {
            System.out.println("包含 #{uuid}");
        }
        if (processedSql.contains("#{account}")) {
            System.out.println("包含 #{account}");
        }
        if (processedSql.contains("#{status}")) {
            System.out.println("包含 #{status}");
        }
        if (processedSql.contains("#{limit}")) {
            System.out.println("包含 #{limit}");
        }
    }

    @Test
    public void testConditionEvaluation() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("uuid", "test-uuid-001");
        parameters.put("account", "test");
        parameters.put("status", 1);
        parameters.put("limit", 5);

        System.out.println("=== 条件评估测试 ===");
        
        // 测试各种条件
        String[] conditions = {
            "uuid != null and uuid != ''",
            "account != null and account != ''", 
            "status != null",
            "limit != null"
        };

        for (String condition : conditions) {
            try {
                // 这里需要使用反射调用私有方法，或者创建一个公共的测试方法
                System.out.println("条件: " + condition + " -> 需要通过反射测试");
            } catch (Exception e) {
                System.out.println("条件: " + condition + " -> 评估失败: " + e.getMessage());
            }
        }
    }
}
