/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.hystrix.processor;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.apache.camel.Processor;
import org.apache.camel.model.HystrixConfigurationDefinition;
import org.apache.camel.model.HystrixDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.spi.ProcessorFactory;
import org.apache.camel.spi.RouteContext;
import org.apache.camel.util.CamelContextHelper;

/**
 * To integrate camel-hystrix with the Camel routes using the Hystrix EIP.
 */
public class HystrixProcessorFactory implements ProcessorFactory {

    @Override
    public Processor createChildProcessor(RouteContext routeContext, ProcessorDefinition<?> definition, boolean mandatory) throws Exception {
        // not in use
        return null;
    }

    @Override
    public Processor createProcessor(RouteContext routeContext, ProcessorDefinition<?> definition) throws Exception {
        if (definition instanceof HystrixDefinition) {
            HystrixDefinition cb = (HystrixDefinition) definition;
            String id = cb.idOrCreate(routeContext.getCamelContext().getNodeIdFactory());

            // create the regular processor
            Processor processor = cb.createChildProcessor(routeContext, true);
            Processor fallback = null;
            if (cb.getFallback() != null) {
                fallback = cb.getFallback().createProcessor(routeContext);
            }

            // create setter using the default options
            HystrixCommand.Setter setter = HystrixCommand.Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(id));
            HystrixCommandProperties.Setter command = HystrixCommandProperties.Setter();
            setter.andCommandPropertiesDefaults(command);
            HystrixThreadPoolProperties.Setter threadPool = HystrixThreadPoolProperties.Setter();
            setter.andThreadPoolPropertiesDefaults(threadPool);

            // at first configure any shared options
            if (cb.getHystrixConfigurationRef() != null) {
                HystrixConfigurationDefinition config = CamelContextHelper.mandatoryLookup(routeContext.getCamelContext(), cb.getHystrixConfigurationRef(), HystrixConfigurationDefinition.class);
                configureHystrix(command, threadPool, config);
            }
            // then any local configured can override
            if (cb.getHystrixConfiguration() != null) {
                HystrixConfigurationDefinition config = cb.getHystrixConfiguration();
                configureHystrix(command, threadPool, config);
            }

            return new HystrixProcessor(id, setter, processor, fallback);
        } else {
            return null;
        }
    }

    private void configureHystrix(HystrixCommandProperties.Setter command, HystrixThreadPoolProperties.Setter threadPool, HystrixConfigurationDefinition config) {
        // command
        if (config.getCircuitBreakerEnabled() != null) {
            command.withCircuitBreakerEnabled(config.getCircuitBreakerEnabled());
        }
        if (config.getCircuitBreakerErrorThresholdPercentage() != null) {
            command.withCircuitBreakerErrorThresholdPercentage(config.getCircuitBreakerErrorThresholdPercentage());
        }
        if (config.getCircuitBreakerForceClosed() != null) {
            command.withCircuitBreakerForceClosed(config.getCircuitBreakerForceClosed());
        }
        if (config.getCircuitBreakerForceOpen() != null) {
            command.withCircuitBreakerForceOpen(config.getCircuitBreakerForceOpen());
        }
        if (config.getCircuitBreakerRequestVolumeThreshold() != null) {
            command.withCircuitBreakerRequestVolumeThreshold(config.getCircuitBreakerRequestVolumeThreshold());
        }
        if (config.getCircuitBreakerSleepWindowInMilliseconds() != null) {
            command.withCircuitBreakerSleepWindowInMilliseconds(config.getCircuitBreakerSleepWindowInMilliseconds());
        }
        if (config.getExecutionIsolationSemaphoreMaxConcurrentRequests() != null) {
            command.withExecutionIsolationSemaphoreMaxConcurrentRequests(config.getExecutionIsolationSemaphoreMaxConcurrentRequests());
        }
        if (config.getExecutionIsolationStrategy() != null) {
            command.withExecutionIsolationStrategy(HystrixCommandProperties.ExecutionIsolationStrategy.valueOf(config.getExecutionIsolationStrategy()));
        }
        if (config.getExecutionIsolationThreadInterruptOnTimeout() != null) {
            command.withExecutionIsolationThreadInterruptOnTimeout(config.getExecutionIsolationThreadInterruptOnTimeout());
        }
        if (config.getExecutionTimeoutInMilliseconds() != null) {
            command.withExecutionTimeoutInMilliseconds(config.getExecutionTimeoutInMilliseconds());
        }
        if (config.getExecutionTimeoutEnabled() != null) {
            command.withExecutionTimeoutEnabled(config.getExecutionTimeoutEnabled());
        }
        if (config.getFallbackIsolationSemaphoreMaxConcurrentRequests() != null) {
            command.withFallbackIsolationSemaphoreMaxConcurrentRequests(config.getFallbackIsolationSemaphoreMaxConcurrentRequests());
        }
        if (config.getFallbackEnabled() != null) {
            command.withFallbackEnabled(config.getFallbackEnabled());
        }
        if (config.getMetricsHealthSnapshotIntervalInMilliseconds() != null) {
            command.withMetricsHealthSnapshotIntervalInMilliseconds(config.getMetricsHealthSnapshotIntervalInMilliseconds());
        }
        if (config.getMetricsRollingPercentileBucketSize() != null) {
            command.withMetricsRollingPercentileBucketSize(config.getMetricsRollingPercentileBucketSize());
        }
        if (config.getMetricsRollingPercentileEnabled() != null) {
            command.withMetricsRollingPercentileEnabled(config.getMetricsRollingPercentileEnabled());
        }
        if (config.getMetricsRollingPercentileWindowInMilliseconds() != null) {
            command.withMetricsRollingPercentileWindowInMilliseconds(config.getMetricsRollingPercentileWindowInMilliseconds());
        }
        if (config.getMetricsRollingPercentileWindowBuckets() != null) {
            command.withMetricsRollingPercentileWindowBuckets(config.getMetricsRollingPercentileWindowBuckets());
        }
        if (config.getMetricsRollingStatisticalWindowInMilliseconds() != null) {
            command.withMetricsRollingStatisticalWindowInMilliseconds(config.getMetricsRollingStatisticalWindowInMilliseconds());
        }
        if (config.getMetricsRollingStatisticalWindowBuckets() != null) {
            command.withMetricsRollingStatisticalWindowBuckets(config.getMetricsRollingStatisticalWindowBuckets());
        }
        if (config.getRequestCacheEnabled() != null) {
            command.withRequestCacheEnabled(config.getRequestCacheEnabled());
        }
        if (config.getRequestLogEnabled() != null) {
            command.withRequestLogEnabled(config.getRequestLogEnabled());
        }
        // thread pool
        if (config.getCorePoolSize() != null) {
            threadPool.withCoreSize(config.getCorePoolSize());
        }
        if (config.getKeepAliveTime() != null) {
            threadPool.withKeepAliveTimeMinutes(config.getKeepAliveTime());
        }
        if (config.getMaxQueueSize() != null) {
            threadPool.withMaxQueueSize(config.getMaxQueueSize());
        }
        if (config.getQueueSizeRejectionThreshold() != null) {
            threadPool.withQueueSizeRejectionThreshold(config.getQueueSizeRejectionThreshold());
        }
        if (config.getThreadPoolRollingNumberStatisticalWindowInMilliseconds() != null) {
            threadPool.withMetricsRollingStatisticalWindowInMilliseconds(config.getThreadPoolRollingNumberStatisticalWindowInMilliseconds());
        }
        if (config.getThreadPoolRollingNumberStatisticalWindowBuckets() != null) {
            threadPool.withMetricsRollingStatisticalWindowBuckets(config.getThreadPoolRollingNumberStatisticalWindowBuckets());
        }
    }
}