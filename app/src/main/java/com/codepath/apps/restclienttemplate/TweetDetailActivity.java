package com.codepath.apps.restclienttemplate;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

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
//            Log.d("TweetsAdapter", "profile img url: " + tweet.user.profileImageUrl);
        if (tweet.mEmbedImgUrl != null){
            Glide.with(mContext).load(tweet.mEmbedImgUrl)
                    .transform(new RoundedCorners(CORNER_RADIUS))
                    .fitCenter()
                    .into(mDetailMedia);
//                Log.d("TweetsAdapter", "non null imgurl: " + tweet.imgUrl);
        } else {
            mDetailMedia.setVisibility(android.view.View.GONE);
        }

        mDetailLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClient.likeTweet(tweet.mId, !tweet.mFavorited, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        tweet.mFavorited = !tweet.mFavorited;

                        // TODO: use selectors in XML instead, and add color styling
                        mDetailLikeButton.setImageResource(tweet.mFavorited ?
                                R.drawable.ic_vector_heart :
                                R.drawable.ic_vector_heart_stroke);
                        Log.d("TweetsAdapter", "liked the tweet?");
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e("TweetsAdapter",
                                String.format("failed to %s tweet: %s",
                                        tweet.mFavorited ? "unlike" : "like", response),
                                throwable);
                    }
                });
            }
        });

        mDetailRetweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClient.retweet(tweet.mId, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.d("TweetsAdapter", "successfully retweeted");
                        // TODO: use selectors in XML instead, and add color styling
                        mDetailRetweetButton.setImageResource(R.drawable.ic_vector_retweet);
                    }
                    @Override
                    public void onFailure(int statusCode, Headers headers,
                                          String response, Throwable throwable) {
                        Log.e("TweetsAdapter", "failed to retweet: " + response,
                                throwable);
                    }
                });
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
                // need to do the checks...

                // tweet is empty or too long --> let user try again
                String tweetContent = mEtReplyCompose.getText().toString();
                if (tweetContent.isEmpty()){
                    Toast.makeText(TweetDetailActivity.this,
                            "Sorry, your tweet cannot be empty",
                            Toast.LENGTH_LONG).show();
                    return;
                } if (tweetContent.length() > MAX_TWEET_LENGTH){
                    Toast.makeText(TweetDetailActivity.this,
                            "Sorry, your tweet is too long",
                            Toast.LENGTH_LONG).show();
                    return;
                }
                // make API call to twitter to publish the tweet

//                Toast.makeText(ComposeActivity.this, tweetContent, Toast.LENGTH_LONG).show();
                mClient.publishTweet(tweetContent, tweet.mId, new JsonHttpResponseHandler() {
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