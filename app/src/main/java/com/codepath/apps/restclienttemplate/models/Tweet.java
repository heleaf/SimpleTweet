package com.codepath.apps.restclienttemplate.models;

import android.text.format.DateUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
public class Tweet {

    public String body;
    public String createdAt;
    public User user;
    public String imgUrl;
    public boolean favorited;
    public boolean retweeted;
    public String id;

    public Tweet(){} // empty constructor needed by parceler library

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        tweet.id = jsonObject.getString("id_str");
        JSONObject entities = jsonObject.getJSONObject("entities");
        if (entities.has("media")){
            JSONObject img = entities.getJSONArray("media").getJSONObject(0);
            tweet.imgUrl = img.getString("media_url_https");
        } else tweet.imgUrl = null;
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
