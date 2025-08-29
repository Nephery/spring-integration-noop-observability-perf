package com.solace.performance;

import static org.junit.jupiter.api.Assertions.assertFalse;

import io.micrometer.observation.ObservationRegistry;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.integration.channel.DirectChannel;

/**
 * Tests Spring Integration performance by directly sending messages to a DirectChannel
 * with a No-Op MessageHandler, simulating a message producer scenario.
 * When using the optimized profile, this test demonstrates the performance improvement
 * from bypassing observability overhead when NOOP registry is configured.
 */
class DirectPerformanceTest {

  private static final Duration WARMUP_DURATION = Duration.ofMinutes(1);
  private static final Duration TEST_DURATION = Duration.ofMinutes(10);
  private static final String TEST_PAYLOAD = "performance-test-message";

  private TestMessageProducer springProducer;
  private DirectChannel outputChannel;

  @BeforeEach
  void setUp() {
    // Create output channel with no-op handler
    this.outputChannel = new DirectChannel();
    NoOpMessageHandler handler = new NoOpMessageHandler();
    outputChannel.subscribe(handler);

    // Setup producer with NOOP observability registry
    this.springProducer = new TestMessageProducer();
    setupProducer(springProducer);
  }

  @Test
  void measureSpringIntegrationPerformance() {
    assertFalse(springProducer.isObserved(),
        "Producer should be configured with NOOP ObservationRegistry");

    System.out.println("=== Spring Integration Performance Analysis ===");
    System.out.println("Testing Spring Integration with NOOP observability registry");

    // Check if we're using optimized version by looking at classpath
    String integrationVersion = "unknown";
    boolean isOptimized = false;
    try {
      Package pkg = org.springframework.integration.endpoint.MessageProducerSupport.class.getPackage();
      if (pkg != null && pkg.getImplementationVersion() != null) {
        integrationVersion = pkg.getImplementationVersion();
        isOptimized = integrationVersion.contains("TEST-SNAPSHOT");
      }
    } catch (Exception e) {
      // Fallback - assume we're not optimized if we can't detect
    }

    System.out.printf("Spring Integration Version: %s%n", integrationVersion);
    System.out.printf("Using Optimized Build: %s%n", isOptimized ? "YES" : "NO");
    System.out.printf("Test Duration: %s after %s warmup%n", TEST_DURATION, WARMUP_DURATION);
    System.out.println();

    // Measure performance with NOOP registry
    long throughput = measureProducerThroughput(springProducer,
        "Spring Integration with NOOP observability");

    System.out.println();
    System.out.println("PERFORMANCE RESULTS:");
    System.out.println("===================");
    System.out.printf("Spring Integration Throughput: %,d msg/s%n", throughput);

    if (isOptimized) {
      System.out.println("Using OPTIMIZED Spring Integration with observability bypass");
      System.out.println("NOOP registry detection should eliminate observability overhead");
    } else {
      System.out.println("Using STANDARD Spring Integration");
      System.out.println("Run with -Poptimized profile to test optimized version");
    }
  }

  private long measureProducerThroughput(TestMessageProducer producer, String description) {
    System.out.printf("Measuring %s...%n", description);

    // Warmup
    warmupProducer(producer);

    // Reset and measure
    producer.resetCount();

    Instant start = Instant.now();
    long messageCount = 0;

    while (Duration.between(start, Instant.now()).compareTo(TEST_DURATION) < 0) {
      producer.sendTestMessage(TEST_PAYLOAD);
      messageCount++;
    }

    Duration actualDuration = Duration.between(start, Instant.now());
    long throughput = messageCount * 1000 / actualDuration.toMillis();

    System.out.printf("  %s: %,d msg/s%n",
                     description.substring(0, 1).toUpperCase() + description.substring(1),
                     throughput);
    return throughput;
  }

  private void setupProducer(TestMessageProducer producer) {
    producer.setOutputChannel(outputChannel);

    // is already default, but being explicit for illustrative purposes...
    producer.registerObservationRegistry(ObservationRegistry.NOOP);

    producer.afterPropertiesSet();
    producer.start();
  }

  private void warmupProducer(TestMessageProducer producer) {
    Instant warmupStart = Instant.now();
    while (Duration.between(warmupStart, Instant.now()).compareTo(WARMUP_DURATION) < 0) {
      producer.sendTestMessage(TEST_PAYLOAD);
    }
  }
}