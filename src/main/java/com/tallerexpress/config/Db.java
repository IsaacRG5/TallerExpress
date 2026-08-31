package com.tallerexpress.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Centraliza la conexión JDBC de TallerExpress. */
public final class Db {
    private Db() { }

    public static Connection getConnection() throws SQLException {
        String url = value("TE_DB_URL", "jdbc:postgresql://localhost:5432/tallerexpress");
        String user = value("TE_DB_USER", "postgres");
        String password = value("TE_DB_PASSWORD", "postgres");
        return DriverManager.getConnection(url, user, password);
    }

    private static String value(String name, String defaultValue) {
        String system = System.getProperty(name);
        if (system != null && !system.isBlank()) return system;
        String environment = System.getenv(name);
        if (environment != null && !environment.isBlank()) return environment;
        return defaultValue;
    }
}
