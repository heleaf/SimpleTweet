package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ComposeActivity extends AppCompatActivity {

    // TODO: add android snackbar for error handling?

    public static final int MAX_TWEET_LENGTH = 140;
    public static final String TAG = "ComposeActivity";

    EditText mEtCompose;
    Button mButtonTweet;

    TwitterClient mClient;

    ComposeActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        mEtCompose = findViewById(R.id.etCompose);
        mButtonTweet = findViewById(R.id.buttonTweet);

        mClient = TwitterApp.getRestClient(this);

        activity = this;

        // add a click listener to the button
        mButtonTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tweet is empty or too long --> let user try again
                String tweetContent = mEtCompose.getText().toString();
                if (!isTweetValid(tweetContent, activity)) return;
                // make API call to twitter to publish the tweet

                // TODO: add support for the replied tweet to immediately appear on the timeline
                mClient.publishTweet(tweetContent, null,
                        TwitterClient.getPublishTweetHandler(activity));
            }
        });
    }

    public static boolean isTweetValid(String tweetContent, AppCompatActivity activity){
        if (tweetContent.isEmpty()){
            Toast.makeText(activity,
                    "Sorry, your tweet cannot be empty",
                    Toast.LENGTH_LONG).show();
            return false;
        } if (tweetContent.length() > MAX_TWEET_LENGTH){
            Toast.makeText(activity,
                    "Sorry, your tweet is too long",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }
}