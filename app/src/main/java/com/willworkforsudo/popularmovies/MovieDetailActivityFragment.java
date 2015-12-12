package com.willworkforsudo.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class MovieDetailActivityFragment extends Fragment {
    private final String LOG_TAG = MovieDetailActivityFragment.class.getSimpleName();
    public static String MOVIE_DETAILS = "movie details";

    public MovieDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_detail, container, false);
        Intent intent = getActivity().getIntent();
        MovieInfo movieInfo;
        if (intent != null && intent.hasExtra(MOVIE_DETAILS)) {
            movieInfo = (MovieInfo) intent.getParcelableExtra(MOVIE_DETAILS);
        }
        else {
            return rootView;
        }
        TextView titleView = (TextView) rootView.findViewById(R.id.details_title_textview);
        titleView.setText(movieInfo.getTitle());

        ImageView posterView = (ImageView) rootView.findViewById(R.id.details_poster_view);
        Picasso.with(getContext()).load(movieInfo.getImageURLString()).into(posterView);

        TextView synopsisView = (TextView) rootView.findViewById(R.id.details_synopsis_textview);
        synopsisView.setText(movieInfo.getSynopsis());

        TextView userRatingView =
                (TextView) rootView.findViewById(R.id.details_user_rating_textview);
        userRatingView.
                setText(getString(R.string.details_rating_label) + movieInfo.getUserRating());

        TextView releaseDateView =
                (TextView) rootView.findViewById(R.id.details_release_date_textview);
        releaseDateView.
                setText(getString(R.string.details_release_date_label) + movieInfo.getReleaseDate());


        return rootView;
    }
}
