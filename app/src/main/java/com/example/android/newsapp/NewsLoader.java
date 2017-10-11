package com.example.android.newsapp;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;



/**
 * Book loader uses QueryUtils to fetch the books based on
 * an API request URL.
 */

class NewsLoader extends AsyncTaskLoader<AsyncTaskResult<PieceOfNews>> {
    /** Tag for log messages */
    private static final String LOG_TAG = NewsLoader.class.getName();

    /** Url for the query **/
    private String mUrl;

    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public AsyncTaskResult<PieceOfNews> loadInBackground() {
        if (mUrl == null)
            return null;

        try {
            ArrayList news = QueryUtils.fetchNewsData(mUrl).getNews();
            return new AsyncTaskResult<>(news, null);
        } catch (IOException | JSONException e) {
            return new AsyncTaskResult<>(null, e);
        }
    }
}
