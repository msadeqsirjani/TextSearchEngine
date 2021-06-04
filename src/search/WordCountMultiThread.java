package search;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class WordCountMultiThread {
    private static final int THREAD_COUNT = 1;


    private static class FileIterator implements Iterator, AutoCloseable {
        private final BufferedReader br;
        private List<String> words;
        private String nextLine;
        private int numberOfLine;

        public FileIterator(String fileName, String pattern) throws IOException {
            this.br = new BufferedReader(new FileReader(fileName));
            this.nextLine = br.readLine();
            this.numberOfLine = 1;
            this.words = new ArrayList<>();

            next(new BufferedReader(new FileReader(pattern)));
        }


        @Override
        public boolean hasNext() {
            return nextLine != null;
        }

        public List<String> getWords() {
            return this.words;
        }

        @Override
        public WordLine next() {
            String lineToReturn = nextLine;
            try {
                nextLine = br.readLine();
                numberOfLine++;
            } catch (IOException e) {
                nextLine = null;
                numberOfLine = -1;
            }
            return new WordLine(this.numberOfLine, lineToReturn);
        }

        public void next(BufferedReader reader) {
            try {
                String lineToReturn = reader.readLine();
                this.words.add(lineToReturn);
                if(lineToReturn == null)
                    return;
                next(reader);
            } catch (IOException ignored) {
                return;
            }
        }


        @Override
        public void remove() {
            throw new NotImplementedException();
        }


        @Override
        public void close() throws IOException {
            br.close();
        }
    }


    private static class Transformers {
        public String[] mapToTokens(String input) {
            return input.split("[ _.,\\-+]");
        }


        private String[] filterIllegalTokens(String[] words) {
            List<String> filteredList = new ArrayList<>();
            for (String word : words) {
                if (word.matches("[a-zA-Z]+")) {
                    filteredList.add(word);
                }
            }
            return filteredList.toArray(new String[0]);
        }


        private String[] mapToLowerCase(String[] words) {
            String[] filteredList = new String[words.length];
            for (int i = 0; i < words.length; i++) {
                filteredList[i] = words[i].toLowerCase();
            }
            return filteredList;
        }


        public synchronized void reduce(List<Word> words, List<String> pattern, int thread, String word, int numberOfLine) {
            if (pattern.contains(word))
                words.add(new Word(numberOfLine, word, thread, LocalDateTime.now()));
        }
    }


    private static class TransformationThread implements Runnable {
        private final Transformers tr;
        private final Queue<WordLine> dataQueue;
        private final List<Word> words;
        private final List<String> pattern;
        private final int thread;


        public TransformationThread(Transformers tr, List<Word> words, Queue<WordLine> dataQueue, List<String> pattern, int thread) {
            this.tr = tr;
            this.dataQueue = dataQueue;
            this.words = words;
            this.pattern = pattern;
            this.thread = thread;
        }


        @Override
        public void run() {
            while (!dataQueue.isEmpty()) {
                WordLine wordLine = dataQueue.poll();
                String line = wordLine.getContent();
                int numberOfLine = wordLine.getNumberOfLine();
                if (line != null) {
                    String[] words = tr.mapToTokens(line);
                    String[] legalWords = tr.filterIllegalTokens(words);
                    String[] lowerCaseWords = tr.mapToLowerCase(legalWords);
                    for (String word : lowerCaseWords) {
                        tr.reduce(this.words, this.pattern, this.thread, word, numberOfLine);
                    }
                }
            }
        }
    }


    public static void main(final String[] args) throws Exception {
        Transformers tr = new Transformers();
        List<Word> words = new ArrayList<>();
        WriteFile writer = new WriteFile(args[2]);
        final Queue<WordLine> dataQueue = new ConcurrentLinkedQueue<>();
        final List[] pattern = new List[]{new ArrayList<>()};
        new Thread(() -> {
            try (FileIterator fc = new FileIterator(args[0], args[1])) {
                pattern[0] = fc.getWords();
                while (fc.hasNext()) {
                    dataQueue.add(fc.next());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        while (dataQueue.isEmpty())
            Thread.sleep(10);

        Instant starts = Instant.now();

        ExecutorService es = Executors.newFixedThreadPool(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++)
            es.execute(new TransformationThread(tr, words, dataQueue, pattern[0], i));
        es.shutdown();
        es.awaitTermination(1, TimeUnit.MINUTES);

        Instant ends = Instant.now();

        writer.WriteToFile(words, Duration.between(starts, ends).toString());
    }
}