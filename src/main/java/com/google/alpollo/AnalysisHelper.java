package com.google.alpollo;

import java.io.IOException;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Sentiment;

/**
 * Helper class with methods that use the Natural Language API or use the data that comes
 * from the API.
 */
public class AnalysisHelper {
  private static LanguageServiceSettings SETTINGS;

  private AnalysisHelper() {};
  
  /**
   * Based on the lyrics sent, the AI can extract the main "sentiment" of the text.
   * This sentiment has a score, showing the overall positivity of the text, ranging from -1.0 to 1.0
   * and a magnitude, representing how strong the sentiment is, ranging from 0.0 to +inf.
   */
  public static Sentiment getSentiment(String lyrics) throws IOException {
    if (SETTINGS == null) {
      SETTINGS = LanguageServiceSettings.newBuilder().setHeaderProvider(
          FixedHeaderProvider.create("X-Goog-User-Project", "google.com:alpollo-step-2020")).build();
    }
    try (LanguageServiceClient language = LanguageServiceClient.create(SETTINGS)) {
      Document doc = Document.newBuilder().setContent(lyrics).setType(Document.Type.PLAIN_TEXT).build();
      Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
      return sentiment;
    } 
  }
}