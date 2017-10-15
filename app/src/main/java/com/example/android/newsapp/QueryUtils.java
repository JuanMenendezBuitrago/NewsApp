package com.example.android.newsapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.util.ArrayMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static android.R.attr.author;


/**
 * QueryUtils holds static methods that are used to make API queries and process the result.
 */
public final class QueryUtils {

    /** log tag for debugging **/
    private static final String LOG_TAG = QueryUtils.class.getName();

    /**
     * private constructor.
     */
    private QueryUtils() {
    }

    /**
     * Check connection status.
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Query the Google books API and return the list of books.
     */
    public static NewsChunk fetchNewsData(String requestUrl) throws JSONException, IOException {

        // Create URL object
        URL url = new URL(requestUrl);
        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = makeHttpRequest(url);
        // Extract books from JSON
        NewsChunk chunk = extractNewsFromJson(jsonResponse);

        return chunk;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        // If the URL is null, then return early.
        String jsonResponse = "";
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            // build connection and connect
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod(getStringResource(R.string.http_method_get));
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and return the response
            // otherwise throw IOException.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                throw new IOException(getStringResource(R.string.error_fetching_data));
            }
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
            if (inputStream != null) inputStream.close();
        }

        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        // instantiate string builder
        StringBuilder output = new StringBuilder();

        // compose string
        if (inputStream != null) {
            // instantiate stream reader for utf-8 charset
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName(getStringResource(R.string.charset_utf_8)));
            // instantiate buffered reader from stream reader
            BufferedReader reader = new BufferedReader(inputStreamReader);

            // read line by line
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link PieceOfNews} objects that has been built up from
     * parsing a JSON response.
     */
    public static NewsChunk extractNewsFromJson(String resultJSON) throws JSONException{

        // Create an empty ArrayList that we can start adding books to
        ArrayList<PieceOfNews> news = new ArrayList<>();

        JSONObject reader = new JSONObject(resultJSON).getJSONObject(getStringResource(R.string.json_object_response));
        if (Integer.valueOf(reader.getInt(getStringResource(R.string.json_int_total))) == 0) {
            return null;
        }

        ArrayMap<String, Integer> metaData = new ArrayMap<>();
        metaData.put(
                getStringResource(R.string.json_int_total),
                reader.getInt(getStringResource(R.string.json_int_total)));
        metaData.put(
                getStringResource(R.string.json_int_index),
                reader.getInt(getStringResource(R.string.json_int_index)));
        metaData.put(
                getStringResource(R.string.json_int_page_size),
                reader.getInt(getStringResource(R.string.json_int_page_size)));
        metaData.put(
                getStringResource(R.string.json_int_current_page),
                reader.getInt(getStringResource(R.string.json_int_current_page)));
        metaData.put(
                getStringResource(R.string.json_int_pages),
                reader.getInt(getStringResource(R.string.json_int_pages)));

        JSONArray newsJSON = reader.getJSONArray(getStringResource(R.string.json_array_results));
        for (int i=0; i<newsJSON.length(); i++) {
            // grab the book JSON object
            JSONObject pieceOfNews = newsJSON.getJSONObject(i);

            // get id
            String id = pieceOfNews.getString(getStringResource(R.string.piece_of_news_id));
            // get title
            String title = pieceOfNews.getString(getStringResource(R.string.piece_of_news_web_title));
            // get link URL
            String url = pieceOfNews.getString(getStringResource(R.string.piece_of_news_web_url));
            // get date
            String date = pieceOfNews.getString(getStringResource(R.string.piece_of_news_web_publication_date));
            // get section
            String sectionName = pieceOfNews.getString(getStringResource(R.string.piece_of_news_section_name));
            // get section id
            String sectionId = pieceOfNews.getString(getStringResource(R.string.piece_of_news_section_id));

            // get author
            String authorFullName = composeAuthor(pieceOfNews);

            // finally, add the book object to the list
            news.add(new PieceOfNews(id, title, authorFullName, date, url, sectionName, sectionId));
        }

        NewsChunk chunk = new NewsChunk(metaData, news);

        // Return the chunk
        return chunk;
    }

    private static String composeAuthor(JSONObject pieceOfNews) throws JSONException {
        String authorFullName = "";
        JSONObject author = pieceOfNews.getJSONArray(getStringResource(R.string.piece_of_news_tags)).optJSONObject(0);
        if (author != null) {
            authorFullName = author.getString(getStringResource(R.string.piece_of_news_first_name)) + " " + author.getString(getStringResource(R.string.piece_of_news_last_name));
            authorFullName = authorFullName.trim();
        }
        return authorFullName;
    }

    public static String getStringResource(int resourceId) {
        return NewsApp.getAppContext().getResources().getString(resourceId);
    }
}