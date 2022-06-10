package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import okhttp3.Headers;

public class TweetDetailActivity extends AppCompatActivity {
    final int CORNER_RADIUS = 20;

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

    }
}