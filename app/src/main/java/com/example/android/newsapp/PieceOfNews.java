package com.example.android.newsapp;


import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by juan on 27/09/17.
 */

public class PieceOfNews {
    /** Piece of news title **/
    private String mTitle;

    /** Piece of news date **/
    private String mDatePublished;

    /** Piece of news url**/
    private String mUrl;

    /** Piece of news section name**/
    private String mSection;

    /** Piece of news section id **/
    private String mSectionId;

    /** Piece of news id**/
    private String mId;

    /**
     * Construct
     * @param mId
     * @param mTitle
     * @param mDatePublished
     * @param mUrl
     * @param mSection
     * @param mSectionId
     */
    public PieceOfNews(String mId, String mTitle, String mDatePublished, String mUrl, String mSection, String mSectionId) {
        this.mId = mId;
        this.mTitle = mTitle;
        this.mDatePublished = mDatePublished;
        this.mUrl = mUrl;
        this.mSection = mSection;
        this.mSectionId = mSectionId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDateString() {
        return mDatePublished;
    }

    public String getUrlString() {
        return mUrl;
    }

    public String getSection() {
        return mSection;
    }

    public String getSectionId() {
        return mSectionId;
    }

    public String getId() {
        return mId;
    }

    public Date getDate() throws ParseException {
        String myStrDate = getDateString();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.parse(myStrDate);
    }

    public URL getUrlObject() throws MalformedURLException {
        return new URL(getUrlString());
    }
}
