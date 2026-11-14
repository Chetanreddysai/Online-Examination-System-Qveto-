package com.qveto.exam.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * JDBC Connection Provider loading externalized properties from db.properties.
 *
 * Implements prepared connection validation and connectivity diagnostics
 * so the application can operate reliably or report status clearly.
 */
public final class DBConnection {

    private static final Properties properties = new Properties();
    private static boolean propertiesLoaded = false;
    private static Boolean cachedConnectionStatus = null;

    static {
        loadProperties();
    }

    private DBConnection() {}

    private static void loadProperties() {
        InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties");
        if (input == null) {
            File fallback = new File("src/main/resources/db.properties");
            if (fallback.exists()) {
                try {
                    input = new FileInputStream(fallback);
                } catch (Exception ignored) {}
            }
        }

        if (input != null) {
            try (InputStream is = input) {
                properties.load(is);
                propertiesLoaded = true;
                String driver = properties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                try {
                    Class.forName(driver);
                } catch (ClassNotFoundException e) {
                    System.err.println("[DBConnection] MySQL driver class not found: " + driver);
                }
            } catch (Exception e) {
                System.err.println("[DBConnection] Error loading db.properties: " + e.getMessage());
            }
        } else {
            System.err.println("[DBConnection] db.properties not found; using built-in defaults.");
            properties.setProperty("db.url", "jdbc:mysql://localhost:3306/online_exam_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
            properties.setProperty("db.user", "root");
            properties.setProperty("db.password", "root");
        }
    }

    /**
     * Obtains a new JDBC connection to the MySQL database.
     *
     * @return active java.sql.Connection
     * @throws SQLException if connection fails
     */
    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String pass = properties.getProperty("db.password");

        // Set login timeout to 3 seconds for responsive checks
        DriverManager.setLoginTimeout(3);
        return DriverManager.getConnection(url, user, pass);
    }

    /**
     * Checks whether MySQL is currently reachable.
     */
    public static synchronized boolean isDatabaseConnected() {
        try (Connection conn = getConnection()) {
            cachedConnectionStatus = (conn != null && !conn.isClosed());
            return cachedConnectionStatus;
        } catch (Exception e) {
            cachedConnectionStatus = false;
            return false;
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}
