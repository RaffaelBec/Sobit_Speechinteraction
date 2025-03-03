package org.example;

import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class NERPreprocessor {

    private static boolean isTitle(String word) {
        return word.equalsIgnoreCase("herr") || word.equalsIgnoreCase("frau") || word.equalsIgnoreCase("herrn");
    }

    public static class ProcessedNER {
        public String action;
        public String person;
        public String personType;
        public String relationType = "relative|FM";
        public String firstName;
        public String lastName;
        public String time;
        public String entity;
        public String task;
        public String distance;
        public int clientId;
        public boolean isAssignedPatient;
    }


    public static Integer getOngoingAssignmentClientId(int employeeId) {
        String query = "SELECT ClientId " +
                "FROM Assignment " +
                "WHERE EmployeeId = " + employeeId + " " +
                "AND ActualTimeStart IS NOT NULL " +
                "AND ActualTimeStart <= DATETIME('now','localtime') " +
                "AND ActualTimeEnd IS NULL " +
                "ORDER BY ActualTimeStart DESC " +
                "LIMIT 1";

        List<Map<String, Object>> results = DatabaseQueryExecutor.executeQuery(query);

        if (!results.isEmpty()) {
            return (Integer) results.get(0).get("ClientId");
        }

        return null; // No ongoing assignment found
    }

    public static ProcessedNER processNER(List<String[]> nerTaggedWords, int employeeId) {
        ProcessedNER result = new ProcessedNER();
        result.action = "SELECT";
        result.time = "now";

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
                    } else {
                        // First, check if it's a known special person type (CURRENT_PATIENT, etc.)
                        String mappedPerson = KeywordMapper.mapPerson(word);
                        if (mappedPerson != null) {
                            result.personType = mappedPerson;
                            System.out.println("→ Identified as a special person type: " + result.personType);
                            personDetected = true;
                        }
                        // If it's NOT a special person, then check if it's a contact type (e.g., relative, sibling)
                        else {
                            String mappedContact = KeywordMapper.mapContact(word);
                            if (mappedContact != null) {
                                result.relationType = mappedContact;
                                System.out.println("→ Identified as a contact type: " + result.relationType + " (Not a Name)");
                            }
                            // If it's neither, treat it as a first name
                            else {
                                processingLastName = false;
                                firstNameBuilder = new StringJoiner(" ");
                                firstNameBuilder.add(word);
                                personDetected = true;
                                System.out.println("→ First Name detected: " + word);
                            }
                        }
                    }
                    System.out.println("Person detected status: " + personDetected);
                    break;

                case "I-Person":
                    // If we detected a title before, treat this as part of the last name
                    if (processingLastName) {
                        lastNameBuilder.add(word);
                        personDetected = true;
                        System.out.println("Person detected status: " + personDetected);
                        System.out.println("→ Adding to Last Name: " + word);
                    }
                    // Otherwise, continue adding to first name
                    else {
                        firstNameBuilder.add(word);
                        personDetected = true;
                        System.out.println("→ Adding to First Name: " + word);
                    }
                    break;


                case "B-Time":
                    if (word.equalsIgnoreCase("wann") ) {
                        System.out.println("→ Ignored 'wann' as B-Time");
                        break;  // Ignore "wann" and move on
                    }
                    if(KeywordMapper.mapTime(word)!=null) {
                        System.out.print(word);
                    System.out.println(KeywordMapper.mapTime(word));

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
                    }
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

// If a person was detected and has a first name, check if they are an assigned patient
        if (personDetected && detectedFirstName != null) {
            result.isAssignedPatient = PatientBuffer.isAssignedPatient(fullName);
            System.out.println("→ Is Assigned Patient: " + result.isAssignedPatient);
            result.person = fullName;
        }
// If no person was detected, default to CURRENT_CLIENT instead of CURRENT_EMPLOYEE
        else if (!personDetected) {
            result.person = "CURRENT_CLIENT";
            System.out.println("→ No person detected, defaulting to CURRENT_CLIENT");
            if(NERPreprocessor.getOngoingAssignmentClientId(employeeId)!=null) {
                result.clientId = NERPreprocessor.getOngoingAssignmentClientId(employeeId);
                System.out.println(result.clientId);// Fetch using existing method
                System.out.println(result.person);
            }
            else{
                System.out.println("No active Client found");
            }
        }


// If for some reason we get here without detecting anything, default to CURRENT_EMPLOYEE
        else {
            result.person = "CURRENT_EMPLOYEE";
        }

// Assign other properties
        result.firstName = detectedFirstName;
        result.lastName = detectedLastName;
        result.entity = entityBuilder.length() > 0 ? entityBuilder.toString() : null;
        result.task = builder.length() > 0 ? builder.toString() : null;
        result.distance = distanceBuilder.length() > 0 ? distanceBuilder.toString() : null;

        System.out.println("====================================");

        return result;

    }
}
