# Spring Integration NOOP Observability Performance Test

Performance test project demonstrating a notable performance improvement in Spring Integration messaging when NOOP observability overhead is eliminated.

## Project Overview

This project validates a performance optimization for Spring Integration's `MessageProducerSupport.sendMessage()` method. Currently, observability infrastructure is invoked even when `ObservationRegistry.NOOP` is configured, creating unnecessary overhead.

**Our fork implementation** adds a simple check:
```java
if (this.observationRegistry.isNoop()) {
    this.messagingTemplate.send(getRequiredOutputChannel(), trackMessageIfAny(message));  // Direct send
} else {
    // Existing observability path
    IntegrationObservation.HANDLER.observation(
                this.observationConvention,
                DefaultMessageReceiverObservationConvention.INSTANCE,
                () -> new MessageReceiverContext(message, getComponentName(), "message-producer"),
                this.observationRegistry)
            .observe(() -> this.messagingTemplate.send(getRequiredOutputChannel(), trackMessageIfAny(message)));
}
```

## Performance Results

**Latest Results** (10-minute test duration with 1-minute warmup):

| Configuration | Throughput | Improvement |
|---------------|------------|-------------|
| **Standard Spring Integration 6.5.1** | 6,579,301 msg/s | - |
| **Optimized (observability bypass)** | 7,793,131 msg/s | **+18.4%** |

**Performance Gain**: **1,213,830 additional messages per second**

## Setup

First, you need to locally install the Spring Integration fork with the optimization:

```bash
# Clone the fork (as submodule)
git submodule update --init --recursive

# Build and install locally
cd spring-integration-fork
./gradlew publishToMavenLocal
```

This installs `spring-integration-core:6.5.1.TEST-SNAPSHOT` to your local Maven repository.

## Running the Test

The project includes a single, focused performance test that demonstrates the observability overhead:

```bash
# Test standard Spring Integration 6.5.1
mvn test -Dtest=DirectPerformanceTest

# Test optimized version (requires fork setup below)  
mvn test -Dtest=DirectPerformanceTest -Poptimized
```

**Test Configuration**: 1-minute warmup + 10-minute (long-ish times for more statistical reliability)