import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
  static String[] sentimentWords = new String[10000];
  static double[] sentimentScores = new double[10000];
  static int sentimentWordCount = 0;

  public static void main(String[] args) {
    if (!loadWordScores()) {
      return;
    }

    double totalSentiment = 0.0;
    int reviewCount = 0;

    try (Scanner scanner = new Scanner(new File("amazon_reviews.csv"))) {
      if (scanner.hasNextLine()) {
        scanner.nextLine();
      }

      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        String[] columns = splitCsvLine(line);
        if (columns.length <= 4) {
          continue;
        }

        double reviewSentiment = getReviewSentiment(columns[4]);
        if (!Double.isNaN(reviewSentiment)) {
          totalSentiment += reviewSentiment;
          reviewCount++;
        }
      }
    } catch (FileNotFoundException e) {
      System.out.println("Error: amazon_reviews.csv was not found.");
      return;
    }

    double averageSentiment = reviewCount == 0 ? 0.0 : totalSentiment / reviewCount;
    String consensus;

    if (averageSentiment > 0.15) {
      consensus = "positive";
    } else if (averageSentiment < -0.15) {
      consensus = "negative";
    } else {
      consensus = "mixed/neutral";
    }

    System.out.println("General consensus about Amazon products: " + consensus);
    System.out.println("Average sentiment value: " + averageSentiment);
    System.out.println("Reviews analyzed: " + reviewCount);
  }

  private static boolean loadWordScores() {
    try (Scanner scanner = new Scanner(new File("ConsumerLab_Code/cleanSentiment.csv"))) {
      while (scanner.hasNextLine()) {
        String[] parts = scanner.nextLine().split(",");
        if (parts.length == 2 && sentimentWordCount < sentimentWords.length) {
          sentimentWords[sentimentWordCount] = parts[0].toLowerCase();
          sentimentScores[sentimentWordCount] = Double.parseDouble(parts[1]);
          sentimentWordCount++;
        }
      }
    } catch (FileNotFoundException e) {
      System.out.println("Error: cleanSentiment.csv was not found.");
      return false;
    }

    return true;
  }

  private static double getReviewSentiment(String review) {
    double total = 0.0;
    int wordsFound = 0;
    String[] words = review.toLowerCase().split("[^a-z]+");

    for (String word : words) {
      for (int i = 0; i < sentimentWordCount; i++) {
        if (sentimentWords[i].equals(word)) {
          total += sentimentScores[i];
          wordsFound++;
          break;
        }
      }
    }

    if (wordsFound == 0) {
      return Double.NaN;
    }

    return total / wordsFound;
  }

  private static String[] splitCsvLine(String line) {
    String[] columns = new String[20];
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;
    int columnCount = 0;

    for (int i = 0; i < line.length(); i++) {
      char character = line.charAt(i);

      if (character == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          current.append('"');
          i++;
        } else {
          inQuotes = !inQuotes;
        }
      } else if (character == ',' && !inQuotes) {
        columns[columnCount] = current.toString();
        columnCount++;
        current.setLength(0);
      } else {
        current.append(character);
      }
    }

    columns[columnCount] = current.toString();
    columnCount++;

    String[] result = new String[columnCount];
    for (int i = 0; i < columnCount; i++) {
      result[i] = columns[i];
    }
    return result;
  }

}
