package com.colormakr.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class JDBC {
    public static Connection connection() {
        Connection conn;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://192.168.178.92:5432/colors", "postgres", "iwidss4m");
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
