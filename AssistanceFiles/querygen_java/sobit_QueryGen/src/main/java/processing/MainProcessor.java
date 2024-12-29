package processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainProcessor {
    private final TagProcessor tagProcessor;
    private final EmployeeHandler employeeHandler;
    private final PersonHandler personHandler;

    public MainProcessor(TagProcessor tagProcessor, EmployeeHandler employeeHandler, PersonHandler personHandler) {
        this.tagProcessor = tagProcessor;
        this.employeeHandler = employeeHandler;
        this.personHandler = personHandler;
    }

    public List<List<String>> process(List<List<String>> input, Map<String, String> personMapping) {
        // Parse input into TaggedElement list
        List<TaggedElement> taggedElements = parseInput(input);

        // Process through the pipeline
        taggedElements = tagProcessor.filterElements(taggedElements);
        taggedElements = tagProcessor.mergeWordsWithPrefix(taggedElements);
        taggedElements = personHandler.mapPersonTags(taggedElements, personMapping);
        taggedElements = personHandler.classifyLastName(taggedElements);

        // Convert back to the original format
        return formatOutput(taggedElements);
    }

    private List<TaggedElement> parseInput(List<List<String>> input) {
        List<TaggedElement> taggedElements = new ArrayList<>();
        List<String> words = input.get(0);
        List<String> tags = input.get(1);

        for (int i = 0; i < words.size(); i++) {
            taggedElements.add(new TaggedElement(words.get(i), tags.get(i)));
        }
        return taggedElements;
    }

    private List<List<String>> formatOutput(List<TaggedElement> taggedElements) {
        List<String> words = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        for (TaggedElement element : taggedElements) {
            words.add(element.getWord());
            tags.add(element.getTag());
        }

        List<List<String>> output = new ArrayList<>();
        output.add(words);
        output.add(tags);
        return output;
    }
}
