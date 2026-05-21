package com.g4.api.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record DBConfig(String user, String pwd, String url) {

    private static final Logger log = LoggerFactory.getLogger(DBConfig.class);

    private static final String DEFAULT_USER;
    private static final String DEFAULT_PWD;
    private static final String DEFAULT_URL;

    static {
        Properties props = new Properties();
        try (InputStream is = DBConfig.class.getResourceAsStream("/application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            log.warn("Could not load application.properties: {}", e.getMessage(), e);
        }

        DEFAULT_USER = System.getenv("DB_USER") != null ? System.getenv("DB_USER") :
                props.getProperty("db.user", "root");
        DEFAULT_PWD = System.getenv("DB_PWD") != null ? System.getenv("DB_PWD") :
                props.getProperty("db.pwd", "");
        DEFAULT_URL = System.getenv("DB_URL") != null ? System.getenv("DB_URL") :
                props.getProperty("db.url",
                        "jdbc:mysql://localhost:3306/rest_api?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris");
    }

    public static DBConfig getConfig(String key) {
        return switch (key) {
            case "master" -> new DBConfig(DEFAULT_USER, DEFAULT_PWD, DEFAULT_URL);
            default -> new DBConfig(DEFAULT_USER, DEFAULT_PWD, DEFAULT_URL);
        };
    }
}
