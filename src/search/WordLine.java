package search;

public class WordLine {
    private int numberOfLine;
    private String content;

    public WordLine(int line, String content) {
        this.numberOfLine = line;
        this.content = content;
    }

    public int getNumberOfLine() {
        return numberOfLine;
    }

    public String getContent() {
        return content;
    }
}
