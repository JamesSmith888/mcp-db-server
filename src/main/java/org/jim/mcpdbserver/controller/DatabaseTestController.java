package org.jim.mcpdbserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jim.mcpdbserver.config.extension.Extension;
import org.jim.mcpdbserver.mcp.DatabaseOperationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 数据库测试控制器 - 用于测试MCP数据库工具
 * 提供Web接口来测试各种数据库操作功能
 *
 * @author yangxin
 */
@RestController
@RequestMapping("/api/test/database")
@RequiredArgsConstructor
@Slf4j
public class DatabaseTestController {

    private final DatabaseOperationService databaseOperationService;

    /**
     * 测试在默认数据源上执行SQL
     * GET /api/test/database/default?sql=SELECT 1
     */
    @GetMapping("/default")
    public ResponseEntity<JsonNode> testExecuteSqlOnDefault(@RequestParam String sql) {
        log.info("Testing executeSqlOnDefault with SQL: {}", sql);
        try {
            JsonNode result = databaseOperationService.executeSqlOnDefault(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing executeSqlOnDefault: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 测试在所有数据源上执行SQL
     * POST /api/test/database/all
     * Body: {"sql": "SELECT 1"}
     */
    @PostMapping("/all")
    public ResponseEntity<Map<String, Object>> testExecuteSqlOnAll(@RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        log.info("Testing executeSql on all datasources with SQL: {}", sql);
        try {
            Map<String, Object> result = databaseOperationService.executeSql(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing executeSql on all datasources: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 测试在指定数据源上执行SQL
     * POST /api/test/database/datasource/{dataSourceName}
     * Body: {"sql": "SELECT 1"}
     */
    @PostMapping("/datasource/{dataSourceName}")
    public ResponseEntity<Map<String, Object>> testExecuteSqlWithDataSource(
            @PathVariable String dataSourceName,
            @RequestBody Map<String, String> request) {
        String sql = request.get("sql");
        log.info("Testing executeSqlWithDataSource on [{}] with SQL: {}", dataSourceName, sql);
        try {
            Map<String, Object> result = databaseOperationService.executeSqlWithDataSource(dataSourceName, sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing executeSqlWithDataSource: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取所有数据源信息
     * GET /api/test/database/datasources
     */
    @GetMapping("/datasources")
    public ResponseEntity<Map<String, Object>> testListDataSources() {
        log.info("Testing listDataSources");
        try {
            Map<String, Object> result = databaseOperationService.getDataSourcesInfo();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing listDataSources: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取数据库信息
     * GET /api/test/database/info/{dataSourceName}
     * GET /api/test/database/info (使用默认数据源)
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> testGetDatabaseInfo() {
        try {
            Map<String, Object> result = databaseOperationService.getDataSourcesInfo();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing getDatabaseInfo: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


    /**
     * 获取所有扩展信息
     * GET /api/test/database/extensions
     */
    @GetMapping("/extensions")
    public ResponseEntity<List<Extension>> testGetAllExtensions() {
        log.info("Testing getAllExtensions");
        try {
            List<Extension> result = databaseOperationService.getAllExtensions();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing getAllExtensions: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 测试执行Groovy脚本
     * POST /api/test/database/groovy/{extensionName}
     * Body: {"input": "test input"}
     */
    @PostMapping("/groovy/{extensionName}")
    public ResponseEntity<JsonNode> testExecuteGroovyScript(
            @PathVariable String extensionName,
            @RequestBody Map<String, String> request) {
        String input = request.get("input");
        log.info("Testing executeGroovyScript with extension [{}] and input: {}", extensionName, input);
        try {
            JsonNode result = databaseOperationService.executeGroovyScript(extensionName, input);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing executeGroovyScript: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }


}
