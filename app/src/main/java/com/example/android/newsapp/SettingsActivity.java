package com.example.android.newsapp;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Activity that takes care of the settings screen
 */

public class SettingsActivity extends AppCompatActivity {

    public static class NewsPreferencesFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

//        setActivityToolbar();
    }

    /**
     * Enable Toolbar that replaces  ActionBar.
     */
//    private void setActivityToolbar() {
//        Toolbar myToolbar = (Toolbar)findViewById(R.id.my_toolbar);
//        setSupportActionBar(myToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//    }

}
