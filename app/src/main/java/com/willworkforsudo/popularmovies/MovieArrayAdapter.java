package com.willworkforsudo.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by jackson on 12/12/15.
 */
public class MovieArrayAdapter extends ArrayAdapter<MovieInfo> {
    private final String LOG_TAG = MovieArrayAdapter.class.getSimpleName();
    private Context context;
    private List<MovieInfo> movieInfoList;
    private final String BASE_URL = "http://image.tmdb.org/t/p/";
    private final String IMAGE_SIZE = "w342";

    public MovieArrayAdapter(Context context, int resource, List<MovieInfo> movieInfoList) {
        super(context, resource, movieInfoList);
        this.context = context;
        this.movieInfoList = movieInfoList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView == null) {
            imageView = new ImageView(context);
        } else {
            imageView = (ImageView) convertView;
        }

        //imageView.setImageResource(mThumbIds[position]);
        Picasso.with(context).load(getImageURLString(position)).into(imageView);
        return imageView;
    }

    private String getImageURLString(int position) {
        return movieInfoList.get(position).getImageURLString();
    }
}
