package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

/* TODO: implement onclick?
* like, retweet, and reply?
* client.likeTweet(....)
* client.retweet(....)
* client.replyTweet(....)
* */


public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;
    private SwipeRefreshLayout swipeContainer;

    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
//    ImageView logOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

//        getSupportActionBar().setLogo(R.drawable.ic_launcher_twitter);
//        getSupportActionBar().setDisplayUseLogoEnabled(true);
//        getSupportActionBar().setIcon(R.drawable.ic_launcher_twitter);

        // swipe container view
        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });

        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        client =  TwitterApp.getRestClient(this);

        // find the recycler view
        rvTweets = findViewById(R.id.rvTweets);

        // initialize the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        // recycler view setup: layout manager and the adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);

//        logOut = findViewById(R.id.logOutButton);
//        logOut.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onLogoutButton();
//            }
//        });


        populateHomeTimeline();
    }

    private void fetchTimelineAsync(int page) {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                // clear out old items
                Log.d(TAG, "fetchTimelineAsync adapter tweets: " + adapter.getItemCount());
                adapter.clear();

                // add new items to adapter
                populateHomeTimeline();

                // signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "fetchTimelineAsync failed: " + response, throwable);

            }
        });
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "populateHomeTimeline onSuccess " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> ts = Tweet.fromJsonArray(jsonArray);
                    tweets.addAll(ts);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "populateHomeTimeline Json exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "populateHomeTimeline onFailure " + response, throwable);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.compose){
//            Toast.makeText(this, "Compose", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ComposeActivity.class);

            // child activity gives back a submitted tweet if user submits
            startActivityForResult(intent, REQUEST_CODE);
            return true;
        }
        if (item.getItemId() == R.id.logOutFromMenu){
            onLogoutButton();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // get data from the intent (tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

            // update the recyclerview with new tweet
            // put new tweet at the top of model, then update adapter
            tweets.add(0, tweet);
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void onLogoutButton(){
        TwitterApp.getRestClient(this).clearAccessToken();

        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

}