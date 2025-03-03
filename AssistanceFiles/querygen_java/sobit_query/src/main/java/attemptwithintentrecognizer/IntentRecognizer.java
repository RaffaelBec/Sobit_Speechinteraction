package attemptwithintentrecognizer;

import java.util.*;

public class IntentRecognizer {
    static int employeeId = 1;

    public static void main(String[] args) {
        List<String[]> nerTaggedWords = Arrays.asList(
                new String[]{"trage", "B-Action"},
                new String[]{"den", "O"},
                new String[]{"kilometer", "B-Entity"},
                new String[]{"##stand", "B-Entity"},
                new String[]{"66", "B-Distance"},
                new String[]{"##6", "B-Distance"},
                new String[]{"als", "O"},
                new String[]{"start", "B-Entity"},
                new String[]{"##stand", "B-Entity"},
                new String[]{"ein", "O"}
        );

        processIntent(nerTaggedWords);
    }

    public static void processIntent(List<String[]> nerTaggedWords) {
        String intent = null;
        String table = null;
        List<String> values = new ArrayList<>();
        Map<String, String> conditions = new HashMap<>();
        int personId = employeeId;
        String personType = "Employee";

        // === Step 1: Merge subwords ===
        for (int i = 1; i < nerTaggedWords.size(); i++) {
            String word = nerTaggedWords.get(i)[0];
            if (word.startsWith("##")) {
                String cleanWord = word.substring(2);
                nerTaggedWords.get(i - 1)[0] += cleanWord;
                nerTaggedWords.get(i)[0] = "";
            }
        }

        // === Step 2: Extract Intent and Entities ===
        for (String[] token : nerTaggedWords) {
            String word = token[0].toLowerCase();
            String tag = token[1];

            if (word.isEmpty()) continue; // Skip merged words

            if (tag.equals("B-Action")) {
                intent = KeyWordMapper.getMappedValue(word);
            } else if (tag.startsWith("B-Entity") || tag.startsWith("B-Distance")) {
                values.add(KeyWordMapper.getMappedValue(word));
            } else if (tag.startsWith("B-Time")) {
                conditions.put("Time", KeyWordMapper.getMappedValue(word));
            } else if (tag.startsWith("B-Person")) {
                personType = "Client"; // Assume person reference means Client
            }
        }

        // === Step 3: Identify the Table ===
        for (String value : values) {
            table = EntityTableMapper.getTableForEntity(value);
            if (table != null) break;
        }

        // === Step 4: Set Default Conditions ===
        if (table != null && (table.equals("DriversLog") || table.equals("Assignment"))) {
            conditions.put(personType + "Id", String.valueOf(personId));
        }

        // === Output Results ===
        System.out.println("Intent: " + intent);
        System.out.println("Table: " + table);
        System.out.println("Values: " + values);
        System.out.println("Conditions: " + conditions);
    }
}
