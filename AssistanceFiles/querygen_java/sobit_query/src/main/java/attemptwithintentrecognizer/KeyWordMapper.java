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

        // === Synonyms and Keywords ===
        keyWordMapper.put("km", "kilometerstand");
        keyWordMapper.put("auto", "fahrzeug");
        keyWordMapper.put("wagen", "fahrzeug");
        keyWordMapper.put("kennzeichen", "licenseplate");
        keyWordMapper.put("name", "person");
        keyWordMapper.put("kunde", "client");
        keyWordMapper.put("mitarbeiter", "employee");
        keyWordMapper.put("einsatz", "assignment");
        keyWordMapper.put("fahrzeug", "car");
        keyWordMapper.put("beziehung", "personrelationship");
        keyWordMapper.put("notiz", "note");

        // === Time-related Keywords ===
        keyWordMapper.put("startzeit", "TargetTimeStart");
        keyWordMapper.put("endzeit", "TargetTimeEnd");
        keyWordMapper.put("datum", "Date");
        keyWordMapper.put("heute", "Date");
        keyWordMapper.put("jetzt", "ActualTimeStart");
        keyWordMapper.put("dauer", "duration");
    }

    public static String getMappedValue(String word) {
        return keyWordMapper.getOrDefault(word.toLowerCase(), word);
    }
}
