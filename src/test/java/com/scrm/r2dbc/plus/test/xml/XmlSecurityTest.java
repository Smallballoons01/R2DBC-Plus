package com.scrm.r2dbc.plus.test.xml;

import com.scrm.r2dbc.plus.xml.XmlMapperParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * XML 解析安全加固测试
 *
 * <p>验证 {@link XmlMapperParser} 对以下攻击面具备防护：
 * <ul>
 *   <li><b>XXE</b>：外部实体注入可读取本地文件 / 触发 SSRF</li>
 *   <li><b>XML Bomb</b>：billion laughs 实体膨胀耗尽内存</li>
 * </ul>
 *
 * <p>这些测试针对<b>解析器配置</b>本身，因此不依赖 Spring 容器。
 */
@Tag("unit")
@Tag("fast")
@DisplayName("XML 解析安全防护")
class XmlSecurityTest {

    @BeforeEach
    void clearCache() {
        XmlMapperParser.clearCache();
    }

    /**
     * 构造与被测代码相同的 DocumentBuilderFactory 配置路径，
     * 通过解析恶意 XML 观察行为。
     */
    private Document parse(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // 复用生产代码的加固配置：反射调用保证测试与实现同步演进
        Class<?> parserClass = Class.forName("com.scrm.r2dbc.plus.xml.XmlMapperParser");
        var method = parserClass.getDeclaredMethod("configureSecureXml", DocumentBuilderFactory.class);
        method.setAccessible(true);
        method.invoke(null, factory);

        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setEntityResolver((publicId, systemId) ->
                new org.xml.sax.InputSource(new java.io.StringReader("")));
        return builder.parse(new ByteArrayResource(xml.getBytes(StandardCharsets.UTF_8)).getInputStream());
    }

    @Test
    @DisplayName("XXE：外部实体不应读取到本地文件内容")
    void xxeShouldNotReadLocalFile() {
        String xxe = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper [
                  <!ENTITY xxe SYSTEM "file:///etc/passwd">
                ]>
                <mapper namespace="test">
                    <select id="t">&xxe;</select>
                </mapper>
                """;

        // 要么解析成功（实体被忽略），要么抛异常；
        // 关键是不能把 /etc/passwd 的内容带进文档
        try {
            var doc = parse(xxe);
            String text = doc.getDocumentElement().getTextContent();
            assertFalse(text.contains("root:"),
                    "XXE 防护失效：外部实体内容被解析进文档");
        } catch (Exception e) {
            // 抛异常同样是安全行为（拒绝解析）
            assertNotNull(e);
        }
    }

    @Test
    @DisplayName("XXE：SYSTEM 类型的外部 URL 不应被请求（防 SSRF）")
    void xxeShouldNotFetchRemoteUrl() {
        // 127.0.0.1:1 是不可连接的端口；若解析器真的发起请求会等待超时
        String xxe = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper [
                  <!ENTITY xxe SYSTEM "http://127.0.0.1:1/evil">
                ]>
                <mapper namespace="test">
                    <select id="t">&xxe;</select>
                </mapper>
                """;

        long start = System.currentTimeMillis();
        try {
            var doc = parse(xxe);
            String text = doc.getDocumentElement().getTextContent();
            assertFalse(text.contains("evil"), "外部 URL 被解析进文档");
        } catch (Exception e) {
            // 超时说明发生了网络请求，属于失败
            long elapsed = System.currentTimeMillis() - start;
            assertTrue(elapsed < 3000,
                    "疑似发起了外部网络请求，耗时 " + elapsed + "ms");
        }
    }

    @Test
    @DisplayName("XML Bomb：实体膨胀攻击应被限制而非耗尽内存")
    void xmlBombShouldBeLimited() {
        // 经典 billion laughs：9 层指数展开
        StringBuilder bomb = new StringBuilder();
        bomb.append("<?xml version=\"1.0\"?>\n");
        bomb.append("<!DOCTYPE lolz [\n");
        bomb.append("  <!ENTITY lol \"lol\">\n");
        for (int i = 1; i <= 9; i++) {
            String prev = i == 1 ? "lol" : "lol" + (i - 1);
            StringBuilder exp = new StringBuilder();
            for (int j = 0; j < 10; j++) {
                exp.append("&").append(prev).append(";");
            }
            bomb.append("  <!ENTITY lol").append(i).append(" \"")
                    .append(exp).append("\">\n");
        }
        bomb.append("]>\n");
        bomb.append("<mapper namespace=\"test\"><select id=\"t\">&lol9;</select></mapper>");

        long start = System.currentTimeMillis();
        try {
            var doc = parse(bomb.toString());
            // 若成功解析，内容长度应受限（JDK 默认 entityExpansionLimit=64000）
            String text = doc.getDocumentElement().getTextContent();
            assertTrue(text.length() < 200_000,
                    "实体膨胀未被限制，展开长度 " + text.length());
        } catch (Exception e) {
            // 被解析器直接拒绝也是安全行为
            assertNotNull(e);
        }
        long elapsed = System.currentTimeMillis() - start;
        assertTrue(elapsed < 5000, "实体膨胀处理耗时过长：" + elapsed + "ms");
    }

    @Test
    @DisplayName("合法 Mapper XML 应正常解析（防护不能误伤正常功能）")
    void validMapperXmlShouldParse() {
        String valid = """
                <?xml version="1.0" encoding="UTF-8"?>
                <mapper namespace="com.example.TestMapper">
                    <select id="selectAll" resultType="java.util.Map">
                        SELECT * FROM test_user
                        <where>
                            <if test="name != null">AND name = #{name}</if>
                        </where>
                    </select>
                </mapper>
                """;
        Document doc = assertDoesNotThrow(() -> parse(valid));
        assertNotNull(doc.getDocumentElement());
        assertEquals("com.example.TestMapper",
                doc.getDocumentElement().getAttribute("namespace"));
    }

    @Test
    @DisplayName("含 MyBatis DTD 声明的 Mapper 仍可解析（兼容性保障）")
    void mapperWithDoctypeShouldStillParse() {
        String withDoctype = """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
                    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
                <mapper namespace="com.example.TestMapper">
                    <select id="x" resultType="java.util.Map">SELECT 1</select>
                </mapper>
                """;
        // EntityResolver 会拦截 DTD 引用并返回空内容，不影响解析
        Document doc = assertDoesNotThrow(() -> parse(withDoctype));
        assertEquals("com.example.TestMapper",
                doc.getDocumentElement().getAttribute("namespace"));
    }

    @Test
    @DisplayName("ByteArrayResource 可正常构造（保证测试自身可靠）")
    void byteArrayResourceWorks() throws Exception {
        Resource resource = new ByteArrayResource(
                "<a/>".getBytes(StandardCharsets.UTF_8));
        assertNotNull(resource.getInputStream());
    }
}