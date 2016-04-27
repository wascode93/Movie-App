package com.example.mal_phase_1;

import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by MW on 3/24/2016.
 */
public class MoviesFragment extends Fragment {

    List<Movie> movies;
    GridView moviesGrid;
    MovieAdapter adapter;
    MovieInterface activity;

    public MoviesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        movies = new ArrayList<Movie>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.movies_fragment, container, false);

        adapter = new MovieAdapter(getActivity());

        moviesGrid = (GridView) rootView.findViewById(R.id.moviesGrid);
        moviesGrid.setAdapter(adapter);

        moviesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                Movie movie = (Movie) adapter.getItem(position);
                activity.onMovieSelected(movie);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {

        super.onStart();
        updateMovies();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (MovieInterface) activity;
    }

    public void updateMovies() {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String moviePref = sharedPrefs.getString(getString(R.string.pref_movie_key),
                getString(R.string.pref_movie_default));

        MovieDetailFragment.favMovies = sharedPrefs.getStringSet("favourites", null);

        //Log.d("Hello", moviePref);

        if (moviePref.equals("favourites")) {

            if (MovieDetailFragment.favMovies != null) {

                //Log.d("fav", MovieDetailFragment.favMovies.size() + "");

                try {
                    movies = getFavouritesFromJson(MovieDetailFragment.favMovies.toString());
                    adapter.setData(movies);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {

            GetMoviesTask getMovies = new GetMoviesTask();
            getMovies.execute(moviePref);
        }
    }

    private List<Movie> getFavouritesFromJson(String moviesJsonStr)
            throws JSONException {

        List<Movie> movies = new ArrayList<Movie>();

        // These are the names of the JSON objects that need to be extracted.
        final String POSTER_PATH = "poster_path";
        final String OVERVIEW = "overview";
        final String RELEASE_DATE = "release_date";
        final String ORIGINAL_TITLE = "original_title";
        final String VOTE_AVERAGE = "vote_average";
        final String ID = "id";

        JSONArray resultsArray = new JSONArray(moviesJsonStr);

        //Log.d("favs", resultsArray.length() + "");

        for (int i = 0; i < resultsArray.length(); i++) {

            //Log.d("favs", "Herreeeeeeeeeeeeeee");

            Movie m = new Movie();

            JSONObject movie = resultsArray.getJSONObject(i);

            m.setPoster(movie.getString(POSTER_PATH));
            m.setOriginalTitle(movie.getString(ORIGINAL_TITLE));
            m.setOverview(movie.getString(OVERVIEW));
            m.setReleaseDate(movie.getString(RELEASE_DATE));
            m.setVoteAverage(movie.getString(VOTE_AVERAGE));
            m.setId(movie.getString(ID));

            //Log.d("FavMovieTitle", "Title of movie #" + (i + 1) + " = " + movie.getString(ORIGINAL_TITLE));

            movies.add(m);
        }

        return movies;
    }

    public class GetMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        public String MOVIE_API_KEY = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {

            //adapter.clearAll();
            //Log.d("Debugging", "Size after task = " + movies.size());
            adapter.setData(movies);
        }

        @Override
        protected List<Movie> doInBackground(String... strings) {

            List<Movie> moviesData = null;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;

            try {
                // Construct the URL for the MOVIE API query

                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/movie/" + strings[0] + "?";

                final String APP_KEY = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(APP_KEY, MOVIE_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                moviesJsonStr = buffer.toString();

                //Log.d("Movies in JSON", "JSON = " + moviesJsonStr);

                moviesData = getMoviesDataFromJson(moviesJsonStr);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {

                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            return moviesData;
        }
    }

    private List<Movie> getMoviesDataFromJson(String moviesJsonStr)
            throws JSONException {

        List<Movie> movies = new ArrayList<Movie>();

        // These are the names of the JSON objects that need to be extracted.
        final String RESULTS = "results";
        final String POSTER_PATH = "poster_path";
        final String OVERVIEW = "overview";
        final String RELEASE_DATE = "release_date";
        final String ORIGINAL_TITLE = "original_title";
        final String VOTE_AVERAGE = "vote_average";
        final String ID = "id";

        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray resultsArray = moviesJson.getJSONArray(RESULTS);

        for (int i = 0; i < resultsArray.length(); i++) {

            Movie m = new Movie();

            JSONObject movie = resultsArray.getJSONObject(i);

            m.setPoster(movie.getString(POSTER_PATH));
            m.setOriginalTitle(movie.getString(ORIGINAL_TITLE));
            m.setOverview(movie.getString(OVERVIEW));
            m.setReleaseDate(movie.getString(RELEASE_DATE));
            m.setVoteAverage(movie.getString(VOTE_AVERAGE));
            m.setId(movie.getString(ID));

            //Log.d("MovieTitle", "Title of movie #" + (i + 1) + " = " + movie.getString(ORIGINAL_TITLE));

            movies.add(m);
        }

        return movies;
    }
}
