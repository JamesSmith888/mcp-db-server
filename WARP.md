# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

Project: mcp-db-server (Spring Boot + Spring AI MCP Server)

Summary
- Purpose: Expose an MCP (Model Context Protocol) HTTP server that can execute SQL against one or more JDBC data sources, with optional Groovy-powered data processing extensions and SQL safety controls.
- Tech stack: Java 21, Spring Boot 3.5.x, Spring JDBC, spring-ai-starter-mcp-server-webmvc, Groovy. Packaged/build with Maven.
- Service port: 6789 (see src/main/resources/application.yml)
- Protocol: SSE mode (single server, multiple clients), replaces stdio mode for better resource management

Important notes from README
- JDK 21+ and Maven 3.6+ are required.
- Datasource config lives in src/main/resources/datasource.yml by default and can be overridden via --datasource.config=...
- Groovy extensions are defined in src/main/resources/extension.yml; optional external scripts/dep jars can be loaded by setting -Dloader.path to src/main/resources/groovy when running.
- Supports multiple databases: MySQL, PostgreSQL, Oracle, SQL Server, H2, SQLite, MariaDB, ClickHouse, etc.
- Recommended startup: cd to project root and run ./mvnw spring-boot:run
- Client configuration (SSE mode): {"mcp-db-server": {"url": "http://localhost:6789/sse"}}

Build, run, test
- Build (fast, no tests)
  ./mvnw -q -DskipTests clean package

- Build (default)
  ./mvnw -q clean package

- Run in dev (Spring Boot plugin)
  ./mvnw -q spring-boot:run

  With a custom datasource file:
  ./mvnw -q spring-boot:run -Dspring-boot.run.arguments="--datasource.config=/absolute/path/to/datasource.yml"

  With Groovy extensions on the classpath (if you have groovy scripts/deps under src/main/resources/groovy):
  ./mvnw -q spring-boot:run -Dspring-boot.run.jvmArguments="-Dloader.path=src/main/resources/groovy"

  Combine both:
  ./mvnw -q spring-boot:run -Dspring-boot.run.jvmArguments="-Dloader.path=src/main/resources/groovy" -Dspring-boot.run.arguments="--datasource.config=/absolute/path/to/datasource.yml"

- Run the packaged jar
  java -jar target/mcp-db-server-0.0.1-SNAPSHOT.jar

  With custom datasource:
  java -jar target/mcp-db-server-0.0.1-SNAPSHOT.jar --datasource.config=/absolute/path/to/datasource.yml

  With Groovy extensions:
  java -Dloader.path=src/main/resources/groovy -jar target/mcp-db-server-0.0.1-SNAPSHOT.jar

- Tests
  This repo does not currently include tests. If/when tests are added:
  - Run all: ./mvnw -q test
  - Run a single test class: ./mvnw -q -Dtest=FullyQualifiedClassName test
  - Run a single test method: ./mvnw -q -Dtest=FullyQualifiedClassName#methodName test

Lint/format
- No lint/format tooling is currently configured in pom.xml.

Runtime behavior and configuration (big picture)
- MCP server
  - spring-ai-starter-mcp-server-webmvc provides the MCP HTTP server scaffolding. The server name/version is set in application.yml under spring.ai.mcp.server.
  - The service listens on port 6789 (server.port) and exposes SSE endpoint at /sse.
  - SSE mode allows single server instance to serve multiple AI clients simultaneously, improving resource efficiency.

- SQL execution and data sources
  - Spring JDBC is used to interact with databases. Multiple JDBC data sources can be defined under datasource.datasources in datasource.yml. One can be flagged default: true; otherwise, the first is treated as default.
  - You can override the default datasource file path at runtime with --datasource.config=... (works for both spring-boot:run and java -jar).
  - Drivers for common databases (MySQL, PostgreSQL, Oracle, SQL Server, H2, SQLite, MariaDB, ClickHouse, Derby, HSQLDB) are declared as runtime deps in pom.xml; only those relevant to your configured URLs are needed at runtime.

- SQL safety controls
  - A simple pre-execution safety check denies queries containing configured dangerous keywords (case-insensitive). Configure under sql.security in application.yml.
  - Example keys include DML/DDL/DCL operations like update, delete, drop, create, alter, truncate, grant, revoke, call, execute, commit, rollback, etc.

- Extensions (Groovy)
  - extension.yml declares available extensions; each entry can specify name, description, enabled, prompt, and either inline script or a script file via mainFileName (with assets under src/main/resources/groovy/<extension>/script and dependency jars under .../dependency).
  - To run extensions that depend on those groovy resources/jars, include -Dloader.path=src/main/resources/groovy in your JVM args.

Key files and their roles
- pom.xml: Maven build, dependency management (Spring Boot, Spring AI MCP server starter, JDBC drivers, Groovy). Note that the Apache Derby dependency version is redacted (*********) and must be set to a valid version or removed if not needed to build successfully.
- src/main/resources/application.yml: Service port, MCP server metadata, SQL safety configuration, logging file path.
- src/main/resources/datasource.yml: Default multi-datasource configuration (JDBC URLs, credentials, default flag, optional Hikari settings). Use a separate file and --datasource.config=... for environment-specific secrets.
- src/main/resources/datasource-multi-db-example.yml: Example covering many JDBC vendors and recommended options.
- src/main/resources/extension.yml: Extension registry for Groovy scripts and prompts.

Workflow tips for Warp
- If build fails immediately on dependencies, inspect pom.xml (notably the Derby dependency with a placeholder version). Either remove that dependency or set a specific version before building.
- For environment-specific database credentials, prefer a separate YAML (not in VCS) and pass --datasource.config=/absolute/path/your.yml at run time.
- When using Groovy extensions, remember to include -Dloader.path=src/main/resources/groovy; otherwise external scripts/deps will not be loaded.

References
- README.md: Quick start, environment requirements.
- DATASOURCE.md: Datasource YAML format, overriding via CLI args, examples for multiple vendors.
- EXTENSIONS.md: Extension model, directory conventions, running with -Dloader.path, troubleshooting.
- SQL_SECURITY.md: Configuring and understanding SQL safety checks and error responses.

