import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

class ReviewRunner {
  public static String[] parseCsvLine(String line) {
    ArrayList<String> values = new ArrayList<String>();
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;

    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);

      if (ch == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          current.append('"');
          i++;
        } else {
          inQuotes = !inQuotes;
        }
      } else if (ch == ',' && !inQuotes) {
        values.add(current.toString());
        current.setLength(0);
      } else {
        current.append(ch);
      }
    }

    values.add(current.toString());
    return values.toArray(new String[0]);
  }

  public static double getReviewSentiment(String reviewText) {
    String[] words = reviewText.toLowerCase().replaceAll("[^a-zA-Z ]", " ").split("\\s+");
    double total = 0.0;
    int wordCount = 0;

    for (String word : words) {
      String cleaned = Review.removePunctuation(word.trim());
      if (cleaned.length() == 0) {
        continue;
      }

      double score = Review.sentimentVal(cleaned);
      total += score;
      wordCount++;
    }

    if (wordCount == 0) {
      return 0.0;
    }

    return total / wordCount;
  }

  public static double getMedian(ArrayList<Double> values) {
    Collections.sort(values);
    int size = values.size();

    if (size == 0) {
      return 0.0;
    }

    if (size % 2 == 0) {
      return (values.get(size / 2 - 1) + values.get(size / 2)) / 2.0;
    }

    return values.get(size / 2);
  }

  public static void main(String[] args) {
    ArrayList<Double> sentimentValues = new ArrayList<Double>();

    try {
      File dataFile = new File("amazon_reviews.csv");
      if (!dataFile.exists()) {
        dataFile = new File("..", "amazon_reviews.csv");
      }

      Scanner input = new Scanner(dataFile);
      if (input.hasNextLine()) {
        input.nextLine();
      }

      while (input.hasNextLine()) {
        String line = input.nextLine();
        if (line.trim().isEmpty()) {
          continue;
        }

        String[] columns = parseCsvLine(line);
        if (columns.length > 4) {
          String reviewText = columns[4];
          double reviewSentiment = getReviewSentiment(reviewText);
          sentimentValues.add(reviewSentiment);
        }
      }

      input.close();
    } catch (Exception e) {
      System.out.println("Unable to read amazon_reviews.csv");
      e.printStackTrace();
      return;
    }

    double medianSentiment = getMedian(sentimentValues);
    String consensus;

    if (medianSentiment > 0.15) {
      consensus = "positive";
    } else if (medianSentiment < -0.15) {
      consensus = "negative";
    } else {
      consensus = "mixed/neutral";
    }

    System.out.println("General consensus about Amazon products: " + consensus);
    System.out.println("Median sentiment value: " + medianSentiment);
  }
}