package org.example;

import java.util.List;
import java.util.StringJoiner;

public class NERPreprocessor {

    private static boolean isTitle(String word) {
        return word.equalsIgnoreCase("herr") || word.equalsIgnoreCase("frau" ) || word.equalsIgnoreCase("herrn");
    }

    public static class ProcessedNER {
        public String action;
        public String person;
        public String personType;
        public String relationType="FM";
        public String firstName;
        public String lastName;
        public String time;
        public String entity;
        public String task;
        public String distance;
        public boolean isAssignedPatient;
    }

    public static ProcessedNER processNER(List<String[]> nerTaggedWords, int employeeId) {
        ProcessedNER result = new ProcessedNER();
        result.action = "SELECT";
        result.time = "datetime('now')";

        StringJoiner firstNameBuilder = new StringJoiner(" ");
        StringJoiner lastNameBuilder = new StringJoiner(" ");
        StringJoiner entityBuilder = new StringJoiner(" ");
        StringJoiner builder = new StringJoiner(" ");
        StringJoiner distanceBuilder = new StringJoiner(" ");
        boolean processingLastName = false;
        boolean personDetected = false;
        boolean timeRegistered = false;
        // Load assigned patients
        PatientBuffer.loadPatientsForToday(employeeId);

        System.out.println("\n=== DEBUG: Processing NER Tokens ===");

        // Step 1: Merge subword tokens (##)
        for (int i = 1; i < nerTaggedWords.size(); i++) { // Start from index 1
            String word = nerTaggedWords.get(i)[0];

            // If it's a subword token, merge it with the previous word
            if (word.startsWith("##")) {
                String cleanWord = word.substring(2); // Remove ##
                String[] prevEntry = nerTaggedWords.get(i - 1); // Reference to previous word array

                // Modify the previous word directly in its array
                prevEntry[0] = prevEntry[0] + cleanWord;

                // Set current word to empty, mark it as processed
                nerTaggedWords.get(i)[0] = "";

                // Check if the next word is also a subword (##), continue merging if necessary
                while (i + 1 < nerTaggedWords.size() && nerTaggedWords.get(i + 1)[0].startsWith("##")) {
                    i++; // Move to the next word to continue merging
                    cleanWord = nerTaggedWords.get(i)[0].substring(2); // Remove ##
                    prevEntry[0] = prevEntry[0] + cleanWord; // Merge it with the previous word
                    nerTaggedWords.get(i)[0] = ""; // Mark it as processed
                }
            }
        }


        String currentTag = null;

        // Step 2: Process merged tokens while skipping empty placeholders
        for (String[] token : nerTaggedWords) {
            String word = token[0];
            String tag = token[1];

            // Skip special tokens or empty placeholders
            if (word.equals("[CLS]") || word.equals("[SEP]") || word.isEmpty()) continue;

            System.out.println("Token: " + word + " | Tag: " + tag);

            switch (tag) {
                case "B-Action":
                    result.action = KeywordMapper.mapAction(word).toLowerCase();
                    break;
                case "B-Person":
                    System.out.println("→ Processing new PERSON token: " + word);

                    // Check if it's a title ("Herr" or "Frau"), then treat the next word as the last name
                    if (isTitle(word)) {
                        processingLastName = true;
                        lastNameBuilder = new StringJoiner(" ");
                        System.out.println("→ Title detected: " + word + " (Expecting Last Name next)");
                    }
                    // Check if it's a known person reference (e.g., "klienten" -> "CURRENT_PATIENT")
                    else if (KeywordMapper.mapPerson(word) != null) {
                        result.personType = KeywordMapper.mapPerson(word);
                        System.out.println("→ Identified as a special person type: " + result.personType);
                    }
                    // Check if it's a contact type (relative, sibling, etc.), assign it to `personType`
                    else if (KeywordMapper.mapContact(word) != null) {
                        result.relationType = KeywordMapper.mapContact(word);
                        System.out.println("→ Identified as a contact type: " + result.relationType + " (Not a Name)");
                    }
                    // Otherwise, assume it's a first name
                    else {
                        processingLastName = false;
                        firstNameBuilder = new StringJoiner(" ");
                        firstNameBuilder.add(word);
                        System.out.println("→ First Name detected: " + word);
                    }

                    personDetected = true;
                    break;


                case "I-Person":
                    // If we already detected a first name, treat the next I-Person as a last name
                    if (!firstNameBuilder.toString().isEmpty() && lastNameBuilder.toString().isEmpty()) {
                        lastNameBuilder.add(word);
                        System.out.println("→ Adding to Last Name: " + word);
                    } else {
                        firstNameBuilder.add(word);
                        System.out.println("→ Adding to First Name: " + word);
                    }

                    break;

                  // Flag to track if a valid time has been set

                case "B-Time":
                    if (word.equalsIgnoreCase("wann")) {
                        System.out.println("→ Ignored 'wann' as B-Time");
                        break;  // Ignore "wann" and move on
                    }

                    if (!timeRegistered) {  // Only register the first valid B-Time
                        String mappedTime = KeywordMapper.mapTime(word);
                        if (mappedTime != null) {
                            result.time = mappedTime;
                            timeRegistered = true;  // Lock further changes
                            System.out.println("→ Time registered: " + result.time);
                        }
                    } else {
                        System.out.println("→ Ignored extra B-Time: " + word);
                    }
                    break;

                case "I-Time":
                    if (timeRegistered) {  // Append I-Time only if a B-Time was set
                        result.time += " " + word;
                        System.out.println("→ Appended I-Time: " + word);
                    }
                    break;

                case "B-Entity":
                case "I-Entity":
                    entityBuilder.add(word);
                    break;
                case "B-Task":
                case "I-Task":
                    builder.add(word);
                    break;
                case "B-Distance":
                case "I-Distance":
                    distanceBuilder.add(word);
                    break;
            }
        }

        // Resolve detected names
        String detectedFirstName = firstNameBuilder.length() > 0 ? firstNameBuilder.toString() : null;
        String detectedLastName = lastNameBuilder.length() > 0 ? lastNameBuilder.toString() : null;
        String fullName = (detectedFirstName != null ? detectedFirstName : "") +
                (detectedLastName != null ? " " + detectedLastName : "");

        System.out.println("→ Detected First Name: " + detectedFirstName);
        System.out.println("→ Detected Last Name: " + detectedLastName);
        System.out.println("→ Full Name Built: " + fullName);

        if (personDetected && detectedFirstName != null) {
            result.isAssignedPatient = PatientBuffer.isAssignedPatient(fullName);
            System.out.println("→ Is Assigned Patient: " + result.isAssignedPatient);

            result.person = fullName;
        } else {
            result.person = "CURRENT_EMPLOYEE";
        }

        result.firstName = detectedFirstName;
        result.lastName = detectedLastName;
        result.entity = entityBuilder.length() > 0 ? entityBuilder.toString() : null;
        result.task = builder.length() > 0 ? builder.toString() : null;
        result.distance = distanceBuilder.length() > 0 ? distanceBuilder.toString() : null;

        System.out.println("====================================");

        return result;
    }
}
