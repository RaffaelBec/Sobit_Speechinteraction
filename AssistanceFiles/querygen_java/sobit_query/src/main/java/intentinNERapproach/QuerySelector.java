package intentinNERapproach;

import java.sql.*;

public class QuerySelector {
    private Connection connection;
    private Client targetClient;
    private Employee employee;

    public QuerySelector(Connection connection, Client targetClient, Employee employee) {
        this.connection = connection;
        this.targetClient = targetClient;
        this.employee = employee;
    }

    public String getQuery(String queryType) {
        if (targetClient == null || employee == null) {
            return null; // No valid target client or employee, return null query
        }

        switch (queryType) {
            case "UNBEKANNT":
                return null;
            case "NAME_BEKOMMEN":
                return "SELECT FirstName, LastName FROM Client WHERE ClientId = " + targetClient.clientId;
            case "ALTER_BEKOMMEN":
                return "SELECT strftime('%Y', 'now') - strftime('%Y', DateOfBirth) AS Age FROM Client WHERE ClientId = " + targetClient.clientId;
            case "GEBURTSDATUM_BEKOMMEN":
                return "SELECT DateOfBirth FROM Client WHERE ClientId = " + targetClient.clientId;
            case "SOZIALVERSICHERUNGSNUMMER_BEKOMMEN":
                return "SELECT SSN FROM Client WHERE ClientId = " + targetClient.clientId;
            case "GESCHLECHT_BEKOMMEN":
                return "SELECT Sex FROM Client WHERE ClientId = " + targetClient.clientId;
            case "TELEFONNUMMER_BEKOMMEN":
                return "SELECT PhoneNr FROM Person WHERE PersonId = " + targetClient.clientId;
            case "JETZTIGER_KLIENT_BEKOMMEN":
                return "SELECT FirstName, LastName FROM Client WHERE ClientId = " + targetClient.clientId;
            case "NÄCHSTER_EINSATZ_KLIENT_BEKOMMEN":
                return "SELECT FirstName, LastName FROM Client WHERE ClientId = (SELECT ClientId FROM Assignment WHERE EmployeeId = " + employee.employeeId + " AND Date > DATE('now') ORDER BY Date ASC LIMIT 1)";
            case "NÄCHSTER_EINSATZ_UHRZEIT_BEKOMMEN":
                return "SELECT TargetTimeStart, TargetTimeEnd FROM Assignment WHERE EmployeeId = " + employee.employeeId + " AND Date > DATE('now') ORDER BY Date ASC LIMIT 1";
            case "LETZTER_EINSATZ_KLIENT_BEKOMMEN":
                return "SELECT FirstName, LastName FROM Client WHERE ClientId = (SELECT ClientId FROM Assignment WHERE EmployeeId = " + employee.employeeId + " AND Date < DATE('now') ORDER BY Date DESC LIMIT 1)";
            case "LETZTER_EINSATZ_UHRZEIT_BEKOMMEN":
                return "SELECT TargetTimeStart, TargetTimeEnd FROM Assignment WHERE EmployeeId = " + employee.employeeId + " AND Date < DATE('now') ORDER BY Date DESC LIMIT 1";
            case "JETZTIGER_EINSATZ_ENDE_BEKOMMEN":
                return "SELECT TargetTimeEnd FROM Assignment WHERE EmployeeId = " + employee.employeeId + " AND ActualTimeStart IS NOT NULL AND ActualTimeEnd IS NULL ORDER BY ActualTimeStart DESC LIMIT 1";
            case "EINSATZ_VON_KLIENT_BEKOMMEN":
                return "SELECT TargetTimeStart, TargetTimeEnd FROM Assignment WHERE ClientId = " + targetClient.clientId + " ORDER BY Date DESC LIMIT 1";
            case "ANZAHL_EINSAETZE_HEUTE_BEKOMMEN":
                return "SELECT COUNT(*) AS TotalAssignments FROM Assignment WHERE EmployeeId = " + employee.employeeId + " AND Date = DATE('now')";
            case "BESUCHTE_KLIENTEN_HEUTE_BEKOMMEN":
                return "SELECT COUNT(DISTINCT ClientId) AS VisitedClients FROM Assignment WHERE EmployeeId = " + employee.employeeId + " AND Date = DATE('now')";
            case "ANZAHL_EINSAETZE_HEUTE_UEBRIG_BEKOMMEN":
                return "SELECT COUNT(*) AS RemainingAssignments FROM Assignment WHERE EmployeeId = " + employee.employeeId + " AND Date = DATE('now') AND ActualTimeEnd IS NULL";
            default:
                return "";
        }
    }

    public void executeAndPrintQuery(String queryType) {
        String query = getQuery(queryType);
        System.out.println("\nExecuting Query: " + queryType);
        System.out.println("SQL: " + query);

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            boolean hasResults = false;

            while (rs.next()) {
                hasResults = true;
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(metaData.getColumnName(i) + ": " + rs.getString(i) + " | ");
                }
                System.out.println();
            }
            if (!hasResults) {
                System.out.println("No results found for query: " + queryType);
            }
        } catch (SQLException e) {
            System.out.println("Error executing query: " + e.getMessage());
        }
    }
}
