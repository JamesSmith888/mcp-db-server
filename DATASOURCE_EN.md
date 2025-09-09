# Data Source Configuration

Supports multiple databases (MySQL, PostgreSQL, Oracle, SQL Server, H2, SQLite, MariaDB, ClickHouse, etc.). Reads `src/main/resources/datasource.yml` by default.

## Configuration Location
- Default: `src/main/resources/datasource.yml`
- Override: Use `--datasource.config=/absolute/path/your-datasource.yml` at runtime
  - Maven startup: `./mvnw spring-boot:run -Dspring-boot.run.arguments="--datasource.config=/path/to/ds.yml"`
  - JAR startup: `java -jar target/mcp-db-server-0.0.1-SNAPSHOT.jar --datasource.config=/path/to/ds.yml`

## Configuration Format
```yaml
datasource:
  datasources:
    name1:
      url: jdbc:vendor://host:port/db
      username: user
      password: pass
      default: true   # Optional, defaults to first if not specified
    name2:
      url: jdbc:vendor://...
      username: ...
      password: ...
```

## Usage Rules
- When no data source is specified, uses the one marked `default: true`; if none set, uses the first in list.
- Multiple data sources can be queried in parallel or selected by name (controlled by client tools/interfaces).

## Example (MySQL & ClickHouse)
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

## Recommendations
- Store different environment credentials in separate YAML files, specify via `--datasource.config`.
- Only keep actually needed JDBC driver dependencies to reduce size and risk.
