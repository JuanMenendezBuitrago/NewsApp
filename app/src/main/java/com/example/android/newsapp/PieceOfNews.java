package com.example.android.newsapp;


import android.net.Uri;
import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static android.R.attr.capitalize;

/**
 * Created by juan on 27/09/17.
 */

public class PieceOfNews {
    /** Author **/
    private final String mAuthor;

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
     * @param id
     * @param title
     * @param author
     * @param datePublished
     * @param url
     * @param section
     * @param sectionId
     */
    public PieceOfNews(String id, String title, String author, String datePublished, String url, String section, String sectionId) {
        this.mId = id;
        this.mTitle = title;
        this.mAuthor = author;
        this.mDatePublished = datePublished;
        this.mUrl = url;
        this.mSection = section;
        this.mSectionId = sectionId;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        if (!TextUtils.isEmpty(mAuthor)) {
            return capitalizeFirst(mAuthor);
        }
        return "";
    }

    private String capitalizeFirst(String mAuthor) {
        String[] strArr = mAuthor.split(" ");
        StringBuffer res = new StringBuffer();
        for (String str : strArr) {
            char[] stringArray = str.trim().toCharArray();
            stringArray[0] = Character.toUpperCase(stringArray[0]);
            str = new String(stringArray);

            res.append(str).append(" ");
        }

        return res.toString().trim();
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
        SimpleDateFormat format = new SimpleDateFormat(QueryUtils.getStringResource(R.string.API_date_format));
        format.setTimeZone(TimeZone.getTimeZone(QueryUtils.getStringResource(R.string.gmt_timezone)));
        return format.parse(myStrDate);
    }

    public URL getUrlObject() throws MalformedURLException {
        return new URL(getUrlString());
    }

    @Override
    public boolean equals(Object object) {
        try {
            PieceOfNews pieceOfNews = (PieceOfNews)object;
            return mId.equals(pieceOfNews.getId());
        } catch (ClassCastException e) {
            return false;
        }
    }
}
