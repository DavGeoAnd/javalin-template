package com.davgeoand.api.monitor.event.handler;

import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.exception.MissingPropertyException;
import com.davgeoand.api.monitor.event.type.Event;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.write.Point;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class InfluxEventHandler implements EventHandler {
    private final WriteApiBlocking writeApiBlocking;
    private final Map<String, String> commonTagMap;

    public InfluxEventHandler() {
        log.info("Initializing InfluxDB ServiceEventHandler");
        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(
                ServiceProperties.getProperty("service.event.influxdb.url").orElseThrow(() -> new MissingPropertyException("service.event.influxdb.url")),
                ServiceProperties.getProperty("service.event.influxdb.token").orElseThrow(() -> new MissingPropertyException("service.event.influxdb.token")).toCharArray(),
                ServiceProperties.getProperty("service.event.influxdb.org").orElseThrow(() -> new MissingPropertyException("service.event.influxdb.org")),
                ServiceProperties.getProperty("service.event.influxdb.bucket").orElseThrow(() -> new MissingPropertyException("service.event.influxdb.bucket"))
        );
        log.info("Sending events to InfluxDB version: " + influxDBClient.version());
        writeApiBlocking = influxDBClient.getWriteApiBlocking();
        commonTagMap = ServiceProperties.getCommonAttributesMap();
        log.info("Successfully initialized InfluxDB ServiceEventHandler");
    }

    @Override
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try {
            while (true) {
                Event event = eventBlockingQueue.take();
                Point eventPoint = event.toPoint();
                eventPoint.addTags(commonTagMap);
                writeApiBlocking.writePoint(eventPoint);
            }
        } catch (Exception e) {
            log.warn("Issue processing event", e);
        }
    }
}