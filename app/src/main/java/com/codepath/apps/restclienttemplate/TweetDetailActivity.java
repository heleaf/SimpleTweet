package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class TweetDetailActivity extends AppCompatActivity {
    final int CORNER_RADIUS = 20;
    public static final int MAX_TWEET_LENGTH = 140;
    public static final String TAG = "TweetDetailActivity";

    ImageView mDetailProfileImage;
    TextView mDetailBody;
    TextView mDetailName;
    TextView mDetailScreenName;
    ImageView mDetailMedia;
    TextView mDetailTimeStamp;

    ImageView mDetailLikeButton;
    ImageView mDetailRetweetButton;
    ImageView mDetailReplyButton;

    Context mContext;
    TwitterClient mClient;

    EditText mEtReplyCompose;
    TextInputLayout mEtReplyComposeContainer;
    Button mPublishTweetResponse;
    Boolean mIsReplying;

    TweetDetailActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        activity = this;

        mDetailName = findViewById(R.id.detailName);
        mDetailScreenName = findViewById(R.id.detailUserName);
        mDetailBody = findViewById(R.id.detailBody);
        mDetailTimeStamp = findViewById(R.id.detailTimeStamp);

        mDetailProfileImage = findViewById(R.id.detailProfileImg);
        mDetailMedia = findViewById(R.id.detailEmbImg);

        mDetailLikeButton = findViewById(R.id.detailLikeButton);
        mDetailRetweetButton = findViewById(R.id.detailRetweetButton);
        mDetailReplyButton = findViewById(R.id.detailReplyButton);

        mContext = this;
        mClient = TwitterApp.getRestClient(this.mContext);

        Tweet tweet = Parcels.unwrap(getIntent().getParcelableExtra("clickedTweet"));
        // validate tweet object?

        mDetailName.setText(tweet.mUser.mName);
        mDetailScreenName.setText(String.format("@%s",tweet.mUser.mScreenName));
        mDetailBody.setText(tweet.mBody);
        mDetailTimeStamp.setText(tweet.mCreatedAt);

        Glide.with(mContext).load(tweet.mUser.mProfileImageUrl)
                .transform(new RoundedCorners(CORNER_RADIUS))
                .into(mDetailProfileImage);
        if (tweet.mEmbedImgUrl != null){
            Glide.with(mContext).load(tweet.mEmbedImgUrl)
                    .transform(new RoundedCorners(CORNER_RADIUS))
                    .fitCenter()
                    .into(mDetailMedia);
        } else {
            mDetailMedia.setVisibility(android.view.View.GONE);
        }

        mDetailLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClient.likeTweet(tweet.mId, !tweet.mFavorited,
                        mClient.getLikeTweetHandler(tweet, mDetailLikeButton));
            }
        });

        mDetailRetweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClient.retweet(tweet.mId,
                        mClient.getRetweetHandler(mDetailRetweetButton, null));
            }
        });

        // replying functionality
        mEtReplyCompose = findViewById(R.id.etReplyTweet);
        mEtReplyComposeContainer = findViewById(R.id.etReplyTweetLayout);
        mPublishTweetResponse = findViewById(R.id.replyTweetButton);

        mIsReplying = getIntent().getBooleanExtra("isReplying", false);
        if (mIsReplying){
            mEtReplyComposeContainer.setVisibility(View.VISIBLE);
            mPublishTweetResponse.setVisibility(View.VISIBLE);
            mEtReplyCompose.setText(String.format("@%s ", tweet.mUser.mScreenName));
        }

        //this is the envelope button
        mDetailReplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open the replying view
                if (!mIsReplying){
                    mEtReplyComposeContainer.setVisibility(View.VISIBLE);
                    mPublishTweetResponse.setVisibility(View.VISIBLE);
                    mEtReplyCompose.setText(String.format("@%s ", tweet.mUser.mScreenName));
                    mIsReplying = true;
                }
                // otherwise leave as is
            }
        });

        // this is the actual "tweet" button
        mPublishTweetResponse.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String tweetContent = mEtReplyCompose.getText().toString();

                // tweet is empty or too long --> let user try again
                if (!ComposeActivity.isTweetValid(tweetContent, activity)) return;

                // make API call to twitter to publish the tweet
                mClient.publishTweet(tweetContent, tweet.mId,
                        TwitterClient.getPublishTweetHandler(activity));
            }
        });



    }

}