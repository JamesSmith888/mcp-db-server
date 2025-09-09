package org.jim.mcpdbserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * 数据库适配器服务，用于识别数据库类型并提供相应的SQL语法支持
 * @author yangxin
 */
@Service
@Slf4j
public class DatabaseAdapterService {

    /**
     * 获取数据源的数据库信息
     * @param dataSource 数据源
     * @return 数据库信息
     */
    public DatabaseInfo getDatabaseInfo(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            String productName = metaData.getDatabaseProductName();
            String productVersion = metaData.getDatabaseProductVersion();
            String driverName = metaData.getDriverName();
            String driverVersion = metaData.getDriverVersion();
            String url = metaData.getURL();

            log.debug("Detected database: {} {} (Driver: {} {})", productName, productVersion, driverName, driverVersion);

            return new DatabaseInfo(productName, productName, productVersion, driverName, driverVersion, url);
        } catch (SQLException e) {
            log.error("Failed to get database info: {}", e.getMessage(), e);
            return new DatabaseInfo("Unknown", "Unknown", "Unknown", "Unknown", "Unknown", "Unknown");
        }
    }


    /**
     * 数据库信息类
     */
    public record DatabaseInfo(String type, String productName, String productVersion, String driverName,
                               String driverVersion, String url) {
    }
}
