
package com.homebanking.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Enable @Async for event listeners

 * Annotations needed:
 * @ EnableAsync on @Configuration
 * @ Async on listener methods

 * Result: Event listeners ejecutan en thread pool
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Default configuration uses SimpleAsyncTaskExecutor
    // Para producción: custom ThreadPoolTaskExecutor
}

