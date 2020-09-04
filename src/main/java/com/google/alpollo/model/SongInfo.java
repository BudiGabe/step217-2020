package com.google.alpollo.model;

import java.util.List;
import com.google.alpollo.Song;
import com.google.alpollo.model.SongEntity;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Entity;

/**
 * Class used for storing data related to the Natural Language API analysis.
<<<<<<< HEAD
 * Extending the Song class, it provides all the info related to a specific song.
=======
 * It provides all the info related to a specific song.
>>>>>>> songInfo-class
 */
@Entity
public class SongInfo {
  /** All the info in this class is attached to a specific parent song. */
  private final Song parentSong;
  /** Score tells us the overall positivty of the song, ranging from -1.0 to 1.0. */
  @Index private float score;
  /** Magnitude tells us how strong the main sentiment of the song is, ranging from 0.0 to +inf. */
  @Index private float magnitude;
  /** TopSalientEntities is a list containing the 10 most important words, given the song context. */
  @Index private List<SongEntity> topSalientEntities;
  @Index private String lyrics;
  /** List of YouTube video IDs based on the most salient entity */
  @Index private List<String> youTubeIds;

  public SongInfo(Song parentSong, float score, float magnitude,
      List<SongEntity> topSalientEntities, String lyrics, List<String> youTubeIds) {
        this.parentSong = parentSong;
    this.score = score;
    this.magnitude = magnitude;
    this.topSalientEntities = topSalientEntities;
    this.lyrics = lyrics;
    this.youTubeIds = youTubeIds;
  }
}