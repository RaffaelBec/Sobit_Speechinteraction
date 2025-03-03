package attemptwithintentrecognizer;

import java.util.HashMap;
import java.util.Map;

public class EntityTableMapper {
    private static final Map<String, String> entityToTableMap = new HashMap<>();

    static {
        // Car-related entities
        entityToTableMap.put("kilometerstand", "DriversLog");
        entityToTableMap.put("startstand", "DriversLog");
        entityToTableMap.put("mileage", "DriversLog");
        entityToTableMap.put("car", "Car");
        entityToTableMap.put("licenseplate", "Car");
        entityToTableMap.put("lastcheck", "Car");

        // Employee-related entities
        entityToTableMap.put("employee", "Employee");
        entityToTableMap.put("firstname", "Employee");
        entityToTableMap.put("lastname", "Employee");
        entityToTableMap.put("profession", "Employee");

        // Client-related entities
        entityToTableMap.put("client", "Client");
        entityToTableMap.put("ssn", "Client");
        entityToTableMap.put("citizenship", "Client");

        // Assignment-related entities
        entityToTableMap.put("einsatz", "Assignment");
        entityToTableMap.put("note", "Assignment");
        entityToTableMap.put("targettimestart", "Assignment");
        entityToTableMap.put("targettimeend", "Assignment");
        entityToTableMap.put("actualtimestart", "Assignment");
        entityToTableMap.put("actualtimeend", "Assignment");

        // Person-related entities
        entityToTableMap.put("phone", "Person");
        entityToTableMap.put("relationship", "PersonRelationship");
    }

    public static String getTableForEntity(String entity) {
        return entityToTableMap.getOrDefault(entity.toLowerCase(), null);
    }
}
