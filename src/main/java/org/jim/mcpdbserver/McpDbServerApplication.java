package org.jim.mcpdbserver;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jim.mcpdbserver.config.extension.GroovyService;
import org.jim.mcpdbserver.mcp.DatabaseOperationService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.function.BiConsumer;

@SpringBootApplication
@Slf4j
public class McpDbServerApplication {

    /**
     * 应用启动入口
     * @param args 命令行参数
     */
    public static void main(String[] args) {

        SpringApplication.run(McpDbServerApplication.class, args);
    }


    @Bean
    public ToolCallbackProvider mysqlToolCallbackProvider(DatabaseOperationService optionService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(optionService)
                .build();
    }

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> extensionInfo() {
        McpSchema.Annotations annotations = new McpSchema.Annotations(List.of(McpSchema.Role.USER, McpSchema.Role.ASSISTANT), 0.9);


        String description = "Returns information about all available Groovy script extensions including their names, descriptions, and parameters. Use before calling executeGroovyScript. IMPORTANT: Call this resource when you encounter data from SQL queries that appears to be encrypted, encoded, or requires special processing (such as Base64 strings, hex values, encrypted fields, JSON strings, timestamps, etc.) to discover available data processing extensions.";

        var systemInfoResource = new McpSchema.Resource("file:///Users/xin.y/Documents/table.json", "已配置的扩展信息", description, "text/plain", annotations);
        var resourceSpecification = new McpServerFeatures.SyncResourceSpecification(systemInfoResource, (exchange, request) -> {
            try {
                String text = """
                        # 此配置目前考虑用于扩展功能的配置
                        extensions:
                          - name: stringProcessor
                            script: "def greet(name) { return \\"Hello, $name!\\" }; greet('Java')"
                            #script-path: "classpath:com/example/CustomFilter.groovy"
                            description: "反转字符串"
                            prompt: "the string to reverse: "
                          - name: customFilter
                            enabled: false
                            #cript-path: "classpath:com/example/CustomFilter.groovy"
                            description: "自定义Java类处理"
                            prompt: "the string to process: "
                          - name: zstdDecode
                            description: "解码业务快照数据"
                            prompt: "decode the snapshot_data from the table core_snapshot or encrypted data of com.github.luben.zstd"
                          - name: SM4Decrypt
                            description: "SM4国密算法解密工具，支持单个字符串或批量解密，返回详细的解密结果和状态信息"
                            prompt: "使用SM4国密算法解密Base64编码的加密数据。支持多种输入格式：1) 单个加密字符串 2) JSON数组：[\\"encrypted1\\",\\"encrypted2\\"] 3) 逗号分隔：encrypted1,encrypted2。返回包含原文、密文、解密状态的详细JSON结果"
                        
                        """;

                return new McpSchema.ReadResourceResult(List.of(new McpSchema.TextResourceContents(request.uri(), "text/plain", text)));
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate system info", e);
            }
        });


        return List.of(resourceSpecification);
    }


    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> executeSqlOnDefaultPrompts() {

        McpSchema.PromptArgument dbType = new McpSchema.PromptArgument("dbType", "数据库类型。默认：MySQL", false);
        var prompt = new McpSchema.Prompt("executeSqlOnDefault", "操作默认数据源", List.of(dbType));


        var promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
            String dbTypeArgument = (String) getPromptRequest.arguments().get("dbType");
            if (StringUtils.isBlank(dbTypeArgument)) {
                dbTypeArgument = "MySQL";
            }

            log.info("Generating prompt for dbType: {}", dbTypeArgument);
            var promptContext = String.format("""
                    任务：
                     通过MCP（模型上下文协议）在默认数据源上执行SQL。
                    数据库方言：
                     %s
                     — 指定目标数据库类型，用以调整语法风格（如分页、函数、标识符符号等）。
                    """, dbTypeArgument);
            log.info("Prompt context: {}", promptContext);
            var userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent(promptContext));
            return new McpSchema.GetPromptResult("executeSqlOnDefault", List.of(userMessage));
        });

        return List.of(promptSpecification);
    }


    @Bean
    public BiConsumer<McpSyncServerExchange, List<McpSchema.Root>> rootsChangeHandler() {
        return (exchange, roots) -> {
            //log.info("Registering root resources: {}", roots);
        };
    }

    @Bean
    public ApplicationRunner extensionInfoLogger(GroovyService groovyService) {
        return args -> {
            log.info("=== Extension Information ===");
            groovyService.getAllExtensions();
            log.info("=== Extension Information End ===");
        };
    }

}
