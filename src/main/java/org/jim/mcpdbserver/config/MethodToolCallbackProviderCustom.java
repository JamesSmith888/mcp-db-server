package org.jim.mcpdbserver.config;

import org.jim.mcpdbserver.service.DatabaseTypeResolver;
import org.jim.mcpdbserver.util.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.DefaultToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.tool.support.ToolUtils;
import org.springframework.ai.util.ParsingUtils;
import org.springframework.ai.util.json.schema.JsonSchemaGenerator;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * FIXME 此处重写MethodToolCallbackProvider，目前主要是为了实现 @Tool description 可以动态化
 * A {@link MethodToolCallbackProvider} that scans one or more objects for methods annotated with {@link Tool}
 */
public class MethodToolCallbackProviderCustom implements ToolCallbackProvider {

    private static final Logger logger = LoggerFactory.getLogger(MethodToolCallbackProviderCustom.class);

    private final List<Object> toolObjects;

    private MethodToolCallbackProviderCustom(List<Object> toolObjects) {
        Assert.notNull(toolObjects, "toolObjects cannot be null");
        Assert.noNullElements(toolObjects, "toolObjects cannot contain null elements");
        assertToolAnnotatedMethodsPresent(toolObjects);
        this.toolObjects = toolObjects;
        validateToolCallbacks(getToolCallbacks());
    }

    public static MethodToolCallbackProviderCustom.Builder builder() {
        return new MethodToolCallbackProviderCustom.Builder();
    }

    private void assertToolAnnotatedMethodsPresent(List<Object> toolObjects) {

        for (Object toolObject : toolObjects) {
            List<Method> toolMethods = Stream
                    .of(ReflectionUtils.getDeclaredMethods(
                            AopUtils.isAopProxy(toolObject) ? AopUtils.getTargetClass(toolObject) : toolObject.getClass()))
                    .filter(this::isToolAnnotatedMethod)
                    .filter(toolMethod -> !isFunctionalType(toolMethod))
                    .toList();

            if (toolMethods.isEmpty()) {
                throw new IllegalStateException("No @Tool annotated methods found in " + toolObject + "."
                        + "Did you mean to pass a ToolCallback or ToolCallbackProvider? If so, you have to use .toolCallbacks() instead of .tool()");
            }
        }
    }

    @Override
    @NonNull
    public ToolCallback[] getToolCallbacks() {
        var toolCallbacks = this.toolObjects.stream()
                .map(toolObject -> Stream
                        .of(ReflectionUtils.getDeclaredMethods(
                                AopUtils.isAopProxy(toolObject) ? AopUtils.getTargetClass(toolObject) : toolObject.getClass()))
                        .filter(this::isToolAnnotatedMethod)
                        .filter(toolMethod -> !isFunctionalType(toolMethod))
                        .filter(ReflectionUtils.USER_DECLARED_METHODS::matches)
                        .map(toolMethod -> MethodToolCallback.builder()

                                // FIXME 此处重写了 toolDefinition 的生成，主要是为了动态化 description
                                .toolDefinition(builder(toolMethod).build())

                                .toolMetadata(ToolMetadata.from(toolMethod))
                                .toolMethod(toolMethod)
                                .toolObject(toolObject)
                                .toolCallResultConverter(ToolUtils.getToolCallResultConverter(toolMethod))
                                .build())
                        .toArray(ToolCallback[]::new))
                .flatMap(Stream::of)
                .toArray(ToolCallback[]::new);

        validateToolCallbacks(toolCallbacks);

        return toolCallbacks;
    }


    public static DefaultToolDefinition.Builder builder(Method method) {
        Assert.notNull(method, "method cannot be null");
        return DefaultToolDefinition.builder()
                .name(ToolUtils.getToolName(method))
                .description(getToolDescription(method))
                .inputSchema(JsonSchemaGenerator.generateForMethodInput(method));
    }


    public static String getToolDescription(Method method) {
        Assert.notNull(method, "method cannot be null");
        var tool = AnnotatedElementUtils.findMergedAnnotation(method, Tool.class);
        if (tool == null) {
            return ParsingUtils.reConcatenateCamelCase(method.getName(), " ");
        }

        String description = tool.description();
        if (!StringUtils.hasText(description)){
            return method.getName();
        }

        // 针对 executeSqlOnDefault 工具的描述进行动态化处理
        if (!"executeSqlOnDefault".equals(method.getName()) || !description.contains("%s")){
            return description;
        }

        // 获取默认数据源的数据库类型并替换占位符
        try {
            if (SpringContextHolder.isApplicationContextAvailable()) {
                DatabaseTypeResolver resolver = SpringContextHolder.getBean(DatabaseTypeResolver.class);
                String databaseType = resolver.getDefaultDatabaseType();
                return String.format(description, databaseType);
            }
        } catch (Exception e) {
            logger.warn("Failed to get database type for executeSqlOnDefault description: {}", e.getMessage());
        }
        
        // 如果获取失败，返回原始描述
        return description;
    }

    private boolean isFunctionalType(Method toolMethod) {
        var isFunction = ClassUtils.isAssignable(Function.class, toolMethod.getReturnType())
                || ClassUtils.isAssignable(Supplier.class, toolMethod.getReturnType())
                || ClassUtils.isAssignable(Consumer.class, toolMethod.getReturnType());

        if (isFunction) {
            logger.warn("Method {} is annotated with @Tool but returns a functional type. "
                    + "This is not supported and the method will be ignored.", toolMethod.getName());
        }

        return isFunction;
    }

    private boolean isToolAnnotatedMethod(Method method) {
        Tool annotation = AnnotationUtils.findAnnotation(method, Tool.class);
        return Objects.nonNull(annotation);
    }

    private void validateToolCallbacks(ToolCallback[] toolCallbacks) {
        List<String> duplicateToolNames = ToolUtils.getDuplicateToolNames(toolCallbacks);
        if (!duplicateToolNames.isEmpty()) {
            throw new IllegalStateException("Multiple tools with the same name (%s) found in sources: %s".formatted(
                    String.join(", ", duplicateToolNames),
                    this.toolObjects.stream().map(o -> o.getClass().getName()).collect(Collectors.joining(", "))));
        }
    }

    public static final class Builder {

        private List<Object> toolObjects;

        private Builder() {
        }

        public MethodToolCallbackProviderCustom.Builder toolObjects(Object... toolObjects) {
            Assert.notNull(toolObjects, "toolObjects cannot be null");
            this.toolObjects = Arrays.asList(toolObjects);
            return this;
        }

        public MethodToolCallbackProviderCustom build() {
            return new MethodToolCallbackProviderCustom(this.toolObjects);
        }

    }

}
