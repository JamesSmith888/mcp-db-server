# MCP DB Server

A Spring AI-based MCP capable of executing SQL operations on multiple databases.

> **Note for STDIO Mode Users:** If you prefer using STDIO mode, please check out [mcp-mysql-server](https://github.com/JamesSmith888/mcp-mysql-server) which supports the traditional STDIO protocol.

[中文文档](README.md) | [English Documentation](README_EN.md)

## Quick Start (SSE Mode)

### 1. Start the Service

Run in the project root directory:

```bash
./mvnw spring-boot:run
```

### 2. MCP Client Configuration

Add to your AI client configuration file (some clients require manually selecting SSE transport type):

```json
{
  "mcpServers": {
    "mcp-db-server": {
      "url": "http://localhost:6789/sse"
    }
  }
}
```

### 3. Data Source Configuration

Modify the `src/main/resources/datasource.yml` file:

```yaml
datasource:
  datasources:
    your_db1_name:
      url: jdbc:mysql://localhost:3306/db1
      username: root
      password: password
      default: true  # Mark as default data source
```

---

## Features

- **Multiple Database Support** - Supports MySQL, PostgreSQL, Oracle, SQL Server, H2, SQLite, MariaDB, ClickHouse, etc.
- **Multiple Data Source Support** - Configure and manage multiple database data sources
- **SSE Transport Mode** - Supports multi-client connections. More suitable for team development scenarios
- **Extension Features** - Extend functionality through Groovy scripts
- **SQL Security Control** - Prevent AI models from executing dangerous SQL operations

## Detailed Documentation

| Document                                                    | Description                                                                         |
|:------------------------------------------------------------|:------------------------------------------------------------------------------------|
| [Extensions Documentation](EXTENSIONS_EN.md)                | Detailed configuration and development guide for Groovy script extensions           |
| [Data Source Configuration Documentation](DATASOURCE_EN.md) | Detailed data source configuration, multi-environment management and best practices |
| [SQL Security Control Documentation](SQL_SECURITY_EN.md)    | Configuration and management of SQL security policies                               |

## Environment Requirements

- **JDK 21** or higher
- **Maven 3.6** or higher
