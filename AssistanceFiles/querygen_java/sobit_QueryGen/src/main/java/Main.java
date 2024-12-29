import processing.*;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Sample input data
        List<TaggedElement> sampleData = Arrays.asList(
                new TaggedElement("wann", "B-Time"),
                new TaggedElement("hat", "O"),
                new TaggedElement("Benjamin", "B-Person"),
                new TaggedElement("Fiala", "I-Person"),
                new TaggedElement("zuletzt", "B-Time"),
                new TaggedElement("eine", "O"),
                new TaggedElement("betreuung", "B-Entity"),
                new TaggedElement("gehabt", "O")
        );

        // Instantiate the MainProcessor
        MainProcessor mainProcessor = new MainProcessor();

        // Process the data
        List<TaggedElement> processedData = mainProcessor.process(sampleData);

        // Print the results
        System.out.println("Processed Data:");
        for (TaggedElement element : processedData) {
            System.out.println(element.getWord() + " - " + element.getTag());
        }
    }
}
