package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.TweetDetailActivity;
import com.codepath.apps.restclienttemplate.TwitterApp;
import com.codepath.apps.restclienttemplate.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder>{

    // pass in context and list of tweets
    TwitterClient mClient;
    Context mContext;
    List<Tweet> mTweets;

    public TweetsAdapter(Context c, List<Tweet> ts){
        this.mContext = c;
        this.mTweets = ts;
        mClient =  TwitterApp.getRestClient(this.mContext);
    }

    // for each row inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get data at position
       Tweet tweet =  mTweets.get(position);

        // bind the tweet with the view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    // getRelativeTimeAgo from: https://gist.github.com/nesquena/f786232f5ef72f6e10a7
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);
        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + "m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + "h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + "d";
            }
        } catch (ParseException e) {
            Log.i("TweetsAdapter", "getRelativeTimeAgo failed");
            e.printStackTrace();
        }
        return "";
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        final int MEDIA_HEIGHT = (Resources.getSystem().getDisplayMetrics().heightPixels / 4);
        final int CORNER_RADIUS = 20;
        ImageView mProfileImage;
        TextView mTweetBody;
        TextView mName;
        TextView mScreenName;
        ImageView mTweetEmbMedia;
        TextView mRelativeTimeStamp;

        ImageView mLikeButton;
        ImageView mRetweetButton;
        ImageView mReplyButton;

        public ViewHolder(@NonNull View itemView){ // one tweet
            super(itemView);
            mProfileImage = itemView.findViewById(R.id.detailProfileImg);
            mTweetBody = itemView.findViewById(R.id.detailBody);
            mName = itemView.findViewById(R.id.detailName);
            mScreenName = itemView.findViewById(R.id.detailUserName);
            mTweetEmbMedia = itemView.findViewById(R.id.detailEmbImg);
            ViewGroup.LayoutParams tvMediaParams = mTweetEmbMedia.getLayoutParams();
            tvMediaParams.height = MEDIA_HEIGHT;
            mTweetEmbMedia.setLayoutParams(tvMediaParams);
            mRelativeTimeStamp = itemView.findViewById(R.id.detailTimeStamp);

            mLikeButton = itemView.findViewById(R.id.detailLikeButton);
            mRetweetButton = itemView.findViewById(R.id.detailRetweetButton);
            mReplyButton = itemView.findViewById(R.id.detailReplyButton);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
//                    Log.d("TweetsAdapter", "clicked..." + Integer.toString(pos));
                    // find the tweet
                    if (pos >= 0 && pos < mTweets.size()){
                        // start new activity
                        Intent i = new Intent(mContext, TweetDetailActivity.class);
                        i.putExtra("clickedTweet", Parcels.wrap(mTweets.get(pos)));
                        mContext.startActivity(i);
                    }
                }
            });
        }

        public void bind(Tweet tweet) {
            mTweetBody.setText(tweet.mBody);
            mName.setText(tweet.mUser.mName);
            mScreenName.setText(String.format("@%s", tweet.mUser.mScreenName));

            mRelativeTimeStamp.setText(getRelativeTimeAgo(tweet.mCreatedAt));
            Glide.with(mContext).load(tweet.mUser.mProfileImageUrl)
                    .transform(new RoundedCorners(CORNER_RADIUS))
                    .into(mProfileImage);
//            Log.d("TweetsAdapter", "profile img url: " + tweet.user.profileImageUrl);
            if (tweet.mEmbedImgUrl != null){
                Glide.with(mContext).load(tweet.mEmbedImgUrl)
                        .transform(new RoundedCorners(CORNER_RADIUS))
                        .centerCrop()
                        .into(mTweetEmbMedia);
//                Log.d("TweetsAdapter", "non null imgurl: " + tweet.imgUrl);
            } else {
                mTweetEmbMedia.setVisibility(android.view.View.GONE);
            }

            // TODO: define handlers separately since they are reused in TweetDetailActivity
            // maybe try to curry arguments?
            mLikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClient.likeTweet(tweet.mId, !tweet.mFavorited, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            tweet.mFavorited = !tweet.mFavorited;
                            mLikeButton.setImageResource(tweet.mFavorited ?
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

            mRetweetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClient.retweet(tweet.mId, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.d("TweetsAdapter", "successfully retweeted");
                            // notify data set changed?
                            notifyItemInserted(0);
                            mRetweetButton.setImageResource(R.drawable.ic_vector_retweet);
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

    public void clear() {
        mTweets.clear();
        this.notifyDataSetChanged();
    }

    public void addAll(List<Tweet> list){
        mTweets.addAll(list);
        this.notifyDataSetChanged();
    }
}
