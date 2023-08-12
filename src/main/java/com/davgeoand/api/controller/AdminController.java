package com.davgeoand.api.controller;

import com.davgeoand.api.exception.MissingPropertyException;
import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.monitor.metric.ServiceMetricRegistry;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.micrometer.core.instrument.Meter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.Manifest;

import static io.javalin.apibuilder.ApiBuilder.get;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AdminController {
    public static EndpointGroup getAdminEndpoints() {
        log.info("Returning api endpoints");
        return () -> {
            get("health", AdminController::health);
            get("jars", AdminController::jars);
            get("info", AdminController::info);
            get("metrics", AdminController::metrics);
        };
    }

    private static void metrics(Context context) {
        log.info("Starting admin metrics request");
        ArrayList<Meter> meterArrayList = new ArrayList<>();
        ServiceMetricRegistry.getMeterRegistry().forEachMeter((meterArrayList::add));
        context.json(meterArrayList);
        context.status(HttpStatus.OK);
        log.info("Finished admin metrics request");
    }

    private static void info(Context context) {
        log.info("Starting admin info request");
        context.json(ServiceProperties.getInfoPropertiesMap());
        context.status(HttpStatus.OK);
        log.info("Finished admin info request");
    }

    private static void jars(Context context) {
        log.info("Starting admin jars request");
        if (ServiceProperties.getProperty("service.env").orElseThrow(() -> new MissingPropertyException("service.env")).equals("local")) {
            context.json("Service is running locally. Not able to retrieve jars");
            context.status(HttpStatus.OK);
        } else {
            InputStream stream = AdminController.class.getResourceAsStream("/META-INF/MANIFEST.MF");
            try {
                Manifest manifest = new Manifest(stream);
                String jars = manifest.getMainAttributes().getValue("Class-Path");
                String[] classPathValues = jars.split(" ");
                ArrayList<String> jarsList = new ArrayList<>(Arrays.asList(classPathValues));
                context.json(jarsList);
                context.status(HttpStatus.OK);
            } catch (IOException e) {
                log.warn("Can't retrieve jars for service", e);
            }
        }
        log.info("Finished admin jars request");
    }

    private static void health(Context context) {
        log.info("Starting admin health request");
        context.status(HttpStatus.OK);
        log.info("Finished admin health request");
    }
}