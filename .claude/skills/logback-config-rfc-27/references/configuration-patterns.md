# Logback Configuration Patterns

Detailed configuration patterns for RFC-27 compliant Logback setup.

## Contents

- [Complete Production Configuration](#complete-production-configuration)
- [AsyncAppender Configuration](#asyncappender-configuration)
- [File Appender with Rotation](#file-appender-with-rotation)
- [Conditional Logging](#conditional-logging)
- [MDC Integration](#mdc-integration)
- [Sensitive Data Masking](#sensitive-data-masking)

---
## Complete Production Configuration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration scan="true" scanPeriod="30 seconds">
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <!-- Properties from Spring environment -->
    <springProperty scope="context" name="serviceName" source="spring.application.name"/>
    <springProperty scope="context" name="environment" source="spring.profiles.active" defaultValue="local"/>
    
    <!-- Local development: human-readable console -->
    <springProfile name="local">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
        </root>
        
        <logger name="com.bitso" level="DEBUG"/>
        <logger name="org.springframework" level="INFO"/>
    </springProfile>
    
    <!-- Non-local: JSON for log aggregation -->
    <springProfile name="!local">
        <appender name="ASYNC_JSON" class="ch.qos.logback.classic.AsyncAppender">
            <queueSize>512</queueSize>
            <discardingThreshold>0</discardingThreshold>
            <appender-ref ref="JSON"/>
        </appender>
        
        <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <customFields>{"dd.service":"${serviceName}","dd.env":"${environment}"}</customFields>
                <includeMdcKeyName>traceId</includeMdcKeyName>
                <includeMdcKeyName>spanId</includeMdcKeyName>
                <includeMdcKeyName>userId</includeMdcKeyName>
                <includeMdcKeyName>requestId</includeMdcKeyName>
            </encoder>
        </appender>
        
        <root level="INFO">
            <appender-ref ref="ASYNC_JSON"/>
        </root>
        
        <!-- Reduce library noise -->
        <logger name="org.apache.kafka" level="WARN"/>
        <logger name="org.hibernate.SQL" level="WARN"/>
        <logger name="io.grpc" level="WARN"/>
    </springProfile>
</configuration>
```

## AsyncAppender Configuration

For high-throughput services, wrap appenders with AsyncAppender:

```xml
<appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
    <!-- Queue size (default 256) -->
    <queueSize>512</queueSize>
    
    <!-- 0 = never discard; positive = discard when queue is X% full -->
    <discardingThreshold>0</discardingThreshold>
    
    <!-- Include caller data (slower but useful for debugging) -->
    <includeCallerData>false</includeCallerData>
    
    <!-- Never block the application thread -->
    <neverBlock>true</neverBlock>
    
    <appender-ref ref="JSON"/>
</appender>
```

## File Appender with Rotation

For services that need file output (not recommended for containers):

```xml
<appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>logs/application.log</file>
    
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>logs/application.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>7</maxHistory>
        <totalSizeCap>1GB</totalSizeCap>
    </rollingPolicy>
    
    <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
</appender>
```

## Conditional Logging

Use Janino for conditional configuration:

```xml
<!-- Add to dependencies: org.codehaus.janino:janino -->
<if condition='property("ENABLE_DEBUG_LOGGING").equalsIgnoreCase("true")'>
    <then>
        <logger name="com.bitso" level="DEBUG"/>
    </then>
    <else>
        <logger name="com.bitso" level="INFO"/>
    </else>
</if>
```

## MDC Integration

Configure MDC keys for structured logging:

```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <!-- Explicitly include specific MDC keys -->
    <includeMdcKeyName>traceId</includeMdcKeyName>
    <includeMdcKeyName>spanId</includeMdcKeyName>
    <includeMdcKeyName>userId</includeMdcKeyName>
    <includeMdcKeyName>orderId</includeMdcKeyName>
    
    <!-- Or include all MDC keys -->
    <!-- <includeMdc>true</includeMdc> -->
</encoder>
```

## Sensitive Data Masking

Mask sensitive fields in logs:

```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <jsonGeneratorDecorator class="net.logstash.logback.mask.MaskingJsonGeneratorDecorator">
        <defaultMask>****</defaultMask>
        <path>password</path>
        <path>creditCard</path>
        <path>ssn</path>
    </jsonGeneratorDecorator>
</encoder>
```
<!-- AUTO-GENERATED FILE - DO NOT EDIT DIRECTLY -->
<!-- Source: bitsoex/ai-code-instructions → java/skills/logback-config-rfc-27/references/configuration-patterns.md -->
<!-- To modify, edit the source file and run the distribution workflow -->

