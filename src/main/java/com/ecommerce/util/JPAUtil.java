package com.ecommerce.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JPAUtil {
    private static final EntityManagerFactory EMF = buildFactory();

    private static EntityManagerFactory buildFactory() {
        Map<String, String> overrides = new HashMap<>();

        // Read from env — fall back to localhost for local dev without Docker
        String host = getEnv("DB_HOST", "localhost");
        String port = getEnv("DB_PORT", "1433");
        String db   = getEnv("DB_NAME", "ecommerce_db");
        String user = getEnv("DB_USER", "sa");
        String pass = getEnv("DB_PASSWORD", "Admin123!");

        overrides.put(
                "jakarta.persistence.jdbc.url",
                "jdbc:sqlserver://" + host + ":" + port
                        + ";databaseName=" + db
                        + ";encrypt=false;trustServerCertificate=true"
        );
        overrides.put("jakarta.persistence.jdbc.user",     user);
        overrides.put("jakarta.persistence.jdbc.password", pass);

        return Persistence.createEntityManagerFactory("ecommercePU", overrides);
    }

    private static String getEnv(String key, String fallback) {
        String val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : fallback;
    }

    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }
}