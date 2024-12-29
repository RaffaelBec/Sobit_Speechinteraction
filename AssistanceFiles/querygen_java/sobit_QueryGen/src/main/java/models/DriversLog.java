package models;

import java.time.LocalDate;

public class DriversLog {
    private int driversLogId;
    private int carId;
    private int assignmentId;
    private int employeeId;
    private String addressFrom;
    private String addressTo;

    public int getDriversLogId() {
        return driversLogId;
    }

    public void setDriversLogId(int driversLogId) {
        this.driversLogId = driversLogId;
    }

    public int getCarId() {
        return carId;
    }

    public void setCarId(int carId) {
        this.carId = carId;
    }

    public int getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(int assignmentId) {
        this.assignmentId = assignmentId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getAddressFrom() {
        return addressFrom;
    }

    public void setAddressFrom(String addressFrom) {
        this.addressFrom = addressFrom;
    }

    public String getAddressTo() {
        return addressTo;
    }

    public void setAddressTo(String addressTo) {
        this.addressTo = addressTo;
    }

    public LocalDate getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDate departureTime) {
        this.departureTime = departureTime;
    }

    public LocalDate getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDate arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getMileageStart() {
        return mileageStart;
    }

    public void setMileageStart(int mileageStart) {
        this.mileageStart = mileageStart;
    }

    public int getMileageEnd() {
        return mileageEnd;
    }

    public void setMileageEnd(int mileageEnd) {
        this.mileageEnd = mileageEnd;
    }

    public double getAddedFuel() {
        return addedFuel;
    }

    public void setAddedFuel(double addedFuel) {
        this.addedFuel = addedFuel;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    private LocalDate departureTime;
    private LocalDate arrivalTime;
    private int mileageStart;
    private int mileageEnd;
    private double addedFuel;
    private String note;
    private boolean isPrivate;

    // Getters and Setters
    //... appropriate setters and getters for all fields
}

