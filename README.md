# MCP DB Server

一个基于 Spring AI 的MCP，可执行任意 SQL，支持多种数据库。
> **STDIO模式:** 如果您更喜欢使用STDIO模式，请查看 [mcp-mysql-server](https://github.com/JamesSmith888/mcp-mysql-server) 

[中文文档](README.md) | [English Documentation](README_EN.md)

## 快速上手（SSE模式）

### 1. 启动服务

在项目根目录下执行：

```bash
./mvnw spring-boot:run
```

### 2. MCP 客户端配置

在AI客户端配置文件中添加（部分客户端需要手动选择SSE传输类型）：

```json
{
  "mcpServers": {
    "mcp-db-server": {
      "url": "http://localhost:6789/sse"
    }
  }
}
```

### 3. 数据源配置

修改 `src/main/resources/datasource.yml` 文件：

```yaml
datasource:
  datasources:
    your_db1_name:
      url: jdbc:mysql://localhost:3306/db1
      username: root
      password: password
      default: true  # 标记为默认数据源
```

---

## 功能特点

- **多数据库支持** - 支持 MySQL、PostgreSQL、Oracle、SQL Server、H2、SQLite、MariaDB、ClickHouse 等
- **多数据源支持** - 配置和管理多个数据库数据源
- **SSE传输模式** - 支持多客户端连接。更适用团队开发场景
- **扩展功能** - 通过 Groovy 脚本扩展功能
- **SQL 安全控制** - 防止 AI 模型执行危险 SQL 操作

## 详细文档

| 文档                            | 描述                    |
|:------------------------------|:----------------------|
| [扩展功能文档](EXTENSIONS.md)       | Groovy 脚本扩展的详细配置和开发指南 |
| [数据源配置文档](DATASOURCE.md)      | 数据源的详细配置、多环境管理和最佳实践   |
| [SQL 安全控制文档](SQL_SECURITY.md) | SQL 安全策略的配置和管理        |

## 环境要求

- **JDK 21+**
- **Maven 3.6+**
