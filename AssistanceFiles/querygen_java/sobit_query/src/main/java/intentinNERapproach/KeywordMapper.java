package intentinNERapproach;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class KeywordMapper {

    private static final Map<String, String> timeMappings = new HashMap<>();
    private static final Map<String, String> actionMap = new HashMap<>();
    private static final Map<String, String> personMap = new HashMap<>();
    private static final Map<String, String> contactMap = new HashMap<>();
    static {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        cal.add(Calendar.DATE, 7);
        Date firstDateOfNextWeek = cal.getTime();
        // Time Mappings
        timeMappings.put("jetzige","now");
        timeMappings.put("jetzt","now");
        timeMappings.put("aktuell", "now");
        timeMappings.put("aktuelle", "now");
        timeMappings.put("aktuellen","now" );
        //timeMappings.put("morgen", "date('now', '+1 day')");
        //timeMappings.put("gestern", "date('now', '-1 day')");
        timeMappings.put("sofort", "datetime('now')");
        timeMappings.put("kommenden","next");
        timeMappings.put("anstehenden","next");
        timeMappings.put("nachsten", "next");
        timeMappings.put("nachste", "next");

        // Action Mappings
        actionMap.put("anrufen", "call");
        actionMap.put("rufe", "call");
        actionMap.put("anruf", "call");
        actionMap.put("rufen", "call");
        actionMap.put("beginne","start");
        actionMap.put("starte","start");
        actionMap.put("starten","start");
        actionMap.put("setzen","start");
        actionMap.put("beende","end");
        actionMap.put("finish","end");
        actionMap.put("ende","end");
        actionMap.put("beenden","end");

        // Person Mappings
        personMap.put("klienten", "CURRENT_PATIENT");
        personMap.put("klient", "CURRENT_PATIENT");
        personMap.put("klientin", "CURRENT_PATIENT");
        personMap.put("patienten", "CURRENT_PATIENT");
        personMap.put("bewohner", "CURRENT_PATIENT");
        personMap.put("pflegebedurftigen", "CURRENT_PATIENT");

        contactMap.put("verwandten", "relative|fm");
        contactMap.put("verwandter", "relative|fm");
        contactMap.put("bekannten", "relative|fr");
        contactMap.put("angehorigen", "relative|fm");
        contactMap.put("angehörigen", "relative|fm");
        // Specific relationship types (Keeps "relative" category)
        contactMap.put("geschwister", "relative|si");
        contactMap.put("bruder", "relative|si");
        contactMap.put("schwester", "relative|si");
        contactMap.put("mutter", "relative|mo");
        contactMap.put("vater", "relative|da");
        contactMap.put("ehemann", "relative|sp");
        contactMap.put("ehefrau", "relative|sp");
        contactMap.put("notfallkontakt", "relative|ec");

    }

    public static String mapTime(String timeExpression) {return timeMappings.getOrDefault(timeExpression.toLowerCase(), null);
    }

    public static String mapAction(String action) {
        return actionMap.getOrDefault(action.toLowerCase(), null);
    }

    public static String mapPerson(String person) {
        return personMap.getOrDefault(person.toLowerCase(), null);
    }
    public static String mapContact(String person) {
        return contactMap.getOrDefault(person.toLowerCase(), null);  // ✅ Return `null` if not a contact type
    }

}
