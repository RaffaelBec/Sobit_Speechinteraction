package processing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PersonHandler {
    public List<TaggedElement> mapPersonTags(List<TaggedElement> inputTags, Map<String, String> personMapping) {
        List<TaggedElement> mappedTags = new ArrayList<>();
        for (TaggedElement tag : inputTags) {
            if (tag.getTag().contains("Person")) {
                String category = personMapping.getOrDefault(tag.getWord().toLowerCase(), "name");
                mappedTags.add(new TaggedElement(tag.getWord(), category));
            } else {
                mappedTags.add(tag);
            }
        }
        return mappedTags;
    }

    public List<TaggedElement> classifyLastName(List<TaggedElement> inputTags) {
        List<TaggedElement> result = new ArrayList<>();
        boolean skipNext = false;

        for (int i = 0; i < inputTags.size(); i++) {
            if (skipNext) {
                skipNext = false;
                continue;
            }

            TaggedElement tag = inputTags.get(i);
            if ("title".equals(tag.getTag()) && i + 1 < inputTags.size() && "name".equals(inputTags.get(i + 1).getTag())) {
                result.add(new TaggedElement(inputTags.get(i + 1).getWord(), "lastname"));
                skipNext = true;
            } else {
                result.add(tag);
            }
        }
        return result;
    }
}

