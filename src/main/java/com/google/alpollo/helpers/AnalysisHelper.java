package com.google.alpollo.helpers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.alpollo.model.SongEntity;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.language.v1.Sentiment;
import com.google.common.collect.Sets;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;

/**
 * Helper class with methods that use the Natural Language API or use the data that comes
 * from the API.
 */
public final class AnalysisHelper {
  private static LanguageServiceSettings settings;
  /** How many entities we want in our TopSalientEntities list. (Top 10, Top 15 etc.)*/
  private static final int MAX_ENTITIES = 10;
  public static final String NEUTRAL = "This song doesn't convey any positive or negative feelings.";
  public static final String NEGATIVE = "This song is predominantly negative.";
  public static final String POSITIVE = "This song is predominantly positive";
  public static final String MIXED = "This song has a lot of mixed feelings";
  private static final double NEUTRAL_MAGNITUDE_THRESHOLD = 2;
  private static final double NEGATIVE_SCORE_THRESHOLD = -0.15;
  private static final double POSITIVE_SCORE_THRESHOLD = 0.15;

  private AnalysisHelper() {};

  private static void createLanguageServiceSettings(String projectID) throws IOException {
    if (projectID == null) {
      throw new IllegalStateException("Project ID wasn't defined.");
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
  public static Sentiment getSentiment(String projectID, String lyrics) throws IllegalStateException, IOException {
    createLanguageServiceSettings(projectID);

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
  public static List<Entity> getEntityList(String projectID, String lyrics) throws IOException {
    createLanguageServiceSettings(projectID);

    try (LanguageServiceClient language = LanguageServiceClient.create(settings)) {
      Document doc = Document.newBuilder().setContent(lyrics).setType(Document.Type.PLAIN_TEXT).build();
      AnalyzeEntitiesRequest request = AnalyzeEntitiesRequest.newBuilder().setDocument(doc)
          .setEncodingType(EncodingType.UTF16).build();
      AnalyzeEntitiesResponse response = language.analyzeEntities(request);

      return response.getEntitiesList();
    }
  }

  /**
   * From every entity generated by the API we get only part of the information 
   * (name, salience, type and wikiLink) and store the new SongEntities into a new simplified list. 
   * 
   * We round the salience value to allow us to actually test these methods.
   * In the beginning, every entity has only one type, so we just populate the set
   * with only one string.
   */
  public static List<SongEntity> getSimplifiedEntityList(List<Entity> entityList) {
    List<SongEntity> simplifiedEntityList = new ArrayList<>();

    for (Entity entity : entityList) {
      // Use round() here to set the double to 2 decimals.
      SongEntity simplifiedEntity = new SongEntity(entity.getName(), 
          entity.getSalience(), 
          Sets.newHashSet(entity.getType().toString()),
          entity.getMetadataMap().getOrDefault("wikipedia_url", ""));
      simplifiedEntityList.add(simplifiedEntity);
    }

    return simplifiedEntityList;
  }

  /**
   * Given the score and magnitude of a sentiment, this will return a general interpretation,
   * telling the user what the values mean.
   */
  public static String getInterpretation(double score, double magnitude) {
    if (magnitude < NEUTRAL_MAGNITUDE_THRESHOLD) {
      return NEUTRAL;
    }

    if (score < NEGATIVE_SCORE_THRESHOLD) {
      return NEGATIVE;
    }

    if (score > POSITIVE_SCORE_THRESHOLD) {
      return POSITIVE;
    }

    return MIXED;
  }

  public static List<SongEntity> getFilteredTopEntities(List<SongEntity> list) {
    HashMap<String, SongEntity> map = new HashMap<>(); 

    for (SongEntity entity : list) {
      if (map.containsKey(entity.getName())) {
        SongEntity existingEntity = map.get(entity.getName());
        Set<String> newTypes = entity.getType();
        Set<String> existingTypes = existingEntity.getType();
        double newSalience = entity.getSalience() + existingEntity.getSalience();
        
        existingTypes.addAll(newTypes);
        existingEntity.setSalience(newSalience);
      } else {
        map.put(entity.getName(), entity);
      }
    }

    return map.values().stream()
    .sorted(SongEntity.ORDER_BY_SALIENCE_DESCENDING)
    .limit(MAX_ENTITIES)
    .collect(Collectors.toList());
  }
}