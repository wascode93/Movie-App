package com.example.mal_phase_1;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by MW on 3/24/2016.
 */
public class Movie implements Serializable {

    private String originalTitle;
    private String poster;
    private String overview;
    private String voteAverage;
    private String releaseDate;
    private String id;

    public Movie() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public JSONObject toJson () throws JSONException {

        final String POSTER_PATH = "poster_path";
        final String OVERVIEW = "overview";
        final String RELEASE_DATE = "release_date";
        final String ORIGINAL_TITLE = "original_title";
        final String VOTE_AVERAGE = "vote_average";
        final String ID = "id";

        JSONObject movie = new JSONObject();

        movie.put(POSTER_PATH, this.poster);
        movie.put(OVERVIEW, this.overview);
        movie.put(RELEASE_DATE, this.releaseDate);
        movie.put(ORIGINAL_TITLE, this.originalTitle);
        movie.put(VOTE_AVERAGE, this.voteAverage);
        movie.put(ID, this.id);

        return movie;
    }
}
