package search;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;

public class WriteFile {
    private String path;
    private boolean appendToFile = false;

    public WriteFile(String path) {
        this.path = path;
    }

    public WriteFile(String path, boolean appendToFile) {
        this.path = path;
        this.appendToFile = appendToFile;
    }

    public void WriteToFile(List<Word> words, String totalTime) throws IOException {

        FileWriter writer = new FileWriter(this.path, this.appendToFile);
        PrintWriter printer = new PrintWriter(writer);

        String now = LocalDateTime.now().toString();

        for (Word word : words) {
            printer.printf("Line: %s, Thread:%s, Word: %s, Find:[%s], Print:[%s],%n",
                    word.getNumberOfLine(), word.getThread(), word.getWord(), word.getTime(), now);
        }

        printer.println("TotalTime: " + totalTime);

        printer.close();
    }
}
