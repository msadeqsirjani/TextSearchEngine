package search;

import java.time.LocalDateTime;

public class Word {
    private int numberOfLine;
    private String word;
    private int thread;
    private LocalDateTime time;

    public Word(int numberOfLine, String word, int thread, LocalDateTime time) {
        this.numberOfLine = numberOfLine;
        this.word = word;
        this.thread = thread;
        this.time = time;
    }

    public String getNumberOfLine() {
        return Integer.toString(numberOfLine);
    }

    public String getWord() {
        return word;
    }

    public String getThread() {
        return Integer.toString(thread);
    }

    public String getTime() {
        return time.toString();
    }

    @Override
    public String toString() {
        return "Word{" +
                "numberOfLine=" + numberOfLine +
                ", word='" + word + '\'' +
                ", thread=" + thread +
                ", time=" + time +
                '}';
    }
}
