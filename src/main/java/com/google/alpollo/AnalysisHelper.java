package com.google.alpollo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.google.alpollo.model.SongEntity;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Sentiment;

/**
 * Helper class with methods that use the Natural Language API or use the data that comes
 * from the API.
 */
public final class AnalysisHelper {
  private static LanguageServiceSettings settings;
  /** How many entities we want in our TopSalientEntities list. (Top 10, Top 15 etc.)*/
  private static final int MAX_ENTITIES = 10;
  /** The index where we start our top list from. */
  private static final int FIRST_ENTITY = 0;
  private static String projectID;

  private AnalysisHelper() {};

  private static void createLanguageServiceSettings() throws IOException {
    if (projectID == null) {
      projectID = ConfigHelper.getProjectID();
      if (projectID == null) {
        throw new IllegalStateException("Failed to obtain Project ID.");
      }
    }

    if (settings == null) {
      settings = LanguageServiceSettings.newBuilder().setHeaderProvider(
          FixedHeaderProvider.create("X-Goog-User-Project", projectID)).build();
    }
  }

  /**
   * Based on the lyrics sent, the AI can extract the main "sentiment" of the text.
   * This sentiment has a score, showing the overall positivity of the text, ranging from -1.0 to 1.0
   * and a magnitude, representing how strong the sentiment is, ranging from 0.0 to +inf.
   */
  public static Sentiment getSentiment(String lyrics) throws IllegalStateException, IOException {
    createLanguageServiceSettings();

    try (LanguageServiceClient language = LanguageServiceClient.create(settings)) {
      Document doc = Document.newBuilder().setContent(lyrics).setType(Document.Type.PLAIN_TEXT).build();
      Sentiment sentiment = language.analyzeSentiment(doc).getDocumentSentiment();
      
      return sentiment;
    }
  }

  /**
   * Based on the lyrics sent, the AI can extract the main "entities" present in the text.
   * Basically, it can figure out what the author speaks about: places, people, things etc.
   * Each entity object has a name and a salience score, telling us how important the word is,
   * ranging from 0 to 1.0 .
   */
  public static List<Entity> getEntityList(String lyrics) throws IOException {
    createLanguageServiceSettings();

    try (LanguageServiceClient language = LanguageServiceClient.create(settings)) {
      Document doc = Document.newBuilder().setContent(lyrics).setType(Document.Type.PLAIN_TEXT).build();
      AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder().setDocument(doc)
          .setEncodingType(EncodingType.UTF16).build();
      AnalyzeEntitiesResponse response = language.analyzeEntities(request);

      return response.getEntitiesList();
    }
  }

  /**
   * From every entity generated by the API we get only part of the information (name and salience)
   * and store the new SongEntities into a new simplified list. 
   */
  public static List<SongEntity> getSimplifiedEntityList(List<Entity> entityList) {
    List<SongEntity> simplifiedEntityList = new ArrayList<>();

    for (Entity entity : entityList) {
      // Use round() here to set the double to 2 decimals.
      SongEntity simplifiedEntity = new SongEntity(entity.getName(), 
          Math.round(entity.getSalience() * 100.0) / 100.0, entity.getType().toString(),
          entity.getMetadataMap().getOrDefault("wikipedia_url", ""));
      simplifiedEntityList.add(simplifiedEntity);
    }

    return simplifiedEntityList;
  }

  public static List<SongEntity> getTopSalientEntities(List<SongEntity> simplifiedEntityList) {
    List<SongEntity> tempList = new ArrayList<>(simplifiedEntityList);
    tempList.sort(SongEntity.ORDER_BY_SALIENCE_DESCENDING);

    if (tempList.size() <= MAX_ENTITIES) {
      return tempList;
    } else {
        return tempList.subList(FIRST_ENTITY, MAX_ENTITIES);
    }
  }
}