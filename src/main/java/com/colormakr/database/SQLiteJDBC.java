package com.colormakr.database;

import java.sql.Connection;
import java.sql.DriverManager;

public class SQLiteJDBC {
    public static Connection connection() {
        Connection conn;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\schal\\IdeaProjects\\Colormakr\\src\\main\\java\\com\\colormakr\\database\\colors.db");
            return conn;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
