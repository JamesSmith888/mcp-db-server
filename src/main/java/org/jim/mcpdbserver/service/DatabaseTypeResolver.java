package org.jim.mcpdbserver.service;

import lombok.extern.slf4j.Slf4j;
import org.jim.mcpdbserver.config.DataSourceConfig;
import org.jim.mcpdbserver.enums.DatabaseType;
import org.jim.mcpdbserver.util.SpringContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 数据库类型解析服务
 * @author yangxin
 */
@Service
@Slf4j
public class DatabaseTypeResolver {

    /**
     * 获取默认数据源的数据库类型
     * @return 数据库类型字符串
     */
    public String getDefaultDatabaseType() {
        try {
            if (!SpringContextHolder.isApplicationContextAvailable()) {
                log.warn("Spring ApplicationContext is not available");
                return "Unknown Database";
            }

            DataSourceConfig dataSourceConfig = SpringContextHolder.getBean(DataSourceConfig.class);
            Map<String, Object> defaultDsProperties = dataSourceConfig.getDefaultDataSourceProperties();
            
            return determineDatabaseType(defaultDsProperties);
        } catch (Exception e) {
            log.warn("Failed to get default database type: {}", e.getMessage());
            return "Unknown Database";
        }
    }

    /**
     * 根据数据源配置判断数据库类型
     * @param dsProperties 数据源配置属性
     * @return 数据库类型字符串
     */
    public String determineDatabaseType(Map<String, Object> dsProperties) {
        if (dsProperties == null || dsProperties.isEmpty()) {
            return "Unknown Database";
        }

        // 优先从 driver-class-name 判断
        String driverClassName = (String) dsProperties.get("driver-class-name");
        if (StringUtils.hasText(driverClassName)) {
            DatabaseType typeFromDriver = DatabaseType.fromDriverClassName(driverClassName);
            if (typeFromDriver != null) {
                return typeFromDriver.getDisplayName();
            }
        }

        // 从 URL 判断
        String url = (String) dsProperties.get("url");
        if (StringUtils.hasText(url)) {
            DatabaseType typeFromUrl = DatabaseType.fromUrl(url);
            if (typeFromUrl != null) {
                return typeFromUrl.getDisplayName();
            }
        }

        // 如果都无法识别，返回包含原始信息的描述
        if (StringUtils.hasText(driverClassName)) {
            return "Unknown Database (" + driverClassName + ")";
        } else if (StringUtils.hasText(url)) {
            return "Unknown Database (" + url.split(":")[1] + ")";
        }

        return "Unknown Database";
    }
}
