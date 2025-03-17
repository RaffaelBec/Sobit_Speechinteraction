package intentinNERapproach;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String DB_URL = "jdbc:sqlite:C:/Users/benja/Downloads/test.db";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            System.out.println("Connected to database successfully!");

            // Clear and insert new test data
            setupDatabase(conn);

            // Sample test input for NER token processing
            List<String[]> nerTaggedWords = new ArrayList<>();
            nerTaggedWords.add(new String[]{"rufe", "B-Action"});
            nerTaggedWords.add(new String[]{"einen", "O"});
            nerTaggedWords.add(new String[]{"ange", "B-Person"});
            nerTaggedWords.add(new String[]{"##hor", "B-Person"});
            nerTaggedWords.add(new String[]{"##igen", "B-Person"});
            nerTaggedWords.add(new String[]{"von", "O"});
            nerTaggedWords.add(new String[]{"Frau", "B-Person"});
            nerTaggedWords.add(new String[]{"Smi", "I-Person"});
            nerTaggedWords.add(new String[]{"##th", "I-Person"});
            nerTaggedWords.add(new String[]{"an", "O"});

            // Process NER and extract client name
            Processor processor = new Processor(nerTaggedWords, "John", "Doe", "NAMEN_BEKOMMEN");
            System.out.println("Extracted First Name: " + processor.getResult().extractedFirstName);
            System.out.println("Extracted Last Name: " + processor.getResult().extractedLastName);

            // Initialize Buffer to get employee and client data
            Buffer buffer = new Buffer(conn, "John", "Doe");
            buffer.determineTargetClient(processor.getResult().extractedFirstName, processor.getResult().extractedLastName);

            Client targetClient = buffer.getTargetClient();
            Employee employee = buffer.getEmployee();

            System.out.println("\n### DEBUG INFO ###");
            System.out.println("Target Client: " + (targetClient != null ? targetClient.firstName + " " + targetClient.lastName + " (ID: " + targetClient.clientId + ")" : "None"));
            System.out.println("Employee: " + (employee != null ? employee.firstName + " " + employee.lastName + " (ID: " + employee.employeeId + ")" : "None"));

            // Check if we have valid employee and targetClient before running queries
            if (targetClient == null || employee == null) {
                System.out.println("ERROR: Missing target client or employee. Queries will not be executed.");
                return;
            }

            // Create QuerySelector and test queries
            QuerySelector querySelector = new QuerySelector(conn, targetClient, employee);
            querySelector.executeAndPrintQuery("NAME_BEKOMMEN");
            querySelector.executeAndPrintQuery("ALTER_BEKOMMEN");
            querySelector.executeAndPrintQuery("NÄCHSTER_EINSATZ_KLIENT_BEKOMMEN");
            querySelector.executeAndPrintQuery("BESUCHTE_KLIENTEN_HEUTE_BEKOMMEN");
            querySelector.executeAndPrintQuery("LETZTER_BETREUER_BEKOMMEN");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void setupDatabase(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // Clear existing data
            stmt.execute("DELETE FROM Client");
            stmt.execute("DELETE FROM Employee");
            stmt.execute("DELETE FROM Assignment");

            // Insert test Employee
            stmt.executeUpdate("INSERT INTO Employee (EmployeeId, FirstName, LastName, Title, SSN, Sex, Citizenship, Profession, BirthName, BirthPlace, BirthCountry) " +
                    "VALUES (1, 'John', 'Doe', 'Mr.', '123-45-6789', 'Male', 'USA', 'Engineer', 'John Doe', 'New York', 'USA')");

            // Insert test Client
            stmt.executeUpdate("INSERT INTO Client (ClientId, FirstName, LastName, Title, SSN, Sex, Citizenship, Profession, BirthName, BirthPlace, BirthCountry) " +
                    "VALUES (1, 'Jane', 'Smith', 'Ms.', '987-65-4321', 'Female', 'USA', 'Doctor', 'Jane Smith', 'Los Angeles', 'USA')");

            // Insert test Assignment
            stmt.executeUpdate("INSERT INTO Assignment (AssignmentId, AssignmentTypeId, EmployeeId, ClientId, Date) " +
                    "VALUES (1, 101, 1, 1, DATE('now'))");

            System.out.println("Database setup complete.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
