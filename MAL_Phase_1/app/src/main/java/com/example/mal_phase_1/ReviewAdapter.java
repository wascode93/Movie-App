package com.example.mal_phase_1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Waseem on 4/24/2016.
 */
public class ReviewAdapter extends ArrayAdapter<Review> {

    List<Review> reviews;
    Context context;
    LayoutInflater inflater;

    public ReviewAdapter(Context context, List<Review> reviews) {

        super(context, 0, reviews);

        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.reviews = reviews;
    }

    public void add(List<Review> reviews){

        this.reviews.clear();

        this.reviews.addAll(reviews);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return reviews.size();
    }

    @Override
    public Review getItem(int position) {
        return reviews.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View rootView = inflater.inflate(R.layout.review_list_item, parent, false);

        TextView reviewAuthor = (TextView) rootView.findViewById(R.id.review_author);
        TextView reviewContent = (TextView) rootView.findViewById(R.id.review_content);

        reviewAuthor.setText(reviews.get(position).getAuthor());
        reviewContent.setText(reviews.get(position).getContent());

        return rootView;
    }
}
