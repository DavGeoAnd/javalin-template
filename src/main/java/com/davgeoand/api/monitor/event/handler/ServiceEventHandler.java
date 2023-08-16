package com.davgeoand.api.monitor.event.handler;


import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.exception.MissingPropertyException;
import com.davgeoand.api.monitor.event.type.Event;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceEventHandler {

    static EventHandler eventHandler;

    public static void init() {
        String eventHandlerType = ServiceProperties.getProperty("service.event.type").orElseThrow(() -> new MissingPropertyException("service.event.type"));
        switch (eventHandlerType) {
            case "log" -> eventHandler = new LogEventHandler();
            case "influx" -> eventHandler = new InfluxEventHandler();
            default -> {
                log.error("Invalid service event handler type. Defaulting to LogEventHandler");
                eventHandler = new LogEventHandler();
            }
        }
        Thread eventHandlerThread = new Thread(eventHandler);
        eventHandlerThread.setName("ServiceEventHandler");
        eventHandlerThread.start();
        log.info("Started ServiceEventHandler thread");
    }

    public static void addEvent(Event event) {
        event.setTime(Instant.now());
        eventHandler.addEventToQueue(event);
    }

    public static int getQueueSize(){
        return eventHandler.queueSize();
    }
}
