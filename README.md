# lsd-junit5

[![Build](https://github.com/lsd-consulting/lsd-junit5/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/lsd-consulting/lsd-junit5/actions/workflows/ci.yml)
[![Nightly Build](https://github.com/lsd-consulting/lsd-junit5/actions/workflows/nightly.yml/badge.svg)](https://github.com/lsd-consulting/lsd-junit5/actions/workflows/nightly.yml)
[![GitHub release](https://img.shields.io/github/release/lsd-consulting/lsd-junit5)](https://github.com/lsd-consulting/lsd-junit5/releases)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.lsd-consulting/lsd-junit5.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.lsd-consulting%22%20AND%20a:%22lsd-junit5%22)

> [!WARNING]
> **DEPRECATED**: This library has been superseded by [lsd-junit-jupiter](https://github.com/lsd-consulting/lsd-junit-jupiter). This repository will continue to support JUnit 5.x but new projects should use lsd-junit-jupiter for JUnit Jupiter (6.x+) support.

JUnit 5 extension for [lsd-core](https://github.com/lsd-consulting/lsd-core) - automatically generates living sequence diagrams from your tests.

## Quick Start

### Installation

Add the dependency to your project:

<details>
  <summary>Maven</summary>

```xml
<dependency>
    <groupId>io.github.lsd-consulting</groupId>
    <artifactId>lsd-junit5</artifactId>
    <version>X.X.X</version>
    <scope>test</scope>
</dependency>
```

</details>

<details>
  <summary>Gradle</summary>

```groovy
testImplementation 'io.github.lsd-consulting:lsd-junit5:X.X.X'
```
</details>

### Usage

Add `@ExtendWith(LsdExtension.class)` to your test class:

<details open>
  <summary>Kotlin Example</summary>

```kotlin
@ExtendWith(LsdExtension::class)
class PaymentServiceTest {
    ...
}
```
</details>

<details>
  <summary>Java Example</summary>

```java
@ExtendWith(LsdExtension.class)
class PaymentServiceTest {
    ... 
}
```
</details>

The extension:
- Hooks into JUnit 5 lifecycle to generate reports
- Creates a new scenario for each `@Test` method
- Generates sequence diagrams showing captured interactions
- Marks scenarios as passed/failed based on test results
- Outputs reports to `build/reports/lsd/` (configurable via `lsd.core.report.outputDir`)

### Working with lsd-core directly

You can capture events manually within tests:

<details open>
  <summary>Kotlin Example</summary>

```kotlin
@ExtendWith(LsdExtension::class)
class OrderProcessingTest {
    private val lsd = LsdContext.instance
    
    @Test
    fun `should process order`() {
        // Manually capture interactions
        lsd.capture(
            ("Customer" messages "OrderService") { label("POST /orders") }
        )
        
        // Your test assertions
        
        lsd.capture(
            ("OrderService" respondsTo "Customer") { label("201 Created") }
        )
    }
}
```
</details>

<details>
  <summary>Java Example</summary>

```java
@ExtendWith(LsdExtension.class)
class OrderProcessingTest {
    private final LsdContext lsd = LsdContext.getInstance();
    
    @Test
    void shouldProcessOrder() {
        // Manually capture interactions
        lsd.capture(
            messageBuilder()
                .from("Customer")
                .to("OrderService")
                .label("POST /orders")
                .build()
        );
        
        // Your test assertions
        
        lsd.capture(
            messageBuilder()
                .from("OrderService")
                .to("Customer")
                .label("201 Created")
                .type(SYNCHRONOUS_RESPONSE)
                .build()
        );
    }
}
```
</details>

See [lsd-core](https://github.com/lsd-consulting/lsd-core) documentation for full API details.

## Configuration

Configure via `lsd.properties` on your classpath. See [lsd-core configuration](https://github.com/lsd-consulting/lsd-core#configuration) for core properties.

### Extension-specific properties

| Property | Default | Description |
|----------|---------|-------------|
| `lsd.junit5.hideStacktrace` | `false` | Hide stacktraces in report popups for aborted/failed tests. Useful for approval testing where stack trace line numbers vary between builds. |

## Ecosystem

- **[lsd-core](https://github.com/lsd-consulting/lsd-core)** - Core library for generating living sequence diagrams
- **[lsd-interceptors](https://github.com/lsd-consulting/lsd-interceptors)** - HTTP/messaging interceptors for automatic capture
- **[lsd-cucumber](https://github.com/lsd-consulting/lsd-cucumber)** - Cucumber plugin for LSD reports


