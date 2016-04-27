package com.example.mal_phase_1;

import android.app.ActionBar;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by MW on 3/24/2016.
 */
public class MovieAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<Movie> movies;

    public MovieAdapter(Context context) {

        this.context = context;
        this.inflater = LayoutInflater.from(context);
        movies = new ArrayList<>();
    }

    @Override
    public int getCount() {

        return movies.size();
    }

    public void setData(List<Movie> newMovies) {

        movies.clear();

        movies.addAll( newMovies );
        notifyDataSetChanged();
    }

    public void clearAll() {

        movies.clear();
        notifyDataSetChanged();
    }

    @Override
    public Object getItem(int i) {
        return movies.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ImageView movie;

        String baseUrl = "http://image.tmdb.org/t/p/w185/";

        if (view == null) {

            view = inflater.inflate(R.layout.movie_item, viewGroup, false);
            movie = (ImageView) view.findViewById(R.id.movie_poster);
            //Picasso.with(context).load(baseUrl + movies.get(i).getPoster()).into(movie);

        } else {

            movie = (ImageView) view;
        }

        Picasso.with(context).load(baseUrl + movies.get(i).getPoster()).into(movie);
        //movie.setLayoutParams(new GridView.LayoutParams(200, 250));
        movie.setScaleType(ImageView.ScaleType.FIT_XY);

        return movie;
    }
}
