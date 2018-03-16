package com.ahmetroid.popularmovies.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ahmetroid.popularmovies.R;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class AboutActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Element versionElement = new Element();
        versionElement.setTitle("Version 1.1");

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.ic_themoviedb)
                .setDescription(getString(R.string.description))
                .addItem(versionElement)
                .addEmail("ahmetturk.pc@gmail.com")
                .addWebsite("http://www.ahmetroid.com/")
                .addPlayStore("com.ahmetroid.popularmovies")
                .addGitHub("ahmetturk")
                .create();

        setContentView(aboutPage);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }
}