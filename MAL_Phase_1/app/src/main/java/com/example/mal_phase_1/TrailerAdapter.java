package com.example.mal_phase_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Waseem on 4/24/2016.
 */
public class TrailerAdapter extends ArrayAdapter<Trailer> {

    Context context;
    List<Trailer> trailers;
    LayoutInflater inflater;

    public TrailerAdapter(Context context, List<Trailer> trailers) {
        super(context, 0, trailers);

        this.trailers = trailers;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void add( List<Trailer> trailers ){

        this.trailers.clear();

        this.trailers.addAll(trailers);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return trailers.size();
    }

    @Override
    public Trailer getItem(int position) {
        return trailers.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rootView = inflater.inflate(R.layout.trailer_list_item, parent, false);

        TextView trailerName = (TextView) rootView.findViewById(R.id.trailer_name);

        trailerName.setText(trailers.get(position).getName());

        return rootView;
    }
}
