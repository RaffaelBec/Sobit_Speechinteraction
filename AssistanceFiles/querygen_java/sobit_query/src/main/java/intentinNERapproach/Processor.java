package intentinNERapproach;

import org.example.KeywordMapper;
import java.util.List;
import java.util.StringJoiner;

public class Processor {
    private List<String[]> nerTaggedWords;
    private String firstName;
    private String lastName;
    private String intent;
    private ProcessingResult result;

    public Processor(List<String[]> nerTaggedWords, String firstName, String lastName, String intent) {
        this.nerTaggedWords = nerTaggedWords;
        this.firstName = firstName;
        this.lastName = lastName;
        this.intent = intent;
        this.result = new ProcessingResult();

        // Preprocess tokens: Remove unnecessary elements
        this.nerTaggedWords = removeHashtagsAndO(nerTaggedWords);

        // Initialize name builders
        firstNameBuilder = new StringJoiner(" ");
        lastNameBuilder = new StringJoiner(" ");

        // Process tokens to extract name information
        this.nerTaggedWords = processTokens(this.nerTaggedWords);

        // Store processed names
        this.firstName = firstNameBuilder.toString();
        this.lastName = lastNameBuilder.toString();

        result.extractedFirstName = this.firstName;
        result.extractedLastName = this.lastName;
        System.out.println("Final Extracted Name: " + this.firstName + " " + this.lastName);
    }

    StringJoiner firstNameBuilder = new StringJoiner(" ");
    StringJoiner lastNameBuilder = new StringJoiner(" ");
    StringJoiner entityBuilder = new StringJoiner(" ");
    StringJoiner builder = new StringJoiner(" ");
    StringJoiner distanceBuilder = new StringJoiner(" ");
    boolean processingLastName = false;
    boolean personDetected = false;

    public static class ProcessingResult {
        public String extractedFirstName = "";
        public String extractedLastName = "";
    }

    public ProcessingResult getResult() {
        return result;
    }

    public List<String[]> processTokens(List<String[]> nerTaggedWords) {
        for (String[] token : nerTaggedWords) {
            String word = token[0];
            String tag = token[1];
            if (shouldSkipToken(word)) continue;
            System.out.println("Token: " + word + " | Tag: " + tag);
            if (tag.equals("B-Person")) {
                processPerson(word);
            } else if (tag.equals("I-Person")) {
                continuePerson(word);
            }
        }
        return nerTaggedWords;
    }

    private boolean isTitle(String word) {
        return word.equalsIgnoreCase("herr") || word.equalsIgnoreCase("frau") || word.equalsIgnoreCase("herrn");
    }

    private void processPerson(String word) {
        if (shouldSkipToken(word)) return;
        System.out.println("→ Processing new PERSON token: " + word);

        // Check if the word should be mapped away
        String mappedContact = KeywordMapper.mapContact(word);
        if (mappedContact != null) {
            System.out.println("→ Skipping word (mapped as contact type): " + word);
            return;
        }

        String mappedPerson = KeywordMapper.mapPerson(word);
        if (mappedPerson != null) {
            result.extractedFirstName = mappedPerson;
            System.out.println("→ Identified as special person type: " + mappedPerson);
            return;
        }

        if (isTitle(word)) {
            processingLastName = true;
            lastNameBuilder = new StringJoiner(" ");
            System.out.println("→ Title detected: " + word + " (Expecting Last Name next)");
        } else {
            assignPersonName(word);
        }
    }

    private void continuePerson(String word) {
        if (processingLastName) {
            lastNameBuilder.add(word);
            System.out.println("→ Adding to Last Name: " + word);
        } else {
            firstNameBuilder.add(word);
            System.out.println("→ Adding to First Name: " + word);
        }
    }

    private void assignPersonName(String word) {
        firstNameBuilder.add(word);
        processingLastName = false;
        System.out.println("→ First Name detected: " + word);
    }

    private boolean shouldSkipToken(String word) {
        return word.equalsIgnoreCase("o") || word.equals("[CLS]") || word.equals("[SEP]") || word.isEmpty();
    }

    public List<String[]> removeHashtagsAndO(List<String[]> input) {
        for (int i = 1; i < input.size(); i++) {
            String word = input.get(i)[0];
            if (word.startsWith("##")) {
                String cleanWord = word.substring(2);
                String[] prevEntry = input.get(i - 1);
                prevEntry[0] = prevEntry[0] + cleanWord;
                input.get(i)[0] = "";
                while (i + 1 < input.size() && input.get(i + 1)[0].startsWith("##")) {
                    i++;
                    cleanWord = input.get(i)[0].substring(2);
                    prevEntry[0] = prevEntry[0] + cleanWord;
                    input.get(i)[0] = "";
                }
            }
        }
        return input;
    }
}
