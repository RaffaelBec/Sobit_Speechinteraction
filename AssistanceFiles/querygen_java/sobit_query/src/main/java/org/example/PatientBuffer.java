package org.example;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class PatientBuffer {
    private static final String DB_URL = "jdbc:sqlite:C:/Users/benja/Downloads/test.db";
    private static final Set<String> assignedClients = new HashSet<>();

    // Load assigned clients (patients) for today
    public static void loadPatientsForToday(int empId) {
        String sql = "SELECT * FROM Client " +
                "JOIN Assignment ON Client.ClientId = Assignment.ClientId " +
                "WHERE DATE(Assignment.Date) = DATE('now');" +
                "AND Assigment.EmployeeId = "+empId;

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            assignedClients.clear();  // Reset the buffer
            while (rs.next()) {
                String fullName = (rs.getString("FirstName") + " " + rs.getString("LastName")).toLowerCase();
                assignedClients.add(fullName);
            }

            System.out.println("DEBUG: Loaded assigned clients -> " + assignedClients);

        } catch (SQLException e) {
            System.err.println("ERROR: Unable to load clients. " + e.getMessage());
        }
    }

    // Check if a client (patient) is assigned
    public static boolean isAssignedPatient(String fullName) {
        if (fullName == null) return false;
        return assignedClients.contains(fullName.toLowerCase());
    }
}
