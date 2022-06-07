package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeActivity extends AppCompatActivity {

    // TODO: add android snackbar for error handling?

    public static final int MAX_TWEET_LENGTH = 140;

    EditText etCompose;
    Button buttonTweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        etCompose = findViewById(R.id.etCompose);
        buttonTweet = findViewById(R.id.buttonTweet);

        // add a click listener to the button
        buttonTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // tweet is empty or too long --> let user try again
                String tweetContent = etCompose.getText().toString();
                if (tweetContent.isEmpty()){
//                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                } if (tweetContent.length() > MAX_TWEET_LENGTH){
//                    Toast.makeText(ComposeActivity.this, "Sorry, your tweet is too long", Toast.LENGTH_LONG).show();
                }
                // make API call to twitter to publish the tweet

//                Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_LONG).show();

            }
        });
    }
}