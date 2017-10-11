package com.example.android.newsapp;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.resource;

/**
 * Created by juan on 27/09/17.
 */

public class NewsAdapter extends ArrayAdapter<PieceOfNews> {

    private static class ViewHolder {
        TextView pieceOfNewsTitle;
        TextView pieceOfNewsData;
    }

    private static final String LOG_TAG = NewsAdapter.class.getName();

    public NewsAdapter(Context context, ArrayList<PieceOfNews> news) {
        super(context, 0, news);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        PieceOfNews currentPieceOfNews = getItem(position);
        if (convertView == null) {
            // inflate view
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.piece_of_news_layout, parent, false);

            // create view holder
            viewHolder = new ViewHolder();
            viewHolder.pieceOfNewsTitle = convertView.findViewById(R.id.text_news_title);
            viewHolder.pieceOfNewsData = convertView.findViewById(R.id.text_news_data);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder)convertView.getTag();
        }

        // set the title
        TextView pieceOfNewsTitle = viewHolder.pieceOfNewsTitle;
        pieceOfNewsTitle.setText(currentPieceOfNews.getTitle());

        // set the date and section
        TextView pieceOfNewsData = viewHolder.pieceOfNewsData;
        DateFormat df = DateFormat.getDateTimeInstance();

        String date;
        try {
            date = df.format(currentPieceOfNews.getDate());
        } catch (ParseException e) {
            date = "parse error";
        }
        pieceOfNewsData.setText(date  + "\nin " + currentPieceOfNews.getSection());

        return convertView;
    }
}
