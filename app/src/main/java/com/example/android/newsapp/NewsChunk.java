package com.example.android.newsapp;

import android.support.v4.util.ArrayMap;

import java.util.ArrayList;

/**
 * Created by juan on 5/10/17.
 */

public class NewsChunk {
    private ArrayMap<String, Integer> mMetaData;
    private ArrayList<PieceOfNews> mNews;

    public NewsChunk(ArrayMap<String, Integer> metaData, ArrayList<PieceOfNews> news) {
        this.mMetaData = metaData;
        this.mNews = news;
    }

    public ArrayMap<String, Integer> getMetaData() {
        return mMetaData;
    }

    public ArrayList<PieceOfNews> getNews() {
        return mNews;
    }
}
