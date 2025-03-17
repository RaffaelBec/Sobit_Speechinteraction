package attemptwithintentrecognizer;

import java.util.*;

public class IntentRecognizer {
    static int employeeId = 1;

    public static void main(String[] args) {
        List<String[]> nerTaggedWords = Arrays.asList(
                new String[]{"Vergangenen", "B-Time"},
                new String[]{"Servicetermin", "B-Entity"},
                new String[]{"für", "O"},
                new String[]{"das", "O"},
                new String[]{"Fahrzeug", "B-Entity"},
                new String[]{"erfassen", "B-Action"},
                new String[]{":", "O"},
                new String[]{"heute", "B-Time"}
        );

        processIntent(nerTaggedWords);
    }

    public static void processIntent(List<String[]> nerTaggedWords) {
        String intent = null;
        String table = null;
        Map<String, String> values = new HashMap<>();
        Map<String, String> conditions = new HashMap<>();
        int personId = employeeId;
        String personType = "Employee";
        List<String> entities = new ArrayList<>();
        List<String> times = new ArrayList<>();

        // === Step 1: Merge subwords ===
        for (int i = 1; i < nerTaggedWords.size(); i++) {
            String word = nerTaggedWords.get(i)[0];
            if (word.startsWith("##")) {
                String cleanWord = word.substring(2);
                nerTaggedWords.get(i - 1)[0] += cleanWord;
                nerTaggedWords.get(i)[0] = "";
            }
        }

        // === Step 2: Extract Intent, Entities, and Time Keywords ===
        for (String[] token : nerTaggedWords) {
            String word = token[0].toLowerCase();
            String tag = token[1];

            if (word.isEmpty()) continue; // Skip merged words

            if (tag.equals("B-Action") && !word.equals(":")) {  // Ignore colon
                intent = KeyWordMapper.getMappedValue(word);
            } else if (tag.equals("B-Entity")) {
                entities.add(KeyWordMapper.getMappedValue(word));
            } else if (tag.equals("B-Time")) {
                times.add(KeyWordMapper.getMappedValue(word));
            }
        }

        // === Step 3: Assign values correctly ===
        if (!entities.isEmpty() && !times.isEmpty()) {
            for (int i = 0; i < entities.size(); i++) {
                String entity = entities.get(i);
                String time = (i < times.size()) ? times.get(i) : times.get(times.size() - 1);
                values.put(entity, time);
            }
        }

        // === Step 4: Identify the Table ===
        for (String entity : entities) {
            table = EntityTableMapper.getTableForEntity(entity);
            if (table != null) break;
        }

        // === Step 5: Default Conditions (for Employee/Assignment cases) ===
        if (table != null && (table.equals("DriversLog") || table.equals("Assignment"))) {
            conditions.put(personType + "Id", String.valueOf(personId));
        }

        // === Step 6: Ensure Intent and Table Validity ===
        if (intent == null) {
            intent = "unknown"; // Fallback intent if none detected
        }
        if (table == null && !entities.isEmpty()) {
            table = "UnknownTable";
        }

        // === Step 7: Output Results (for SQL query generation) ===
        System.out.println("Intent: " + intent);
        System.out.println("Table: " + table);
        System.out.println("Values: " + values);
        System.out.println("Conditions: " + conditions);
    }
}
