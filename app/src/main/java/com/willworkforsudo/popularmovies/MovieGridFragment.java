package com.willworkforsudo.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

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
 * A placeholder fragment containing a simple view.
 */
public class MovieGridFragment extends Fragment {
    private static final String LOG_TAG = MovieGridFragment.class.getSimpleName();
    private MovieArrayAdapter movieArrayAdapter;
    private List<MovieInfo> movieList;

    public MovieGridFragment() {
        movieList = new ArrayList<MovieInfo>();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovieDetails();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieArrayAdapter = new MovieArrayAdapter(getContext(), R.layout.grid_item_movie, movieList);
        GridView gridView = (GridView) rootView.findViewById(R.id.movies_gridview);
        gridView.setAdapter(movieArrayAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast toast = Toast.makeText(getContext(),
                        movieArrayAdapter.getItem(position).getTitle(),
                        Toast.LENGTH_SHORT);
                toast.show();
                Intent showDetailsIntent = new Intent(getActivity(), MovieDetailActivity.class);
                showDetailsIntent.putExtra(MovieDetailActivityFragment.MOVIE_DETAILS,
                        (Parcelable) movieArrayAdapter.getItem(position));
                startActivity(showDetailsIntent);
            }
        });

        return rootView;
    }

    private void updateMovieDetails() {
        FetchMovieDetailsTask fetchMovieDetailsTask = new FetchMovieDetailsTask();
        String sortOrder = PreferenceManager.getDefaultSharedPreferences(getContext())
                .getString(getString(R.string.pref_sort_order_key),
                        getString(R.string.pref_sort_order_default));
        fetchMovieDetailsTask.execute(sortOrder);
    }


    public class FetchMovieDetailsTask extends AsyncTask<String, Void, List<MovieInfo>> {
        private final String LOG_TAG = FetchMovieDetailsTask.class.getSimpleName();

        @Override
        protected List<MovieInfo> doInBackground(String... params) {
            if (params.length == 0) {
                return null;
            }

            final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_PARAM = "sort_by";
            final String VOTE_COUNT = "vote_count.gte";
            final String API_PARAM = "api_key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_PARAM, params[0])
                    .appendQueryParameter(API_PARAM, getString(R.string.api_key))
                    .build();

            if (params[0].equals(getString(R.string.pref_sort_order_ratings))) {
                builtUri = Uri.parse(builtUri.toString()).buildUpon()
                        .appendQueryParameter(VOTE_COUNT, "100")
                        .build();
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String movieDetailsJSONString = null;

            try {
                // Construct the URL for the query from builtUri
                URL url = new URL(builtUri.toString());

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
                movieDetailsJSONString = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            try {
                return getMovieList(movieDetailsJSONString);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<MovieInfo> movieList) {
            if(movieList != null && movieArrayAdapter != null) {
                movieArrayAdapter.clear();
                movieArrayAdapter.addAll(movieList);
            }
        }

        private List<MovieInfo> getMovieList(String movieDetailsJsonString)
                throws JSONException {
            // The JSON Objects that need to be extracted
            final String TMDB_MOVIE_LIST = "results";
            final String TMDB_POSTER_PATH = "poster_path";
            final String TMDB_ORIGINAL_TITLE = "original_title";
            final String TMDB_SYNOPSIS = "overview";
            final String TMDB_USER_RATING = "vote_average";
            final String TMDB_RELEASE_DATE = "release_date";

            JSONObject movieDetailsJson = new JSONObject(movieDetailsJsonString);
            JSONArray movieJsonArray = movieDetailsJson.getJSONArray(TMDB_MOVIE_LIST);

            MovieInfo[] movieArray = new MovieInfo[movieJsonArray.length()];
            List<MovieInfo> movieList = new ArrayList<>();

            for (int i = 0; i < movieJsonArray.length(); i++) {
                JSONObject jsonObject = movieJsonArray.getJSONObject(i);
                String title = jsonObject.getString(TMDB_ORIGINAL_TITLE);
                String posterPath = jsonObject.getString(TMDB_POSTER_PATH);
                String synopsis = jsonObject.getString(TMDB_SYNOPSIS);
                Double userRating = jsonObject.getDouble(TMDB_USER_RATING);
                String releaseDate = jsonObject.getString(TMDB_RELEASE_DATE);

                MovieInfo movieInfo =
                        new MovieInfo(title, posterPath, synopsis, userRating, releaseDate);
                movieList.add(movieInfo);
                movieArray[i] = movieInfo;
            }

            return movieList;
            //return Arrays.asList(movieArray);
        }
    }
}
