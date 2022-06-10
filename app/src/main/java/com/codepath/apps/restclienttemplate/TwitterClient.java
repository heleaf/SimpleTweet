package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.RequestParams;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.codepath.oauth.OAuthBaseClient;
import com.github.scribejava.apis.TwitterApi;
import com.github.scribejava.core.builder.api.BaseApi;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/scribejava/scribejava/tree/master/scribejava-apis/src/main/java/com/github/scribejava/apis
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {
	public static final BaseApi REST_API_INSTANCE = TwitterApi.instance(); // Change this
	public static final String REST_URL = "https://api.twitter.com/1.1"; // Change this, base API URL
	public static final String REST_CONSUMER_KEY = BuildConfig.CONSUMER_KEY;       // Change this inside apikey.properties
	public static final String REST_CONSUMER_SECRET = BuildConfig.CONSUMER_SECRET; // Change this inside apikey.properties

	// Landing page to indicate the OAuth flow worked in case Chrome for Android 25+ blocks navigation back to the app.
	public static final String FALLBACK_URL = "https://codepath.github.io/android-rest-client-template/success.html";

	// See https://developer.chrome.com/multidevice/android/intents
	public static final String REST_CALLBACK_URL_TEMPLATE = "intent://%s#Intent;action=android.intent.action.VIEW;scheme=%s;package=%s;S.browser_fallback_url=%s;end";

	public TwitterClient(Context context) {
		super(context, REST_API_INSTANCE,
				REST_URL,
				REST_CONSUMER_KEY,
				REST_CONSUMER_SECRET,
				null,  // OAuth2 scope, null for OAuth1
				String.format(REST_CALLBACK_URL_TEMPLATE, context.getString(R.string.intent_host),
						context.getString(R.string.intent_scheme), context.getPackageName(), FALLBACK_URL));
	}
	// CHANGE THIS
	// DEFINE METHODS for different API endpoints here
	public void getHomeTimeline(JsonHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		// Can specify query string params directly or through RequestParams.
		RequestParams params = new RequestParams();
		params.put("count", "25");
		params.put("since_id", "1");
		client.get(apiUrl, params, handler);
	}

	/* 1. Define the endpoint URL with getApiUrl and pass a relative path to the endpoint
	 * 	  i.e getApiUrl("statuses/home_timeline.json");
	 * 2. Define the parameters to pass to the request (query or body)
	 *    i.e RequestParams params = new RequestParams("foo", "bar");
	 * 3. Define the request method and make a call to the client
	 *    i.e client.get(apiUrl, params, handler);
	 *    i.e client.post(apiUrl, params, handler);
	 */

	// write a new tweet or respond to someone else
	public void publishTweet(String tweetContent, String idInReplyTo, JsonHttpResponseHandler handler){
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		// must pass in non-null id and user to be considered a reply
		if (idInReplyTo == null){
			params.put("status", tweetContent);
			client.post(apiUrl, params, "", handler);
		}
		// add the in_reply_to_status_id otherwise
		params.put("status", tweetContent);
		params.put("in_reply_to_status_id", idInReplyTo);
		client.post(apiUrl, params, "", handler);
	}

	public static JsonHttpResponseHandler getPublishTweetHandler(AppCompatActivity activity){
		return new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Headers headers, JSON json) {
				Log.i("TwitterClient", "onSuccess to publish tweet");
				// expect a tweet model for the json
				try {
					Tweet tweet = Tweet.fromJson(json.jsonObject);
					Log.i("TwitterClient", "Published tweet says: " + tweet.mBody);
					Intent intent = new Intent();
					intent.putExtra("tweet", Parcels.wrap(tweet));
					activity.setResult(activity.RESULT_OK, intent);
					activity.finish(); // close activity and return to the parent
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
			@Override
			public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
				Log.e("TwitterClient", "onFailure to publish tweet", throwable);
			}
		};
	}

	// like or unlike tweet depending on setToLiked arg
	public void likeTweet(String tweetId, boolean setToLiked, JsonHttpResponseHandler handler){
		String apiUrl = setToLiked ? getApiUrl("favorites/create.json")
				: getApiUrl("favorites/destroy.json");
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.post(apiUrl, params, "", handler);
	}

	public JsonHttpResponseHandler getLikeTweetHandler(Tweet tweet, ImageView likeButton){
		return new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Headers headers, JSON json) {
				tweet.mFavorited = !tweet.mFavorited;
				// TODO: use selectors in XML instead, and add color styling
				likeButton.setImageResource(tweet.mFavorited ?
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
		};
	}

	// retweet
	public void retweet(String tweetId, JsonHttpResponseHandler handler){
		String apiUrl = getApiUrl(String.format("statuses/retweet/%s.json", tweetId));
		RequestParams params = new RequestParams();
		params.put("id", tweetId);
		client.post(apiUrl, params, "", handler);
	}

	public JsonHttpResponseHandler getRetweetHandler(ImageView retweetButton, TweetsAdapter adapter){
		return new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Headers headers, JSON json) {
				Log.d("TweetsAdapter", "successfully retweeted");
				// notify data set changed?
				if (adapter != null){
					adapter.notifyItemInserted(0);
				}
				retweetButton.setImageResource(R.drawable.ic_vector_retweet);
			}
			@Override
			public void onFailure(int statusCode, Headers headers,
								  String response, Throwable throwable) {
				Log.e("TweetsAdapter", "failed to retweet: " + response,
						throwable);

			}
		};
	}

}
