package com.g4.api;

import com.g4.api.db.DB;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.Executors;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        Properties props = new Properties();
        try (InputStream is = Main.class.getResourceAsStream("/application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            log.warn("Could not load application.properties: {}", e.getMessage(), e);
        }

        int port =
                Integer.parseInt(
                        System.getenv().getOrDefault("PORT",
                                props.getProperty("server.port", String.valueOf(DEFAULT_PORT))));
        String baseUri = "http://0.0.0.0:" + port + "/";

        ResourceConfig config = new ResourceConfig();
        config.packages("com.g4.api.controller");
        config.register(ApiApplication.class);

        DB.getInstance("master");

        HttpServer server = JdkHttpServerFactory.createHttpServer(URI.create(baseUri), config);
        server.setExecutor(Executors.newFixedThreadPool(10));

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    try {
                                        server.stop(0);
                                        DB.closeAll();
                                    } catch (Exception e) {
                                        log.error("Error during shutdown: {}", e.getMessage(), e);
                                    }
                                }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Server interrupted: {}", e.getMessage(), e);
        }
    }
}
