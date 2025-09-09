# 数据源配置

支持多数据库（MySQL、PostgreSQL、Oracle、SQL Server、H2、SQLite、MariaDB、ClickHouse 等）。默认读取 `src/main/resources/datasource.yml`。

## 配置位置
- 默认：`src/main/resources/datasource.yml`
- 覆盖：运行时通过 `--datasource.config=/absolute/path/your-datasource.yml`
  - Maven 启动：`./mvnw spring-boot:run -Dspring-boot.run.arguments="--datasource.config=/path/to/ds.yml"`
  - JAR 启动：`java -jar target/mcp-db-server-0.0.1-SNAPSHOT.jar --datasource.config=/path/to/ds.yml`

## 配置格式
```yaml
datasource:
  datasources:
    name1:
      url: jdbc:vendor://host:port/db
      username: user
      password: pass
      default: true   # 可选，未指定则默认第一个
    name2:
      url: jdbc:vendor://...
      username: ...
      password: ...
```

## 使用规则
- 未指定数据源时，使用 `default: true` 的数据源；若未设置，则使用列表第一个。
- 多数据源可并行查询或按名称选择（由客户端工具/接口控制）。

## 示例（MySQL 与 ClickHouse）
```yaml
datasource:
  datasources:
    mysql_primary:
      url: jdbc:mysql://localhost:3306/test_db
      username: root
      password: password
      default: true
    clickhouse_primary:
      url: jdbc:clickhouse://localhost:8123/default
      username: default
      password: ""
```

## 建议
- 将不同环境的凭据放到独立的 YAML 文件，通过 `--datasource.config` 指定。
- 仅保留实际需要的 JDBC 驱动依赖，减少体积与风险。
