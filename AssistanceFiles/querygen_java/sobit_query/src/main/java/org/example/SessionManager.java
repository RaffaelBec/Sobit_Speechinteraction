package org.example;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static String currentEmployeeId = null;
    private static String currentPatientId = null;
    private static List<String> assignedClients = new ArrayList<>();

    public static String getCurrentEmployeeId() {
        return currentEmployeeId;
    }

    public static void setCurrentEmployeeId(String employeeId) {
        currentEmployeeId = employeeId;
    }

    public static String getCurrentPatientId() {
        return currentPatientId;
    }

    public static void setCurrentPatientId(String patientId) {
        currentPatientId = patientId;
    }

    public static List<String> getAssignedClients() {
        return new ArrayList<>(assignedClients);
    }

    public static void setAssignedClients(List<String> clients) {
        assignedClients = new ArrayList<>(clients);
    }
}
