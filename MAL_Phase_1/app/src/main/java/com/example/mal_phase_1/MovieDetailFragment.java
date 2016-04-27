package com.example.mal_phase_1;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Waseem on 3/25/2016.
 */
public class MovieDetailFragment extends Fragment {

    ListView trailersList;
    ListView reviewsList;

    TrailerAdapter trailerAdapter;
    ReviewAdapter reviewAdapter;

    Movie movie;

    public static Set<String> favMovies;

    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bundle extras = getArguments();
        movie = (Movie) extras.getSerializable("MovieExtra");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.movie_detail_fragment, container, false);

        final ImageView favouritesIcon = (ImageView) rootView.findViewById(R.id.favourites_icon);
        favouritesIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (favMovies == null)
                    favMovies = new HashSet<String>();

                try {

                    favMovies.add(movie.toJson().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //Log.d("here", favMovies.toString());

                PreferenceManager.getDefaultSharedPreferences(getActivity())
                        .edit()
                        .putStringSet("favourites", favMovies)
                        .commit();

                favouritesIcon.setImageDrawable(getResources().getDrawable(android.R.drawable.btn_star_big_on));

                Toast.makeText(getActivity(), "Added to favourites", Toast.LENGTH_SHORT).show();
            }
        });

        trailersList = (ListView) rootView.findViewById(R.id.trailers_list);
        reviewsList = (ListView) rootView.findViewById(R.id.reviews_list);

        trailerAdapter = new TrailerAdapter(getActivity(), new ArrayList<Trailer>());
        trailersList.setAdapter(trailerAdapter);

        reviewAdapter = new ReviewAdapter(getActivity(), new ArrayList<Review>());
        reviewsList.setAdapter(reviewAdapter);

        GetMovieTrailersTask getTrailers = new GetMovieTrailersTask();
        getTrailers.execute(movie.getId());

        GetMovieReviewsTask getReviews = new GetMovieReviewsTask();
        getReviews.execute(movie.getId());

        trailersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                        "http://www.youtube.com/watch?v=" + trailerAdapter.getItem(position).getKey()))
                );
            }
        });

        LinearLayout movieDetails = (LinearLayout) rootView.findViewById(R.id.movie_details);
        LinearLayout detailParent = (LinearLayout) movieDetails.findViewById(R.id.detail_parent);

        ImageView movieImage = (ImageView) movieDetails.findViewById(R.id.movie_image);

        TextView movieYear = (TextView) detailParent.findViewById(R.id.movie_year);
        TextView movieRating = (TextView) detailParent.findViewById(R.id.movie_rating);
        TextView movieOverview = (TextView) rootView.findViewById(R.id.movie_overview);
        TextView movieTitle = (TextView) rootView.findViewById(R.id.movie_name);

        Picasso.with(getActivity()).load("http://image.tmdb.org/t/p/w185/" + movie.getPoster()).into(movieImage);

        movieTitle.setText(movie.getOriginalTitle());
        movieYear.setText(movie.getReleaseDate());
        movieRating.setText(movie.getVoteAverage());
        movieOverview.setText(movie.getOverview());

        return rootView;
    }

    public class GetMovieTrailersTask extends AsyncTask<String, Void, List<Trailer>> {

        public String MOVIE_API_KEY = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<Trailer> result) {

            trailerAdapter.add(result);
        }

        @Override
        protected List<Trailer> doInBackground(String... strings) {

            List<Trailer> trailers = null;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String trailersJsonStr = null;

            try {
                // Construct the URL for the MOVIE API query

                final String TRAILER_BASE_URL = "http://api.themoviedb.org/3/movie/" + strings[0] + "/videos?";

                final String APP_KEY = "api_key";

                Uri builtUri = Uri.parse(TRAILER_BASE_URL).buildUpon()
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

                trailersJsonStr = buffer.toString();

                //Log.d("Movies in JSON", "JSON = " + trailersJsonStr);

                trailers = getTrailersFromJson(trailersJsonStr);

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

            return trailers;
        }

        private List<Trailer> getTrailersFromJson(String trailersJsonStr) throws JSONException {

            final String RESULTS = "results";
            final String KEY = "key";
            final String NAME = "name";

            List<Trailer> trailers = new ArrayList<Trailer>();

            JSONObject trailersJson = new JSONObject(trailersJsonStr);
            JSONArray resultsArray = trailersJson.getJSONArray(RESULTS);

            //Log.d("MovieTitle", "Size of json array = " + resultsArray.length());

            for (int i = 0; i < resultsArray.length(); i++) {

                Trailer t = new Trailer();

                JSONObject trailer = resultsArray.getJSONObject(i);

                if (trailer.getString("type").equals("Trailer")) {

                    t.setKey(trailer.getString(KEY));
                    t.setName(trailer.getString(NAME));

                    trailers.add(t);
                }

                //Log.d("MovieTitle", "Title of trailer #" + (i + 1) + " = " + t.getName());
            }

            return trailers;
        }
    }

    public class GetMovieReviewsTask extends AsyncTask<String, Void, List<Review>> {

        public String MOVIE_API_KEY = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<Review> result) {

            //Log.d("Debugging", "Size of trailers after task = " + result.size());

            reviewAdapter.add(result);
        }

        @Override
        protected List<Review> doInBackground(String... strings) {

            List<Review> reviews = null;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String reviewsJsonStr = null;

            try {
                // Construct the URL for the MOVIE API query

                final String REVIEW_BASE_URL = "http://api.themoviedb.org/3/movie/" + strings[0] + "/reviews?";

                final String APP_KEY = "api_key";

                Uri builtUri = Uri.parse(REVIEW_BASE_URL).buildUpon()
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

                reviewsJsonStr = buffer.toString();

                //Log.d("Movies in JSON", "JSON = " + reviewsJsonStr);

                reviews = getReviewsFromJson(reviewsJsonStr);

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

            return reviews;
        }

        private List<Review> getReviewsFromJson(String reviewsJsonStr) throws JSONException {

            final String RESULTS = "results";
            final String CONTENT = "content";
            final String AUTHOR = "author";

            List<Review> reviews = new ArrayList<Review>();

            JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
            JSONArray resultsArray = reviewsJson.getJSONArray(RESULTS);

            //Log.d("MovieTitle", "Size of json array = " + resultsArray.length());

            for (int i = 0; i < resultsArray.length(); i++) {

                Review r = new Review();

                JSONObject trailer = resultsArray.getJSONObject(i);

                //if ( trailer.getString("type").equals("Trailer") ) {

                r.setContent(trailer.getString(CONTENT));
                r.setAuthor(trailer.getString(AUTHOR));

                reviews.add(r);
                //}

                //Log.d("MovieTitle", "Title of review #" + (i + 1) + " = " + r.getAuthor());
            }

            return reviews;
        }
    }
}
