package org.jim.mcpdbserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.jim.mcpdbserver.config.extension.GroovyService;
import org.jim.mcpdbserver.mcp.MysqlOptionService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
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
    public ToolCallbackProvider mysqlToolCallbackProvider(MysqlOptionService mysqlOptionService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(mysqlOptionService)
                .build();
    }

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> myResources() {
        McpSchema.Annotations annotations = new McpSchema.Annotations(
                List.of(McpSchema.Role.USER, McpSchema.Role.ASSISTANT), 0.9
        );

        var systemInfoResource = new McpSchema.Resource("file:///Users/xin.y/Documents/table.json", "测试数据库与表信息", "列出数据库与表信息", "application/json", annotations);
        var resourceSpecification = new McpServerFeatures.SyncResourceSpecification(systemInfoResource, (exchange, request) -> {
            try {
                var systemInfo = Map.of("test1", "test1111");
                String jsonContent = new ObjectMapper().writeValueAsString(systemInfo);
                return new McpSchema.ReadResourceResult(
                        List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent)));
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate system info", e);
            }
        });


        return List.of(resourceSpecification);
    }

    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> extensionInfo() {
        McpSchema.Annotations annotations = new McpSchema.Annotations(List.of(McpSchema.Role.USER, McpSchema.Role.ASSISTANT), 0.9);

        var systemInfoResource = new McpSchema.Resource("file:///Users/xin.y/Documents/table.json", "测试数据库与表信息", "列出数据库与表信息", "application/json", annotations);
        var resourceSpecification = new McpServerFeatures.SyncResourceSpecification(systemInfoResource, (exchange, request) -> {
            try {
                var systemInfo = Map.of("test1", "test1111");
                String jsonContent = new ObjectMapper().writeValueAsString(systemInfo);
                return new McpSchema.ReadResourceResult(
                        List.of(new McpSchema.TextResourceContents(request.uri(), "application/json", jsonContent)));
            } catch (Exception e) {
                throw new RuntimeException("Failed to generate system info", e);
            }
        });


        return List.of(resourceSpecification);
    }


    @Bean
    public List<McpServerFeatures.SyncPromptSpecification> myPrompts() {
        var prompt = new McpSchema.Prompt("greeting", "A friendly greeting prompt", List.of(new McpSchema.PromptArgument("name", "The name to greet", true)));

        var promptSpecification = new McpServerFeatures.SyncPromptSpecification(prompt, (exchange, getPromptRequest) -> {
            String nameArgument = (String) getPromptRequest.arguments().get("name");
            if (nameArgument == null) {
                nameArgument = "friend";
            }
            var userMessage = new McpSchema.PromptMessage(McpSchema.Role.USER, new McpSchema.TextContent("Hello " + nameArgument + "! How can I assist you today?"));
            return new McpSchema.GetPromptResult("A personalized greeting message", List.of(userMessage));
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
