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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        mEtCompose = findViewById(R.id.etCompose);
        mButtonTweet = findViewById(R.id.buttonTweet);

        mClient = TwitterApp.getRestClient(this);

        // add a click listener to the button
        mButtonTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tweet is empty or too long --> let user try again
                String tweetContent = mEtCompose.getText().toString();
                if (tweetContent.isEmpty()){
                    Toast.makeText(ComposeActivity.this,
                            "Sorry, your tweet cannot be empty",
                            Toast.LENGTH_LONG).show();
                    return;
                } if (tweetContent.length() > MAX_TWEET_LENGTH){
                    Toast.makeText(ComposeActivity.this,
                            "Sorry, your tweet is too long",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // make API call to twitter to publish the tweet

//                Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_LONG).show();
                mClient.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish tweet");
                        // expect a tweet model for the json
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            Log.i(TAG, "Published tweet says: " + tweet.mBody);
                            Intent intent = new Intent();
                            intent.putExtra("tweet", Parcels.wrap(tweet));
                            setResult(RESULT_OK, intent);
                            finish(); // close activity and return to the parent
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish tweet", throwable);
                    }
                });



            }
        });
    }
}