package com.g4.api.db;

public record DBConfig(String user, String pwd, String url) {

    private static final String DEFAULT_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : "root";
    private static final String DEFAULT_PWD = System.getenv("DB_PWD") != null ? System.getenv("DB_PWD") : "";
    private static final String DEFAULT_URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:mysql://localhost:3306/rest_api?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris";

    public static DBConfig getConfig(String key) {
        return switch (key) {
            case "master" -> new DBConfig(DEFAULT_USER, DEFAULT_PWD, DEFAULT_URL);
            default -> new DBConfig(DEFAULT_USER, DEFAULT_PWD, DEFAULT_URL);
        };
    }
}
