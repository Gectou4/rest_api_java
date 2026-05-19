package com.g4.api.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DB {

    private static final Map<String, HikariDataSource> DATA_SOURCES = new ConcurrentHashMap<>();
    private static final ThreadLocal<Connection> THREAD_CONNECTION = new ThreadLocal<>();

    private DB() {}

    public static synchronized Connection getInstance(String server) {
        Connection conn = THREAD_CONNECTION.get();
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    return conn;
                }
            } catch (SQLException e) {
                THREAD_CONNECTION.remove();
            }
        }

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
            conn = ds.getConnection();
            THREAD_CONNECTION.set(conn);
            return conn;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get DB connection: " + e.getMessage(), e);
        }
    }

    public static void releaseConnection() {
        Connection conn = THREAD_CONNECTION.get();
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // ignore
            }
            THREAD_CONNECTION.remove();
        }
    }

    public static void closeAll() {
        releaseConnection();
        for (HikariDataSource ds : DATA_SOURCES.values()) {
            if (!ds.isClosed()) {
                ds.close();
            }
        }
        DATA_SOURCES.clear();
    }
}
