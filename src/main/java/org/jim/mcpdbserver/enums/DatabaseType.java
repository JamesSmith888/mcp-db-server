package org.jim.mcpdbserver.enums;

import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * 数据库类型枚举
 * @author yangxin
 */
public enum DatabaseType {
    
    MYSQL("MySQL", 
          new String[]{"mysql"}, 
          new String[]{"jdbc:mysql:"}),
    
    POSTGRESQL("PostgreSQL", 
               new String[]{"postgresql"}, 
               new String[]{"jdbc:postgresql:"}),
    
    ORACLE("Oracle", 
           new String[]{"oracle"}, 
           new String[]{"jdbc:oracle:"}),
    
    SQL_SERVER("SQL Server", 
               new String[]{"sqlserver", "mssql"}, 
               new String[]{"jdbc:sqlserver:"}),
    
    H2("H2", 
       new String[]{"h2"}, 
       new String[]{"jdbc:h2:"}),
    
    SQLITE("SQLite", 
           new String[]{"sqlite"}, 
           new String[]{"jdbc:sqlite:"}),
    
    CLICKHOUSE("ClickHouse", 
               new String[]{"clickhouse"}, 
               new String[]{"jdbc:clickhouse:"}),
    
    MARIADB("MariaDB", 
            new String[]{"mariadb"}, 
            new String[]{"jdbc:mariadb:"});

    private final String displayName;
    private final String[] driverKeywords;
    private final String[] urlPrefixes;

    DatabaseType(String displayName, String[] driverKeywords, String[] urlPrefixes) {
        this.displayName = displayName;
        this.driverKeywords = driverKeywords;
        this.urlPrefixes = urlPrefixes;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * 根据驱动类名判断数据库类型
     * @param driverClassName 驱动类名
     * @return 数据库类型，如果未匹配则返回null
     */
    public static DatabaseType fromDriverClassName(String driverClassName) {
        if (!StringUtils.hasText(driverClassName)) {
            return null;
        }
        
        String lowerDriverName = driverClassName.toLowerCase();
        return Arrays.stream(values())
                .filter(type -> Arrays.stream(type.driverKeywords)
                        .anyMatch(lowerDriverName::contains))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据JDBC URL判断数据库类型
     * @param url JDBC URL
     * @return 数据库类型，如果未匹配则返回null
     */
    public static DatabaseType fromUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        
        return Arrays.stream(values())
                .filter(type -> Arrays.stream(type.urlPrefixes)
                        .anyMatch(url::startsWith))
                .findFirst()
                .orElse(null);
    }
}
