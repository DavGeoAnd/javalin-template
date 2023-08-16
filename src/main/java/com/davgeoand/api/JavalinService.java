package com.davgeoand.api;

import com.davgeoand.api.controller.AdminController;
import com.davgeoand.api.exception.MissingPropertyException;
import com.davgeoand.api.helper.ServiceProperties;
import com.davgeoand.api.monitor.event.handler.ServiceEventHandler;
import com.davgeoand.api.monitor.event.type.Audit;
import com.davgeoand.api.monitor.event.type.ServiceStart;
import com.davgeoand.api.monitor.metric.ServiceMetricRegistry;
import io.javalin.Javalin;
import io.javalin.http.HandlerType;
import io.javalin.http.HttpStatus;
import io.opentelemetry.api.trace.Span;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;

import static io.javalin.apibuilder.ApiBuilder.path;

@Slf4j
public class JavalinService {
    private final Javalin service;
    private long startServiceTime;

    public JavalinService() {
        log.info("Initializing javalin web service");
        service = Javalin.create();
        startingSteps();
        log.info("Successfully initialized javalin web service");
    }

    public void start() {
        log.info("Starting javalin web service");
        startServiceTime = System.currentTimeMillis();
        ServiceProperties.init("service.properties", "build.properties");
        ServiceMetricRegistry.init();
        ServiceEventHandler.init();
        service.start(Integer.parseInt(ServiceProperties.getProperty("service.port").orElseThrow(() -> new MissingPropertyException("service.port"))));
        log.info("Successfully started javalin web service");
    }

    private void startingSteps() {
        log.info("Setting up service starting steps");
        try {
            service.events((eventListener -> {
                eventListener.serverStarting(() -> {
                    serviceMetrics();
                    serviceEvents();
                    routes();
                });
            }));

        } catch (Exception e) {
            log.error("Issue during startup", e);
            System.exit(1);
        }
        log.info("Successfully set up service starting steps");
    }

    private void serviceMetrics() {
        log.info("Setting up service metrics");
        ServiceMetricRegistry.addMetric("service.event.queue.size", ServiceEventHandler.getQueueSize());
        service.updateConfig((javalinConfig -> {
            javalinConfig.plugins.register(ServiceMetricRegistry.getMicrometerPlugin());
        }));
        log.info("Successfully set up service metrics");
    }

    private void serviceEvents() {
        log.info("Setting up service events");
        eventServiceStart();
        eventAudit();
        log.info("Successfully set up service events");
    }

    private void eventServiceStart() {
        log.info("Setting up ServiceStart event");
        service.events((eventListener -> {
            eventListener.serverStarted(() -> {
                long serviceStartDuration = System.currentTimeMillis() - startServiceTime;
                try {
                    String buildVersion = ServiceProperties.getProperty("service.version").orElseThrow(() -> new MissingPropertyException("service.version"));
                    String gitBranch = ServiceProperties.getProperty("git.branch").orElseThrow(() -> new MissingPropertyException("git.branch"));
                    String gitCommitId = ServiceProperties.getProperty("git.commit.id.abbrev").orElseThrow(() -> new MissingPropertyException("git.commit.id.abbrev"));
                    String javaVersion = ServiceProperties.getProperty("process.runtime.version").orElseThrow(() -> new MissingPropertyException("process.runtime.version"));
                    long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
                    ServiceEventHandler.addEvent(ServiceStart.builder()
                            .serviceStartDuration(serviceStartDuration)
                            .buildVersion(buildVersion)
                            .gitBranch(gitBranch)
                            .gitCommitId(gitCommitId)
                            .javaVersion(javaVersion)
                            .startTime(startTime)
                            .build());
                } catch (MissingPropertyException missingPropertyException) {
                    log.error("Not able to add ServiceStart event", missingPropertyException);
                }
            });
        }));
        log.info("Successfully set up ServiceStart event");
    }

    private void eventAudit() {
        log.info("Setting up Audit event");
        service.updateConfig((javalinConfig -> {
            javalinConfig.requestLogger.http((ctx, ms) -> {
                String requestPath = ctx.endpointHandlerPath();
                HttpStatus httpStatus = ctx.status();
                HandlerType method = ctx.method();
                String response;
                if (ctx.status().getCode() < 400) {
                    response = "OK";
                } else {
                    response = ctx.result();
                }
                ServiceEventHandler.addEvent(Audit.builder()
                        .requestPath(requestPath)
                        .requestDuration(ms)
                        .traceId(Span.current().getSpanContext().getTraceId())
                        .response(response)
                        .method(method.toString())
                        .status(String.valueOf(httpStatus.getCode()))
                        .build());
            });
        }));
        log.info("Successfully set up Audit event");
    }

    private void routes() {
        log.info("Setting up routes");
        service.routes(() -> {
            path("admin", AdminController.getAdminEndpoints());
        });
        log.info("Successfully set up routes");
    }
}

