package attemptwithintentrecognizer;

import java.util.HashMap;
import java.util.Map;

public class KeyWordMapper {
    private static final Map<String, String> keyWordMapper = new HashMap<>();

    static {
        // === Intent Mapping ===
        keyWordMapper.put("trage", "update");
        keyWordMapper.put("starte", "update");
        keyWordMapper.put("ändere", "update");
        keyWordMapper.put("setze", "update");
        keyWordMapper.put("aktualisiere", "update");

        keyWordMapper.put("füge", "insert");
        keyWordMapper.put("erstelle", "insert");
        keyWordMapper.put("hinzufügen", "insert");
        keyWordMapper.put("neue", "insert");

        keyWordMapper.put("zeige", "select");
        keyWordMapper.put("suche", "select");
        keyWordMapper.put("rufe", "select");
        keyWordMapper.put("wann", "select");
        keyWordMapper.put("wie", "select");
        keyWordMapper.put("erfassen","select");
        keyWordMapper.put("lösche", "delete");
        keyWordMapper.put("entferne", "delete");

        // === Synonyms and Keywords ===
        keyWordMapper.put("km", "MileageStart");
        keyWordMapper.put("auto", "CarId"); // Use CarId when referring to a specific car
        keyWordMapper.put("wagen", "CarId");
        keyWordMapper.put("kennzeichen", "LicensePlate");
        keyWordMapper.put("name", "FirstName");  // Default to first name
        keyWordMapper.put("kunde", "ClientId");
        keyWordMapper.put("mitarbeiter", "EmployeeId");
        keyWordMapper.put("einsatz", "AssignmentId");
        keyWordMapper.put("beziehung", "PersonRelationshipId");
        keyWordMapper.put("notiz", "Note");

        // === Entity to Column Mapping ===
        keyWordMapper.put("kilometerstand", "MileageStart");
        keyWordMapper.put("startstand", "MileageStart");
        keyWordMapper.put("endstand", "MileageEnd");  // Added mapping for end mileage
        keyWordMapper.put("servicetermin", "LastCheck");
        keyWordMapper.put("fahrzeug", "CarId");  // Maps correctly now

        // === Time-related Keywords ===
        keyWordMapper.put("startzeit", "TargetTimeStart");
        keyWordMapper.put("endzeit", "TargetTimeEnd");
        keyWordMapper.put("datum", "Date");
        keyWordMapper.put("heute", "LastCheck"); // If referring to a service check date
        keyWordMapper.put("jetzt", "ActualTimeStart");
        keyWordMapper.put("dauer", "Duration");
        keyWordMapper.put("vergangenen", "last");
        keyWordMapper.put("letzten", "last");
    }

    public static String getMappedValue(String word) {
        return keyWordMapper.getOrDefault(word.toLowerCase(), word);
    }
}
