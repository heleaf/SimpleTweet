package com.codepath.apps.restclienttemplate.adapters;

import android.content.Context;
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
import com.codepath.apps.restclienttemplate.models.Tweet;

import java.util.List;

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

    // define a viewholder
    public class ViewHolder extends RecyclerView.ViewHolder {
        final int MEDIA_HEIGHT = (Resources.getSystem().getDisplayMetrics().heightPixels / 4);
        final int CORNER_RADIUS = 20;
        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        ImageView tvMedia;
        public ViewHolder(@NonNull View itemView){ // one tweet
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvMedia = itemView.findViewById(R.id.tvEmbedImg);
            ViewGroup.LayoutParams tvMediaParams = tvMedia.getLayoutParams();
            tvMediaParams.height = MEDIA_HEIGHT;
            tvMedia.setLayoutParams(tvMediaParams);
        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvScreenName.setText(tweet.user.screenName);
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
