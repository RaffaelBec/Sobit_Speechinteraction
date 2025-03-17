package intentinNERapproach;

import org.example.KeywordMapper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Buffer {
    private Connection connection;
    private Employee employee;
    private List<Client> todayClients;
    private List<Assignment> todayAssignments;
    private Client currentClient;
    private Assignment currentAssignment;
    private Client targetClient;
    public Buffer(Connection connection, String firstname, String lastname) {
        this.connection = connection;
        this.employee = getEmployee(firstname, lastname);
        if (this.employee != null) {
            this.todayClients = getTodaysClients(this.employee.employeeId);
            this.todayAssignments = getTodaysAssignments(this.employee.employeeId);
            this.currentClient = getCurrentClient(this.employee.employeeId);
            this.currentAssignment = getCurrentAssignment(this.employee.employeeId);
        } else {
            this.todayClients = new ArrayList<>();
            this.todayAssignments = new ArrayList<>();
            this.currentClient = null;
            this.currentAssignment = null;
        }
    }

    public Employee getEmployee(String firstname, String lastname) {
        String query = "SELECT * FROM Employee WHERE FirstName = ? AND LastName = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, firstname);
            stmt.setString(2, lastname);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Employee(
                        rs.getInt("EmployeeId"), rs.getString("FirstName"), rs.getString("LastName"),
                        rs.getString("Title"), rs.getDate("DateOfBirth"), rs.getString("SSN"),
                        rs.getString("Sex"), rs.getString("Citizenship"), rs.getString("Profession"),
                        rs.getString("BirthName"), rs.getString("BirthPlace"), rs.getString("BirthCountry")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Client> getTodaysClients(int employeeId) {
        List<Client> clients = new ArrayList<>();
        String query = "SELECT c.* FROM Assignment a " +
                "JOIN Client c ON a.ClientId = c.ClientId " +
                "WHERE a.EmployeeId = ? AND a.Date = DATE('now')";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                clients.add(new Client(
                        rs.getInt("ClientId"), rs.getString("FirstName"), rs.getString("LastName"),
                        rs.getString("Title"), rs.getDate("DateOfBirth"), rs.getString("SSN"),
                        rs.getString("Sex"), rs.getString("Citizenship"), rs.getString("Profession"),
                        rs.getString("BirthName"), rs.getString("BirthPlace"), rs.getString("BirthCountry")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clients;
    }

    public List<Assignment> getTodaysAssignments(int employeeId) {
        List<Assignment> assignments = new ArrayList<>();
        String query = "SELECT * FROM Assignment WHERE EmployeeId = ? AND Date = DATE('now')";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                // Retrieve date as a String to avoid parsing issues
                String dateString = rs.getString("Date");
                Date date = (dateString != null) ? Date.valueOf(dateString) : null; // Convert if not null

                assignments.add(new Assignment(
                        rs.getInt("AssignmentId"), rs.getInt("AssignmentTypeId"), rs.getInt("EmployeeId"),
                        rs.getInt("ClientId"), date, rs.getTimestamp("TargetTimeStart"),
                        rs.getTimestamp("TargetTimeEnd"), rs.getTimestamp("ActualTimeStart"),
                        rs.getTimestamp("ActualTimeEnd"), rs.getString("CancelationInfo"), rs.getString("Note")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assignments;
    }


    public Assignment getCurrentAssignment(int employeeId) {
        String query = "SELECT * FROM Assignment WHERE EmployeeId = ? AND ActualTimeStart IS NOT NULL AND ActualTimeEnd IS NULL ORDER BY ActualTimeStart DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Assignment(
                        rs.getInt("AssignmentId"), rs.getInt("AssignmentTypeId"), rs.getInt("EmployeeId"),
                        rs.getInt("ClientId"), rs.getDate("Date"), rs.getTimestamp("TargetTimeStart"),
                        rs.getTimestamp("TargetTimeEnd"), rs.getTimestamp("ActualTimeStart"),
                        rs.getTimestamp("ActualTimeEnd"), rs.getString("CancelationInfo"), rs.getString("Note")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Client getCurrentClient(int employeeId) {
        String query = "SELECT c.* FROM Assignment a JOIN Client c ON a.ClientId = c.ClientId WHERE a.EmployeeId = ? AND a.ActualTimeStart IS NOT NULL AND a.ActualTimeEnd IS NULL ORDER BY a.ActualTimeStart DESC LIMIT 1";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Client(
                        rs.getInt("ClientId"), rs.getString("FirstName"), rs.getString("LastName"),
                        rs.getString("Title"), rs.getDate("DateOfBirth"), rs.getString("SSN"),
                        rs.getString("Sex"), rs.getString("Citizenship"), rs.getString("Profession"),
                        rs.getString("BirthName"), rs.getString("BirthPlace"), rs.getString("BirthCountry")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void determineTargetClient(String firstName, String lastName) {
        System.out.println("\n### DEBUG: Running determineTargetClient ###");
        System.out.println("Checking for client with name: " + firstName + " " + lastName);

        if (lastName.isEmpty()) {
            System.out.println("No last name extracted. Assigning currentClient as targetClient.");
            targetClient = currentClient;
            return;
        }

        for (Client client : todayClients) {
            System.out.println("Comparing with: " + client.firstName + " " + client.lastName);
            if (client.firstName.equalsIgnoreCase(lastName) || client.lastName.equalsIgnoreCase(lastName)
                    ||client.firstName.equalsIgnoreCase(firstName)||client.lastName.equalsIgnoreCase(firstName)) {  // Ignore first name if missing
                targetClient = client;
                System.out.println("Target client found: " + targetClient.firstName + " " + targetClient.lastName);
                return;
            }
        }

        if (currentClient != null) {
            System.out.println("No match found in todayClients. Using currentClient as targetClient.");
            targetClient = currentClient;
        } else {
            System.out.println("No match found. Target client remains NULL.");
        }
    }


    public Client getTargetClient() {
        return targetClient;
    }
    public Employee getEmployee() {
        return employee;
    }

    public List<Client> getTodayClients() {
        return todayClients;
    }

    public List<Assignment> getTodayAssignments() {
        return todayAssignments;
    }

    public Client getCurrentClient() {
        return currentClient;
    }

    public Assignment getCurrentAssignment() {
        return currentAssignment;
    }
}
