package com.g4.api.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DB {

    private static final Map<String, HikariDataSource> DATA_SOURCES = new ConcurrentHashMap<>();

    private DB() {
    }

    public static synchronized Connection getInstance(String server) {
        HikariDataSource ds =
                DATA_SOURCES.computeIfAbsent(
                        server,
                        key -> {
                            DBConfig config = DBConfig.getConfig(key);
                            HikariConfig hikari = new HikariConfig();
                            hikari.setJdbcUrl(config.url());
                            hikari.setUsername(config.user());
                            hikari.setPassword(config.pwd());
                            hikari.setMaximumPoolSize(10);
                            hikari.setMinimumIdle(2);
                            hikari.setConnectionTimeout(30000);
                            return new HikariDataSource(hikari);
                        });
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get DB connection: " + e.getMessage(), e);
        }
    }

    public static void closeAll() {
        for (HikariDataSource ds : DATA_SOURCES.values()) {
            if (!ds.isClosed()) {
                ds.close();
            }
        }
        DATA_SOURCES.clear();
    }
}
