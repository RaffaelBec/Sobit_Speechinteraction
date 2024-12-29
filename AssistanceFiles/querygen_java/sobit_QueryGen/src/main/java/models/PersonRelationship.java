package models;

import java.time.LocalDate;

public class PersonRelationship {
    private int relationshipId;
    private int relationshipTypeId;
    private Integer clientId; // Nullable
    private int personId;
    private LocalDate dateStart;
    private LocalDate dateEnd;
    private boolean isEmergencyContact;
    private String comment;

    public int getRelationshipId() {
        return relationshipId;
    }

    public void setRelationshipId(int relationshipId) {
        this.relationshipId = relationshipId;
    }

    public int getRelationshipTypeId() {
        return relationshipTypeId;
    }

    public void setRelationshipTypeId(int relationshipTypeId) {
        this.relationshipTypeId = relationshipTypeId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public int getPersonId() {
        return personId;
    }

    public void setPersonId(int personId) {
        this.personId = personId;
    }

    public LocalDate getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDate dateStart) {
        this.dateStart = dateStart;
    }

    public LocalDate getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDate dateEnd) {
        this.dateEnd = dateEnd;
    }

    public boolean isEmergencyContact() {
        return isEmergencyContact;
    }

    public void setEmergencyContact(boolean emergencyContact) {
        isEmergencyContact = emergencyContact;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // Getters and Setters
    //... appropriate setters and getters for all fields
}
