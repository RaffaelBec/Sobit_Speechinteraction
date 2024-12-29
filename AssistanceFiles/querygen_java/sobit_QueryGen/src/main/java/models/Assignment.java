package models;

import java.time.LocalDate;

public class Assignment {
    private int assignmentId;
    private int assignmentTypeId;
    private int employeeId;
    private int clientId;
    private LocalDate date;
    private LocalDate targetTimeStart;
    private LocalDate targetTimeEnd;
    private LocalDate actualTimeStart;
    private LocalDate actualTimeEnd;
    private String cancelationInfo;
    private String note;

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getAssignmentTypeId() {
        return assignmentTypeId;
    }

    public void setAssignmentTypeId(int assignmentTypeId) {
        this.assignmentTypeId = assignmentTypeId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getTargetTimeStart() {
        return targetTimeStart;
    }

    public void setTargetTimeStart(LocalDate targetTimeStart) {
        this.targetTimeStart = targetTimeStart;
    }

    public LocalDate getTargetTimeEnd() {
        return targetTimeEnd;
    }

    public void setTargetTimeEnd(LocalDate targetTimeEnd) {
        this.targetTimeEnd = targetTimeEnd;
    }

    public LocalDate getActualTimeStart() {
        return actualTimeStart;
    }

    public void setActualTimeStart(LocalDate actualTimeStart) {
        this.actualTimeStart = actualTimeStart;
    }

    public LocalDate getActualTimeEnd() {
        return actualTimeEnd;
    }

    public void setActualTimeEnd(LocalDate actualTimeEnd) {
        this.actualTimeEnd = actualTimeEnd;
    }

    public String getCancelationInfo() {
        return cancelationInfo;
    }

    public void setCancelationInfo(String cancelationInfo) {
        this.cancelationInfo = cancelationInfo;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

// Getters and Setters
//... appropriate setters and getters for all fields
}
