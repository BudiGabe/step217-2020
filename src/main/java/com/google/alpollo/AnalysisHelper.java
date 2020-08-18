package com.google.alpollo;

import java.io.*;
import java.util.Objects;

import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;

/**
 * Helper class for cleaner code and easier testing.
 */

public class AnalysisHelper {
  private final Gson gson = new Gson();

  /**
   * Based on the lyrics sent, the AI can extract the main "sentiment" of the text.
   * This sentiment has a score, showing the overall positivity of the text, ranging from -1 to 1
   * and a magnitude, representing how strong the sentiment is, ranging from 0 to 1
   */
  public Sentiment getSentiment(String lyrics) throws Exception {
    Document doc =
            Document.newBuilder().setContent(lyrics).setType(Document.Type.PLAIN_TEXT).build();

    InputStream inputStream = ClassLoader.getSystemResourceAsStream("config.json");
    final Reader reader = new InputStreamReader(Objects.requireNonNull(inputStream));
    String projectID = gson.fromJson(reader, ConfigInfo.class).getProjectID();

    // Set the header manually so we can use the Natural Language API.
    LanguageServiceSettings settings = LanguageServiceSettings.newBuilder().setHeaderProvider(
            FixedHeaderProvider.create("X-Goog-User-Project", projectID)).build();
    LanguageServiceClient languageService = LanguageServiceClient.create(settings);
    Sentiment sentiment = languageService.analyzeSentiment(doc).getDocumentSentiment();
    languageService.close();
    return sentiment;
  }
}