package org.example;

import java.sql.*;
import java.util.*;

public class DatabaseQueryExecutor {
    private static final String DB_URL = "jdbc:sqlite:C:/Users/benja/Downloads/test.db";

    /**
     * Executes a SQL query (SELECT) and returns the results as a List of Maps.
     * Each map represents a row, with column names as keys and values as objects.
     *
     * @param query SQL SELECT query to execute
     * @return List of rows, where each row is a Map<String, Object>
     */
    public static List<Map<String, Object>> executeQuery(String query) {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
        } catch (SQLException e) {
            System.out.println("Database query error: " + e.getMessage());
        }

        return results;
    }

    /**
     * Executes an UPDATE, INSERT, or DELETE SQL statement.
     *
     * @param query SQL statement to execute
     * @return Number of affected rows
     */
    public static int executeUpdate(String query) {
        int affectedRows = 0;

        try (Connection con = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = con.prepareStatement(query)) {
            affectedRows = stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Database update error: " + e.getMessage());
        }

        return affectedRows;
    }
}
