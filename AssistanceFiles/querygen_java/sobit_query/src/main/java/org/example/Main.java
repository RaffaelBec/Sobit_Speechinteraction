package org.example;

import java.sql.*;
import java.util.List;

public class Main {
    private static final String DB_URL = "jdbc:sqlite:C:/Users/benja/Downloads/test.db";

    public static void main(String[] args) {
        int employeeId = 1; // Simulated logged-in employee
        // Insert sample data with a future timestamp
        setupSampleData();

        // Load patients (clients) assigned for today
        PatientBuffer.loadPatientsForToday(employeeId);

        // Simulated NER output
        List<String[]> nerTaggedWords = List.of(
                new String[]{"welche", "O"},
                new String[]{"klienten", "B-Person"},
                new String[]{"habe", "O"},
                new String[]{"ich", "O"},
                new String[]{"gestern", "B-Time"},
                new String[]{"betreut", "B-Entity"}
        );

        // Process NER and generate SQL
        NERPreprocessor.ProcessedNER nerData = NERPreprocessor.processNER(nerTaggedWords,employeeId);

        // Debugging: Check if name extraction works
        System.out.println("\n=== DEBUG: Extracted Person ===");
        System.out.println("First Name: " + nerData.firstName);
        System.out.println("Last Name : " + nerData.lastName);
        System.out.println("Assigned  : " + nerData.isAssignedPatient);
        System.out.println("================================");

        String sqlQuery = SQLQueryBuilder.generateSQL(nerData,employeeId);
        System.out.println("\n=== Generated SQL Query ===\n" + sqlQuery);

        // Execute the generated SQL query and display results
        executeQuery(sqlQuery);
        displayResults(sqlQuery);
    }

    private static void setupSampleData() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {

            // Clear previous test data
            stmt.execute("DELETE FROM Client");
            stmt.execute("DELETE FROM Assignment");
            stmt.execute("DELETE FROM Person");
            stmt.execute("DELETE FROM PersonRelationship");

            // Insert sample clients
            stmt.execute("INSERT INTO Client (ClientId, FirstName, LastName) VALUES (1, 'Alice', 'Smith')");
            stmt.execute("INSERT INTO Client (ClientId, FirstName, LastName) VALUES (2, 'John', 'Doe')");

            // Insert assignments
            stmt.execute("INSERT INTO Assignment (AssignmentId, AssignmentTypeId, ClientId, EmployeeId, TargetTimeStart, TargetTimeEnd) " +
                    "VALUES (1, 1, 1, 1, DATETIME('now','+4 hour'), DATETIME('now','+5 hour'))");

            stmt.execute("INSERT INTO Assignment (AssignmentId, AssignmentTypeId, ClientId, EmployeeId, TargetTimeStart, TargetTimeEnd, ActualTimeStart) " +
                    "VALUES (2, 1, 2, 1, DATETIME('now','localtime'), DATETIME('now', '+5 hour'), DATETIME('now','-1 hour'))");

// ✅ Insert a test assignment for "gestern" (yesterday)
            stmt.execute("INSERT INTO Assignment (AssignmentId, AssignmentTypeId, ClientId, EmployeeId, TargetTimeStart, TargetTimeEnd) " +
                    "VALUES (3, 1, 3, 1, DATETIME('now', '-1 day', 'localtime'), DATETIME('now', '-1 day', '+5 hour'))");

            stmt.execute("INSERT INTO Assignment (AssignmentId, AssignmentTypeId, ClientId, EmployeeId, TargetTimeStart, TargetTimeEnd, ActualTimeStart) " +
                    "VALUES (4, 1, 4, 1, DATETIME('now', '-1 day', 'localtime'), DATETIME('now', '-1 day', '+5 hour'), DATETIME('now', '-1 day', '-1 hour'))");

            stmt.execute("INSERT INTO Person (PersonId, FirstName, LastName, PhoneNr) VALUES (3, 'Catharina', 'Gruber', '555-555-5555')");
            stmt.execute("INSERT INTO Person (PersonId, FirstName, LastName, PhoneNr) VALUES (4, 'Benjamin', 'Fiala', '111-222-3333')");
            stmt.execute("INSERT INTO Person (PersonId, FirstName, LastName, PhoneNr) VALUES (5, 'Lena', 'Fiala', '444-555-6666')");

            // Insert person relationships (if not already present)
            stmt.execute("INSERT INTO PersonRelationship (RelationshipId, RelationshipTypeId, ClientId, PersonId, DateStart) VALUES (3, 1, 1, 3, DATE('now'))");
            stmt.execute("INSERT INTO PersonRelationship (RelationshipId, RelationshipTypeId, ClientId, PersonId, DateStart) VALUES (4, 1, 2, 4, DATE('now'))");
            stmt.execute("INSERT INTO PersonRelationship (RelationshipId, RelationshipTypeId, ClientId, PersonId, DateStart) VALUES (5, 1, 1, 5, DATE('now'))");

            System.out.println("DEBUG: Sample data inserted successfully!");

        } catch (SQLException e) {
            System.err.println("ERROR: Could not insert sample data -> " + e.getMessage());
        }
    }

    private static void executeQuery(String sqlQuery) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlQuery);
            System.out.println("\n=== Query executed successfully! ===");
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to execute query -> " + e.getMessage());
        }
    }

    private static void displayResults(String sqlQuery) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlQuery)) {

            ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();

            System.out.println("\n=== Query Results ===");
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rsmd.getColumnName(i) + ": " + rs.getString(i) + "  ");
                }
                System.out.println();
            }
            System.out.println("====================");

        } catch (SQLException e) {
            System.err.println("ERROR: Failed to retrieve query results -> " + e.getMessage());
        }
    }
}
