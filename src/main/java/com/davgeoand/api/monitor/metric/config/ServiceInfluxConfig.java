package com.davgeoand.api.monitor.metric.config;

import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.exception.MissingPropertyException;
import io.micrometer.influx.InfluxConfig;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class ServiceInfluxConfig implements InfluxConfig {
    @Override
    public String get(@NotNull String s) {
        return null;
    }

    @Override
    public @NotNull String uri() {
        return ServiceProperties.getProperty("service.metric.influxdb.uri").orElseThrow(() -> new MissingPropertyException("service.metric.influxdb.uri"));
    }

    @Override
    public String org() {
        return ServiceProperties.getProperty("service.metric.influxdb.org").orElseThrow(() -> new MissingPropertyException("service.metric.influxdb.org"));
    }

    @Override
    public @NotNull String bucket() {
        return ServiceProperties.getProperty("service.metric.influxdb.bucket").orElseThrow(() -> new MissingPropertyException("service.metric.influxdb.bucket"));
    }

    @Override
    public String token() {
        return ServiceProperties.getProperty("service.metric.influxdb.token").orElseThrow(() -> new MissingPropertyException("service.metric.influxdb.token"));
    }

    @Override
    public @NotNull Duration step() {
        return Duration.ofSeconds(Integer.parseInt(ServiceProperties.getProperty("service.metric.influxdb.step").orElseThrow(() -> new MissingPropertyException("service.metric.influxdb.step"))));
    }
}
