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
import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import okhttp3.Headers;

public class TweetDetailActivity extends AppCompatActivity {
    final int CORNER_RADIUS = 20;
    ImageView detailProfileImage;
    TextView detailBody;
    TextView detailName;
    TextView detailScreenName;
    ImageView detailMedia;
    TextView detailTimeStamp;

    ImageView detailLikeButton;
    ImageView detailRetweetButton;
    ImageView detailReplyButton;

    Context context;
    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        detailName = findViewById(R.id.detailName);
        detailScreenName = findViewById(R.id.detailUserName);
        detailBody = findViewById(R.id.detailBody);
        detailTimeStamp = findViewById(R.id.detailTimeStamp);

        detailProfileImage = findViewById(R.id.detailProfileImg);
        detailMedia = findViewById(R.id.detailEmbImg);

        detailLikeButton = findViewById(R.id.detailLikeButton);
        detailRetweetButton = findViewById(R.id.detailRetweetButton);
        detailReplyButton = findViewById(R.id.detailReplyButton);

        context = this;
        client = TwitterApp.getRestClient(this.context);

        Tweet tweet = Parcels.unwrap(getIntent().getParcelableExtra("clickedTweet"));
        // validate tweet object?

        detailName.setText(tweet.user.name);
        detailScreenName.setText(String.format("@%s",tweet.user.screenName));
        detailBody.setText(tweet.body);
        detailTimeStamp.setText(tweet.createdAt);

        Glide.with(context).load(tweet.user.profileImageUrl)
                .transform(new RoundedCorners(CORNER_RADIUS))
                .into(detailProfileImage);
//            Log.d("TweetsAdapter", "profile img url: " + tweet.user.profileImageUrl);
        if (tweet.imgUrl != null){
            Glide.with(context).load(tweet.imgUrl)
                    .transform(new RoundedCorners(CORNER_RADIUS))
//                    .centerCrop()
                    .into(detailMedia);
//                Log.d("TweetsAdapter", "non null imgurl: " + tweet.imgUrl);
        } else {
            detailMedia.setVisibility(android.view.View.GONE);
        }

        detailLikeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.likeTweet(tweet.id, !tweet.favorited, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        tweet.favorited = !tweet.favorited;
                        detailLikeButton.setImageResource(tweet.favorited ?
                                R.drawable.ic_vector_heart :
                                R.drawable.ic_vector_heart_stroke);
                        Log.d("TweetsAdapter", "liked the tweet?");
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e("TweetsAdapter",
                                String.format("failed to %s tweet: %s",
                                        tweet.favorited ? "unlike" : "like", response),
                                throwable);
                    }
                });
            }
        });

        detailRetweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.retweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.d("TweetsAdapter", "successfully retweeted");
                        // notify data set changed?
//                        notifyItemInserted(0);
                        detailRetweetButton.setImageResource(R.drawable.ic_vector_retweet);
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