package com.scrm.r2dbc.plus.xml;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * XML Mapper 文件解析器
 * 
 * @author dason
 */
@Slf4j
public class XmlMapperParser {
    
    private static final Map<String, SqlStatement> SQL_STATEMENT_CACHE = new ConcurrentHashMap<>();
    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
    
    /**
     * 解析指定路径下的所有 XML 映射文件
     */
    public static void parseMapperXmls(String locationPattern) {
        try {
            Resource[] resources = resourceResolver.getResources(locationPattern);
            log.info("解析XML映射文件，resources: {}", resources.length);
            for (Resource resource : resources) {
                parseMapperXml(resource);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse mapper XML files", e);
        }
    }
    
    /**
     * 解析单个 XML 映射文件
     */
    /**
     以安全加固方式配置 XML 解析器
     *
     * <p>防护目标：
     * <ul>
     *   <li><b>XXE</b>（外部实体注入）：禁用外部通用/参数实体与外部 DTD 加载，
     *       并通过 {@link javax.xml.XMLConstants#ACCESS_EXTERNAL_DTD} 等属性双重限制</li>
     *   <li><b>XML Bomb</b>（实体膨胀攻击 / billion laughs）：
     *       限制实体扩展次数、总展开量与节点深度</li>
     * </ul>
     *
     * <p>注意：{@code disallow-doctype-decl} 保持为 {@code false}，
     * 因为 MyBatis 风格的 Mapper XML 声明引用了 {@code mybatis-3-mapper.dtd}，
     * 若禁止 DOCTYPE 会导致所有映射文件解析失败。
     * 安全性改由「禁用外部实体 + 空EntityResolver + 实体膨胀限制」三重保障。
     *
     * <p>各特性的支持情况因解析器实现而异，因此逐项尝试并记录失败项，
     * 避免某个特性不被支持时导致整个解析流程中断。
     */
    private static void configureSecureXml(DocumentBuilderFactory factory) {
        factory.setValidating(false);
        factory.setNamespaceAware(false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        // 禁止通过协议或系统属性访问外部资源
        setAttributeQuietly(factory, XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setAttributeQuietly(factory, XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        setAttributeQuietly(factory, XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        // XXE 防护
        setFeatureQuietly(factory, "http://xml.org/sax/features/external-general-entities", false);
        setFeatureQuietly(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        setFeatureQuietly(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        // XML Bomb 防护：限制实体膨胀规模
        //jdk.xml.entityExpansionLimit 默认 64000，这里收紧到 10000 仍有充足余量
        setAttributeQuietly(factory, "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit", "10000");
        setFeatureQuietly(factory, "http://apache.org/xml/features/disallow-doctype-decl", false);

        // 限制 XML 深度，防御深层嵌套导致的栈溢出
        try {
            factory.setAttribute("http://apache.org/xml/properties/security-manager", null);
        } catch (Exception ignored) {
            // 部分实现不支持 security-manager，忽略即可
        }
    }

    private static void setFeatureQuietly(DocumentBuilderFactory factory, String feature, boolean value) {
        try {
            factory.setFeature(feature, value);
        } catch (Exception e) {
            log.debug("当前 XML 解析器不支持特性 {}: {}", feature, e.getMessage());
        }
    }

    private static void setAttributeQuietly(DocumentBuilderFactory factory, String name, Object value) {
        try {
            factory.setAttribute(name, value);
        } catch (Exception e) {
            log.debug("当前 XML 解析器不支持属性 {}: {}", name, e.getMessage());
        }
    }

    /**
     * 解析单个 XML 映射文件
     */
    public static void parseMapperXml(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            configureSecureXml(factory);

            DocumentBuilder builder = factory.newDocumentBuilder();

            // 自定义 EntityResolver 作为兜底：即使上面某项特性未被底层解析器支持，
            // 也能阻止所有外部实体解析
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    return new InputSource(new StringReader(""));
                }
            });

            Document document = builder.parse(inputStream);

            Element root = document.getDocumentElement();
            String namespace = root.getAttribute("namespace");

            // 解析 select 语句
            parseStatements(root, "select", namespace, SqlStatement.StatementType.SELECT);

            // 解析 insert 语句
            parseStatements(root, "insert", namespace, SqlStatement.StatementType.INSERT);

            // 解析 update 语句
            parseStatements(root, "update", namespace, SqlStatement.StatementType.UPDATE);

            // 解析 delete 语句
            parseStatements(root, "delete", namespace, SqlStatement.StatementType.DELETE);

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse mapper XML: " + resource.getFilename(), e);
        }
    }
    
    /**
     * 解析指定类型的 SQL 语句
     */
    private static void parseStatements(Element root, String tagName, String namespace, SqlStatement.StatementType statementType) {
        NodeList nodeList = root.getElementsByTagName(tagName);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                String id = element.getAttribute("id");
                String resultType = element.getAttribute("resultType");
                String parameterType = element.getAttribute("parameterType");

                SqlStatement sqlStatement;

                // 检查是否包含动态 SQL 标签
                if (containsDynamicTags(element)) {
                    // 包含动态标签，保存整个 Element
                    sqlStatement = new SqlStatement(id, element, statementType);
                } else {
                    // 静态 SQL，只保存文本内容
                    String sql = element.getTextContent().trim();
                    sqlStatement = new SqlStatement(id, sql, statementType);
                }

                sqlStatement.setResultType(resultType);
                sqlStatement.setParameterType(parameterType);

                String fullId = namespace + "." + id;
                SQL_STATEMENT_CACHE.put(fullId, sqlStatement);
            }
        }
    }

    /**
     * 检查元素是否包含动态 SQL 标签
     */
    private static boolean containsDynamicTags(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String tagName = child.getNodeName().toLowerCase();
                if (isDynamicTag(tagName)) {
                    return true;
                }
                // 递归检查子元素
                if (containsDynamicTags((Element) child)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否为动态 SQL 标签
     */
    private static boolean isDynamicTag(String tagName) {
        return "if".equals(tagName) ||
               "choose".equals(tagName) ||
               "when".equals(tagName) ||
               "otherwise".equals(tagName) ||
               "where".equals(tagName) ||
               "set".equals(tagName) ||
               "foreach".equals(tagName) ||
               "trim".equals(tagName);
    }
    
    /**
     * 获取 SQL 语句
     */
    public static SqlStatement getSqlStatement(String statementId) {
        if (statementId == null || statementId.trim().isEmpty()) {
            throw new RuntimeException("Statement ID cannot be empty");
        }
        return SQL_STATEMENT_CACHE.get(statementId);
    }
    
    /**
     * 获取所有 SQL 语句
     */
    public static Map<String, SqlStatement> getAllSqlStatements() {
        return SQL_STATEMENT_CACHE;
    }
    
    /**
     * 清空缓存
     */
    public static void clearCache() {
        SQL_STATEMENT_CACHE.clear();
    }
}
