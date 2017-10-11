package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;

import static android.R.attr.data;

public class NewsApp extends AppCompatActivity implements LoaderManager.LoaderCallbacks<AsyncTaskResult<PieceOfNews>>{
    private static Context context;

    /** tag for debugging **/
    private static final String LOG_TAG = NewsApp.class.getName();

    /** loader id  **/
    private static final int NEWS_LOADER_ID = 1;

    /** API URL to retrieve news from **/
    private static final String THE_GUARDIAN_API_CONTENT_URL = "https://content.guardianapis.com/search";

    /** API URL to retrieve news tags from **/
    private static final String THE_GUARDIAN_API_TAGS_URL = "http://content.guardianapis.com/tags";

    /** API URL to retrieve sections from **/
    private static final String THE_GUARDIAN_API_SECTIONS_URL = "http://content.guardianapis.com/sections";

    /** API URL to retrieve news editions from **/
    private static final String THE_GUARDIAN_API_EDITIONS_URL = "http://content.guardianapis.com/editions";

    /** API key **/
    // TODO: move to a safer place
    private static final String THE_GUARDIAN_API_KEY = "b09ba2b4-a362-416b-afb1-b0c316045fc2";

    /** news adapter **/
    private ArrayAdapter<PieceOfNews> mNewsAdapter;

    /** Text view that holds the "empty list" message **/
    private TextView mEmptyListTV;

    private ProgressBar mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_app);

        this.context = getApplicationContext();
        mSpinner = findViewById(R.id.loading_spinner);

        // set toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // set adapter for the ListView
        ListView newsList = findViewById(R.id.news_list);
        mEmptyListTV = findViewById(R.id.empty_list_view);
        newsList.setEmptyView(mEmptyListTV);
        mNewsAdapter = new NewsAdapter(this, new ArrayList<PieceOfNews>());
        newsList.setAdapter(mNewsAdapter);


        // initialize connection
        if (QueryUtils.isConnected(this)) {
            getLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mSpinner.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyListTV.setText(R.string.no_connection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
//        inflater.inflate(R.menu.menu_search, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();
        if (id == R.id.action_settings) {
            // User chose the "Settings" item, show the app settings UI...
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        Log.i(LOG_TAG, "User clicked something that wasn't recognized");
        return super.onOptionsItemSelected(item);

    }


    ////////////////////////////////
    /// Loader overridden methods ///
    ////////////////////////////////

    @Override
    public Loader<AsyncTaskResult<PieceOfNews>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        Uri baseUri = Uri.parse(THE_GUARDIAN_API_CONTENT_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("api-key", THE_GUARDIAN_API_KEY);
        uriBuilder.appendQueryParameter("q", "");
        uriBuilder.appendQueryParameter("page", "1");
        uriBuilder.appendQueryParameter("page-size",
                sharedPrefs.getString(
                        getString(R.string.settings_page_size_key),
                        getString(R.string.settings_page_size_default)
                )
        );
        uriBuilder.appendQueryParameter("orderBy",
                sharedPrefs.getString(
                        getString(R.string.settings_order_by_key),
                        getString(R.string.settings_order_by_default)
                )
        );
        DateFormat df = new DateFormat();
        String formattedDate = (String)(df.format("yyyy-MM-dd", new Date()));
        uriBuilder.appendQueryParameter("from-date", formattedDate);

        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<PieceOfNews>> loader, AsyncTaskResult<PieceOfNews> data) {
        mSpinner.setVisibility(View.GONE);

        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data.getResult() != null && data.getResult().size() > 0) {
            mEmptyListTV.setText("");
            mNewsAdapter.addAll(data.getResult());
        } else if (data.getResult() == null && data.getException() == null) {
            // Set empty state text to display "No books found."
            mEmptyListTV.setText("No news found");
        } else if (data.getException() != null){
            mEmptyListTV.setText(data.getException().getMessage());
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {
        mNewsAdapter.clear();
    }

    /**
     * Get application context
     * @return
     */
    public static Context getAppContext() {
        return NewsApp.context;
    }

}
