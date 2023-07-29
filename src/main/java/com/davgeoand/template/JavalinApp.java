package com.davgeoand.template;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.event.EventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
public class JavalinApp {
    private final Javalin app;

    public JavalinApp() {
        log.info("Initializing javalin web app");
        ApplicationProperties.init("application.properties", "build.properties");
        this.app = Javalin.create(configJavalin())
                .events(configServerEvents());
        log.info("Successfully initialized javalin web app");
    }

    public void start() {
        log.info("Starting javalin web app");
        ApplicationProperties.getProperty("service.port").ifPresentOrElse(
                (port) -> {
                    app.start(Integer.parseInt(port));
                },
                () -> {
                    log.error("Missing port number for javalin web app");
                    System.exit(1);
                }
        );
        log.info("Successfully started javalin web app");
    }

    private Consumer<JavalinConfig> configJavalin() {
        return (javalinConfig -> {
        });
    }

    private Consumer<EventListener> configServerEvents() {
        return (eventListener -> {
            eventListener.serverStarted(() -> {
                log.info("Javalin web app started");
            });
        });
    }

}
