package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateUtils;
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
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    // pass in context and list of tweets
    Context context;
    List<Tweet> tweets;

    public TweetsAdapter(Context c, List<Tweet> ts){
        this.context = c;
        this.tweets = ts;
    }

    // for each row inflate the layout
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        return new ViewHolder(view);
    }

    // bind values based on the position of the element
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get data at position
       Tweet tweet =  tweets.get(position);

        // bind the tweet with the view holder
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    // taken from: https://gist.github.com/nesquena/f786232f5ef72f6e10a7
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

    // define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder {
        final int MEDIA_HEIGHT = (Resources.getSystem().getDisplayMetrics().heightPixels / 4);
        final int CORNER_RADIUS = 20;
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        ImageView tvMedia;
        TextView relativeTimeStamp;
        public ViewHolder(@NonNull View itemView){ // one tweet
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvMedia = itemView.findViewById(R.id.tvEmbedImg);
            ViewGroup.LayoutParams tvMediaParams = tvMedia.getLayoutParams();
            tvMediaParams.height = MEDIA_HEIGHT;
            tvMedia.setLayoutParams(tvMediaParams);
            relativeTimeStamp = itemView.findViewById(R.id.relativeTimeStamp);
        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
            relativeTimeStamp.setText(getRelativeTimeAgo(tweet.createdAt));
            Glide.with(context).load(tweet.user.profileImageUrl)
                    .transform(new RoundedCorners(CORNER_RADIUS))
                    .into(ivProfileImage);
            Log.d("TweetsAdapter", "profile img url: " + tweet.user.profileImageUrl);
            if (tweet.imgUrl != null){
                Glide.with(context).load(tweet.imgUrl)
                        .centerCrop()
                        .transform(new RoundedCorners(CORNER_RADIUS))
                        .into(tvMedia);
                Log.d("TweetsAdapter", "non null imgurl: " + tweet.imgUrl);
            } else {
                tvMedia.setVisibility(android.view.View.GONE);
            }
        }
    }

    public void clear() {
        tweets.clear();
        this.notifyDataSetChanged();
    }

    public void addAll(List<Tweet> list){
        tweets.addAll(list);
        this.notifyDataSetChanged();
    }
}
