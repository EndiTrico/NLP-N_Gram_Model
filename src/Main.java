import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    private static final DecimalFormat decimalFormat = new DecimalFormat("0.00000");
    static Map<String, List<Double>> languageWithSimilarityAndAngle = new ConcurrentHashMap<>();
    static Map<String, String> languagePrefixNames = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        Path rootDir = Paths.get("src\\LocalFolder");

        Scanner input = new Scanner(System.in);
        System.out.print("Enter the N-Gram Model (between 1 and 3 inclusive): ");
        int NGramModel = validateInput(input.nextInt());

        input.close();

        updateLanguagePrefixNames();
        List<Language> folders = new ArrayList<>();
        final Language[] mysteryLanguage = new Language[1];

        ExecutorService executorService1 = Executors.newCachedThreadPool();

        Files.walkFileTree(rootDir, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                if(dir.getFileName().toString().equals("LocalFolder"))
                     mysteryLanguage[0] = new Language("mystery", NGramModel, dir.toString());
                else
                    folders.add(new Language(languagePrefixNames.getOrDefault(dir.getFileName().toString(), dir.getFileName().toString()), NGramModel, dir.toString()));

                return FileVisitResult.CONTINUE;
            }
        });

        executorService1.shutdown();
        try {
            if (!executorService1.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS))
                System.out.println("ExecutorService1 did not terminate in time.");
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }

        ExecutorService executorService = Executors.newCachedThreadPool();

        folders.forEach(folder -> executorService.submit(() -> calculateDocumentDistance(mysteryLanguage[0], folder)));

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS))
                System.out.println("ExecutorService did not terminate in time.");
        } catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
        languageWithSimilarityAndAngle.forEach((key, value) ->
            System.out.printf("Language:%-16sSimilarity:%-15.5fAngle:%-10.5f\n", key, value.get(0), value.get(1)));

        highestSimilarity();
    }

    private static void updateLanguagePrefixNames(){
        languagePrefixNames.put("al", "Albanian");
        languagePrefixNames.put("de", "German");
        languagePrefixNames.put("en", "English");
        languagePrefixNames.put("fr", "French");
        languagePrefixNames.put("gr", "Greek");
        languagePrefixNames.put("it", "Italian");
    }

    public static void calculateDocumentDistance(Language mystery, Language language){
        int dotProduct = mystery.getHistogram().entrySet().stream()
                .filter(entry -> language.getHistogram().containsKey(entry.getKey()))
                .mapToInt(entry -> entry.getValue() * language.getHistogram().get(entry.getKey()))
                .sum();

        double similarity = dotProduct / (mystery.getVector() * language.getVector());
        double angle = Math.toDegrees(Math.acos(similarity));

        languageWithSimilarityAndAngle.put(language.getName(), new ArrayList<>(List.of(
                Double.parseDouble(decimalFormat.format(similarity)),
                Double.parseDouble(decimalFormat.format(angle)))));
    }

    public static void highestSimilarity(){
        double maxSimilarity = languageWithSimilarityAndAngle.values().stream()
                .mapToDouble(doubles -> doubles.get(0))
                .max()
                .orElse(Double.NaN);

        double minAngle = languageWithSimilarityAndAngle.values().stream()
                .mapToDouble(doubles -> doubles.get(1))
                .min()
                .orElse(Double.NaN);

        List<String> languageName = languageWithSimilarityAndAngle.entrySet().stream()
                .filter(entry -> entry.getValue().get(0) == maxSimilarity)
                .map(Map.Entry::getKey)
                .toList();

        printResult(languageName, maxSimilarity, minAngle);
    }

    public static void printResult(List<String> languageName, double maxSimilarity, double minAngle){
        if(languageName.isEmpty()){
            System.out.println("\nThere is no language similar to mystery.txt");
        } else if (languageName.size() == 1) {
            System.out.println("\nThere is only one language with the same highest similarity value.");
            languageName.forEach(language ->
                    System.out.printf("Nearest Language with mystery.txt is: %s Language with Similarity: %.5f and Angle: %.5f.", language, maxSimilarity, minAngle));
        } else{
            System.out.println("\nThere are " + languageName.size() + " languages with the same highest similarity value.");
            languageName.forEach(language ->
                    System.out.printf("Nearest Language with mystery.txt is: %s Language with Similarity: %.5f and Angle: %.5f.\n", language, maxSimilarity, minAngle));
        }
    }

    public static int validateInput(int NGramModel){
        if (NGramModel >= 1 && NGramModel <= 3)
            return NGramModel;
        return 2;
    }
}