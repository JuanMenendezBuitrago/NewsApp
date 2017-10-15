package com.example.android.newsapp;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.R.attr.data;
import static android.R.attr.id;

public class NewsApp extends AppCompatActivity implements LoaderManager.LoaderCallbacks<AsyncTaskResult<PieceOfNews>>{

    private static Context context;

    /** tag for debugging **/
    private static final String LOG_TAG = NewsApp.class.getName();

    /** loader id  **/
    private static final int NEWS_LOADER_ID = 1;

    /** API URL to retrieve news from **/
    private static final String THE_GUARDIAN_API_CONTENT_URL = "https://content.guardianapis.com/search";

    /** API key **/
    // TODO: move to a safer place
    private static final String THE_GUARDIAN_API_KEY = "b09ba2b4-a362-416b-afb1-b0c316045fc2";

    /** news adapter **/
    private ArrayAdapter<PieceOfNews> mNewsAdapter;

    /** Text view that holds the "empty list" message **/
    private TextView mEmptyListTV;

    /** Progress bar view **/
    private ProgressBar mSpinnerPB;

    /** Search button **/
    private Button mSearchB;

    /** Sea rch terms **/
    private String mQuery;

    /** Swipe refresh layout **/
    private SwipeRefreshLayout mSwipeRefreshLayout;

    /** News list view **/
    private ListView mNewsLV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_app);
        context = getApplicationContext();

        setActivityToolbar();
        setActivityProperties();
        configureListView();
        tryConnection();

        //////////////
        /// events ///
        //////////////

        mSearchB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide keyboard
                hideSoftKeyboard(NewsApp.this);

                mQuery = getSearchTerms();

                if (QueryUtils.isConnected(NewsApp.this)) {
                    mSpinnerPB.setVisibility(View.VISIBLE);
                    mNewsAdapter.clear();
                    mEmptyListTV.setText(R.string.empty_string);

                    getLoaderManager().restartLoader(NEWS_LOADER_ID, null, NewsApp.this);
                } else {
                    mSpinnerPB.setVisibility(View.GONE);
                    mEmptyListTV.setText(R.string.no_connection);
                }
            }
        });

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLoaderManager().restartLoader(NEWS_LOADER_ID, null, NewsApp.this);
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        mNewsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current book that was clicked on
                PieceOfNews currentPieceOfNews = mNewsAdapter.getItem(position);

                // Open website with book url
                Uri pieceOfNewsUri = Uri.parse(currentPieceOfNews.getUrlString());
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, pieceOfNewsUri);
                startActivity(websiteIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                // User chose the "Settings" item, show the app settings UI...
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;

            case R.id.menu_refresh:
                // Signal SwipeRefreshLayout to start the progress indicator
                mSwipeRefreshLayout.setRefreshing(true);
                if (tryConnection())
                    getLoaderManager().restartLoader(NEWS_LOADER_ID, null, NewsApp.this);
                mSwipeRefreshLayout.setRefreshing(false);

                return true;

            default:
                Log.i(LOG_TAG, getString(R.string.item_clicked_unknown));
                return super.onOptionsItemSelected(item);
        }
    }



    /////////////////////////////////
    /// Loader overridden methods ///
    /////////////////////////////////

    @Override
    public Loader<AsyncTaskResult<PieceOfNews>> onCreateLoader(int i, Bundle bundle) {
        Uri.Builder uriBuilder = getUriBuilder();

        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<PieceOfNews>> loader, AsyncTaskResult<PieceOfNews> data) {
        showResult(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mNewsAdapter.clear();
    }

    ///////////////////////
    /// utility methods ///
    ///////////////////////

    /**
     * Show the results on screen.
     * @param data
     */
    private void showResult(AsyncTaskResult<PieceOfNews> data) {
        mSpinnerPB.setVisibility(View.GONE);

        // If there is a valid list of {@link Book}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (data.getResult() != null && data.getResult().size() > 0) {
            mEmptyListTV.setText("");
            insertNews(data);
        } else if (data.getResult() == null && data.getException() == null) {
            mEmptyListTV.setText(R.string.no_news_found);
        } else if (data.getException() != null){
            mEmptyListTV.setText(data.getException().getMessage());
        }
    }

    /**
     * Insert new in adapter or show toast with message.
     * @param data
     */
    private void insertNews(AsyncTaskResult<PieceOfNews> data) {
        if (mNewsAdapter.getCount() == 0)
            mNewsAdapter.addAll(data.getResult());
        else if (!mNewsAdapter.getItem(0).getId().equals(data.getResult().get(0).getId()))
            insertNewNews(data);
        else
            Toast.makeText(this, R.string.no_more_news, Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * @param data
     */
    private void insertNewNews(AsyncTaskResult<PieceOfNews> data) {
        ArrayList<PieceOfNews> news = data.getResult();

        int size = news.size();
        for (int i = size-1; i >= 0; i--) {
            if (mNewsAdapter.getPosition(news.get(i)) >= 0)
                continue;
            mNewsAdapter.insert(news.get(i),0);
        }
    }

    /**
     * Compose URI builder according to the preferences and search term.
     * @return Builder
     */
    private Uri.Builder getUriBuilder() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // format today's date
        DateFormat df = new DateFormat();
        String formattedDate = (String)(df.format(getString(R.string.date_format), new Date()));

        // compose builder
        Uri.Builder uriBuilder = Uri.parse(THE_GUARDIAN_API_CONTENT_URL).buildUpon();
        uriBuilder.appendQueryParameter(getString(R.string.query_string_api_key), THE_GUARDIAN_API_KEY);
        uriBuilder.appendQueryParameter(getString(R.string.query_string_show_tags), "contributor");
        uriBuilder.appendQueryParameter(getString(R.string.query_string_page), "1");
        uriBuilder.appendQueryParameter(getString(R.string.query_string_page_size),
                sharedPrefs.getString(
                        getString(R.string.settings_page_size_key),
                        getString(R.string.settings_page_size_default)
                )
        );
        uriBuilder.appendQueryParameter(getString(R.string.query_string_order_by),
                sharedPrefs.getString(
                        getString(R.string.settings_order_by_key),
                        getString(R.string.settings_order_by_default)
                )
        );

        String q = getSearchTerms();

        if (!TextUtils.isEmpty(q))
            uriBuilder.appendQueryParameter(getString(R.string.query_string_search_terms), q);

        String section = sharedPrefs.getString(
                getString(R.string.settings_section_key),
                getString(R.string.settings_section_default)
        );

        if (!TextUtils.isEmpty(section))
            uriBuilder.appendQueryParameter(getString(R.string.query_string_section), section);


        return uriBuilder;
    }

    /**
     * Get text inside the search box
     * @return
     */
    private String getSearchTerms() {
        EditText queryEditText = findViewById(R.id.edit_search_terms);
        return queryEditText.getText().toString();
    }

    /**
     * Enable Toolbar that replaces  ActionBar.
     */
    private void setActivityToolbar() {
        Toolbar myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    /**
     * Set adapter and empty view for the ListView.
     */
    private void configureListView() {
        ListView newsList = findViewById(R.id.news_list);
        newsList.setEmptyView(mEmptyListTV);
        newsList.setAdapter(mNewsAdapter);
    }

    /**
     * Set activity's properties for later use.
     */
    private void setActivityProperties() {
        mSpinnerPB   = findViewById(R.id.loading_spinner);
        mSearchB     = findViewById(R.id.btn_search);
        mEmptyListTV = findViewById(R.id.empty_list_view);
        mNewsLV      = findViewById(R.id.news_list);
        mNewsAdapter = new NewsAdapter(this, new ArrayList<PieceOfNews>());
        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);
    }

    /**
     * Try to establish connection and initialize loader
     * and show error message in case of failure.
     */
    private boolean tryConnection() {
        if (QueryUtils.isConnected(this)) {
            // check for loader to prevent from fetching data on fresh start
            if (getLoaderManager().getLoader(NEWS_LOADER_ID) != null) {
                getLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
            }
            return true;
        }

        mSpinnerPB.setVisibility(View.GONE);
        mEmptyListTV.setText(R.string.no_connection);
        return false;
    }

    /**
     * Get application context
     * @return Context Main activity context.
     */
    public static Context getAppContext() {
        return context;
    }


    /**
     * Hide keyboard
     * @param activity
     */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
    }

}
