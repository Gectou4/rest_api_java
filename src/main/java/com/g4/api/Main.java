package com.g4.api;

import com.g4.api.db.DB;
import java.net.URI;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class Main {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port =
                Integer.parseInt(
                        System.getenv().getOrDefault("PORT", String.valueOf(DEFAULT_PORT)));
        String baseUri = "http://0.0.0.0:" + port + "/";

        ResourceConfig config = new ResourceConfig();
        config.packages("com.g4.api.controller");
        config.register(ApiApplication.class);

        DB.getInstance("master");

        var server = JdkHttpServerFactory.createHttpServer(URI.create(baseUri), config);

        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    try {
                                        server.stop(0);
                                        DB.closeAll();
                                    } catch (Exception e) {
                                        System.err.println(
                                                "Error during shutdown: " + e.getMessage());
                                    }
                                }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Server interrupted: " + e.getMessage());
        }
    }
}
