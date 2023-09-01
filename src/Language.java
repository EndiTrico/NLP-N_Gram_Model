import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class Language {
    private String name;
    private int NGramModel;
    private String path;
    private final Map<String, Integer> histogram;
    private String text;
    private double vector;
    private final Object vectorLock = new Object();

    public Language(String name, int NGramModel, String path) {
        setName(name);
        setNGramModel(NGramModel);
        setPath(path);
        setText("");
        histogram = new ConcurrentHashMap<>();
        vector = 0.0D;
        getTextFiles();
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return name;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Integer> getHistogram() {
        return histogram;
    }

    public int getNGramModel() {
        return NGramModel;
    }

    public void setNGramModel(int NGramModel) {
        this.NGramModel = NGramModel;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setVector(double vector) {
        synchronized (vectorLock) {
            this.vector = vector;
        }
    }

    public double getVector() {
        synchronized (vectorLock) {
            return vector;
        }
    }

    private void getTextFiles() {
        File folder = new File(getPath());

        FilenameFilter textFileFilter = (dir, name) -> name.toLowerCase().endsWith(".txt");

        File[] textFiles = folder.listFiles(textFileFilter);

        StringBuilder contentOfTextFiles = new StringBuilder();

        if (textFiles != null && textFiles.length > 0) {
            Arrays.stream(textFiles).forEach(textFile -> {
                try {
                    String fileContent = Files.readString(textFile.toPath());
                    contentOfTextFiles.append(fileContent).append(" ");
                } catch (IOException e) {
                    System.err.println("Error reading file: " + textFile.getName() + " on folder " + getName());
                }
            });
        }
        removeDigitsPunctuation(contentOfTextFiles);
    }

    private void removeDigitsPunctuation(StringBuilder originalText){
        setText(originalText.toString()
                .toLowerCase()
                .replaceAll("\\p{Punct}", " ")
                .replaceAll("\\d", " ")
                .replaceAll("\\s{2,}", " ")
                .trim());
        tokenize();
    }

    private void tokenize(){
        String[] words = getText().split(" ");

        Arrays.stream(words).forEach(word -> {
            if (word.length() >= getNGramModel()) {
                IntStream.range(0, word.length() - getNGramModel() + 1)
                        .mapToObj(index -> word.substring(index, index + getNGramModel()))
                        .forEach(this::populateHistogram);}
        });

        calculateVector();
    }

    public void populateHistogram(String token){
        synchronized (histogram) {
            histogram.put(token, histogram.getOrDefault(token, 0) + 1);
        }
    }

    public void calculateVector() {
        int sum;

        synchronized (histogram) {
            sum = histogram.values().stream()
                    .mapToInt(value -> value * value)
                    .sum();
        }

        setVector(Math.sqrt(sum));
    }
}