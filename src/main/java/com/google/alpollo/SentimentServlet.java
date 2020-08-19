package com.google.alpollo;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.alpollo.model.SongSentiment;
import com.google.cloud.language.v1.Sentiment;
import com.google.gson.Gson;

@WebServlet("/sentiment")
public class SentimentServlet extends HttpServlet {
  private final Gson gson = new Gson();

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String lyrics = request.getParameter("lyrics");
    AnalysisHelper helper = new AnalysisHelper();
    Sentiment sentiment = helper.getSentiment(lyrics);
    SongSentiment songSentiment = new SongSentiment(sentiment.getScore(), sentiment.getMagnitude());
    
    String json = gson.toJson(songSentiment);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}