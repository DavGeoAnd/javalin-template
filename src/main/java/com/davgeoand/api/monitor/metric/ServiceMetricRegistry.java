package com.davgeoand.api.monitor.metric;

import com.davgeoand.api.exception.MissingPropertyException;
import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.monitor.metric.config.ServiceInfluxConfig;
import io.javalin.micrometer.MicrometerPlugin;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.jvm.*;
import io.micrometer.core.instrument.binder.logging.LogbackMetrics;
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.influx.InfluxMeterRegistry;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceMetricRegistry {
    @Getter
    private static MeterRegistry meterRegistry;

    public static void init() {
        String metricHandlerType = ServiceProperties.getProperty("service.metric.type").orElseThrow(() -> new MissingPropertyException("service.metric.type"));
        switch (metricHandlerType) {
            case "global" -> meterRegistry = Metrics.globalRegistry;
            case "simple" -> meterRegistry = new SimpleMeterRegistry();
            case "influx" -> {
                meterRegistry = new InfluxMeterRegistry(new ServiceInfluxConfig(), Clock.SYSTEM);
                meterRegistry.config().commonTags(ServiceProperties.getCommonAttributeTags());
            }
            default -> {
                log.error("Invalid Meter Registry type. Defaulting to Global");
                meterRegistry = Metrics.globalRegistry;
            }
        }

        new ClassLoaderMetrics().bindTo(meterRegistry);
        new JvmCompilationMetrics().bindTo(meterRegistry);
        new JvmGcMetrics().bindTo(meterRegistry);
        new JvmHeapPressureMetrics().bindTo(meterRegistry);
        new JvmInfoMetrics().bindTo(meterRegistry);
        new JvmMemoryMetrics().bindTo(meterRegistry);
        new JvmThreadMetrics().bindTo(meterRegistry);
        new LogbackMetrics().bindTo(meterRegistry);
        new FileDescriptorMetrics().bindTo(meterRegistry);
        new ProcessorMetrics().bindTo(meterRegistry);
        new UptimeMetrics().bindTo(meterRegistry);
    }

    public static MicrometerPlugin getMicrometerPlugin() {
        return MicrometerPlugin.Companion.create(micrometerConfig -> micrometerConfig.registry = meterRegistry);
    }

    public static void addMetric(String name, int value) {
        meterRegistry.gauge(name, value);
    }

}