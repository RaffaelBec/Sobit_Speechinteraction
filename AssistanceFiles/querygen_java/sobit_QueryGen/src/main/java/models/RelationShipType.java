package models;

public class RelationShipType {
    private int relationshipTypeId;
    private String value;
    private String shortValue;

    // Getters and Setters
    public int getRelationshipTypeId() {
        return relationshipTypeId;
    }

    public void setRelationshipTypeId(int relationshipTypeId) {
        this.relationshipTypeId = relationshipTypeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getShortValue() {
        return shortValue;
    }

    public void setShortValue(String shortValue) {
        this.shortValue = shortValue;
    }

}
