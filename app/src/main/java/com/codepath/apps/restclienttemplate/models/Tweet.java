package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
public class Tweet {

    public String mBody;
    public String mCreatedAt;
    public User mUser;
    public String mEmbedImgUrl;
    public boolean mFavorited;
    public boolean mRetweeted;
    public String mId;

    public Tweet(){} // empty constructor needed by parceler library

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.mBody = jsonObject.getString("text");
        tweet.mCreatedAt = jsonObject.getString("created_at");
        tweet.mUser = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.mFavorited = jsonObject.getBoolean("favorited");
        tweet.mRetweeted = jsonObject.getBoolean("retweeted");
        tweet.mId = jsonObject.getString("id_str");
        JSONObject entities = jsonObject.getJSONObject("entities");
        if (entities.has("media")){
            JSONObject img = entities.getJSONArray("media").getJSONObject(0);
            tweet.mEmbedImgUrl = img.getString("media_url_https");
        } else tweet.mEmbedImgUrl = null;
//        Log.d("Tweet", "imgurl: " + tweet.imgUrl);
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++){
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }
}
