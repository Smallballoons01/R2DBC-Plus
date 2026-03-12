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
    public static void parseMapperXml(Resource resource) {
        try (InputStream inputStream = resource.getInputStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            
            // 禁用外部DTD验证和实体解析，防止网络连接问题和XXE攻击
            factory.setValidating(false);
            factory.setNamespaceAware(false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            // 设置自定义EntityResolver，完全阻止外部实体解析
            builder.setEntityResolver(new EntityResolver() {
                @Override
                public InputSource resolveEntity(String publicId, String systemId) {
                    // 返回空的InputSource，阻止任何外部实体解析
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
