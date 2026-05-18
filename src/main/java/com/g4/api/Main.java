package com.g4.api;

import com.g4.api.db.DB;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;

public class Main {

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", String.valueOf(DEFAULT_PORT)));
        String baseUri = "http://0.0.0.0:" + port + "/";

        ResourceConfig config = new ResourceConfig();
        config.packages("com.g4.api.controller");
        config.register(ApiApplication.class);

        DB.getInstance("master");

        var server = JettyHttpContainerFactory.createServer(URI.create(baseUri), config);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
                DB.closeAll();
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        }));

        try {
            server.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Server interrupted: " + e.getMessage());
        }
    }
}
