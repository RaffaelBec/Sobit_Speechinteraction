package processing;

public class TaggedElement {
    private String word;
    private String tag;

    public TaggedElement(String word, String tag) {
        this.word = word;
        this.tag = tag;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "TaggedElement{" +
                "word='" + word + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }
}
