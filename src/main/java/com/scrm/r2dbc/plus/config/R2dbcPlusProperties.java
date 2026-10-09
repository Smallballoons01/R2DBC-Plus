package com.scrm.r2dbc.plus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * R2DBC-Plus 配置属性
 *
 * <p>在 application.yml 中通过 {@code r2dbc-plus.*} 前缀配置。
 *
 * @author dason
 */
@ConfigurationProperties(prefix = "r2dbc-plus")
public class R2dbcPlusProperties {

    /**
     * XML 映射文件扫描位置
     */
    private String[] mapperLocations = {"classpath*:mapper/**/*.xml"};

    /**
     * 全局表前缀
     */
    private String tablePrefix = "";

    /**
     * 全局列前缀
     */
    private String columnPrefix = "";

    /**
     * 是否在启动时解析 XML 映射文件
     */
    private boolean xmlEnabled = true;

    /**
     * 分页最大条数保护，防止 size 过大拖垮数据库
     */
    private long maxPageSize = 500L;

    /**
     * 单次批量插入的最大批大小
     */
    private int insertBatchSize = 1000;

    /**
     * 默认批量操作的分批数量
     */
    private int defaultBatchSize = 1000;

    /**
     * 各操作的超时时间，null 表示不限制
     */
    private Duration queryTimeout;

    public String[] getMapperLocations() {
        return mapperLocations;
    }

    public void setMapperLocations(String[] mapperLocations) {
        this.mapperLocations = mapperLocations;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public String getColumnPrefix() {
        return columnPrefix;
    }

    public void setColumnPrefix(String columnPrefix) {
        this.columnPrefix = columnPrefix;
    }

    public boolean isXmlEnabled() {
        return xmlEnabled;
    }

    public void setXmlEnabled(boolean xmlEnabled) {
        this.xmlEnabled = xmlEnabled;
    }

    public long getMaxPageSize() {
        return maxPageSize;
    }

    public void setMaxPageSize(long maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public int getInsertBatchSize() {
        return insertBatchSize;
    }

    public void setInsertBatchSize(int insertBatchSize) {
        this.insertBatchSize = insertBatchSize;
    }

    public int getDefaultBatchSize() {
        return defaultBatchSize;
    }

    public void setDefaultBatchSize(int defaultBatchSize) {
        this.defaultBatchSize = defaultBatchSize;
    }

    public Duration getQueryTimeout() {
        return queryTimeout;
    }

    public void setQueryTimeout(Duration queryTimeout) {
        this.queryTimeout = queryTimeout;
    }
}