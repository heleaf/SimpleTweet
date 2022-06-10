package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.codepath.apps.restclienttemplate.adapters.TweetsAdapter;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    private final int REQUEST_CODE = 20;

    private SwipeRefreshLayout mSwipeContainer;

    TwitterClient mClient;
    RecyclerView mRvTweets;
    List<Tweet> mTweets;
    TweetsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // initialize member variables

        // swipe container view
        mSwipeContainer = findViewById(R.id.swipeContainer);

        mClient =  TwitterApp.getRestClient(this);

        // find the recycler view
        mRvTweets = findViewById(R.id.rvTweets);

        // initialize the list of tweets and adapter
        mTweets = new ArrayList<>();
        mAdapter = new TweetsAdapter(this, mTweets);

//        rvTweets.setLayoutManager(new LinearLayoutManager(this));
//        rvTweets.setAdapter(adapter);
//        populateHomeTimeline();
    }

    @Override
    protected void onStart() {
        Log.d("TimelineActivity","starting");
        super.onStart();

        // populate variables

        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchTimelineAsync(0);
            }
        });
        mSwipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        // recycler view setup: layout manager and the adapter
        mRvTweets.setLayoutManager(new LinearLayoutManager(this));
        mRvTweets.setAdapter(mAdapter);

        populateHomeTimeline();
    }

    @Override
    protected void onResume() {
        Log.d("TimelineActivity", "resuming");
        super.onResume();

        // re-update timeline in case time has passed between pausing and resuming
        populateHomeTimeline();
    }

    @Override
    protected void onPause() {
        Log.d("TimelineActivity", "pausing");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("TimelineActivity", "stopping");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("TimelineActivity", "destroying");
        super.onDestroy();
    }


    private void fetchTimelineAsync(int page) {
        mClient.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                // clear out old items
                Log.d(TAG, "fetchTimelineAsync adapter tweets: " + mAdapter.getItemCount());
                mAdapter.clear();

                // add new items to adapter
                populateHomeTimeline();

                // signal refresh has finished
                mSwipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "fetchTimelineAsync failed: " + response, throwable);

            }
        });
    }

    private void populateHomeTimeline() {
        mClient.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "populateHomeTimeline onSuccess " + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> ts = Tweet.fromJsonArray(jsonArray);
                    mTweets.addAll(ts);
                    mAdapter.notifyDataSetChanged();
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
            mTweets.add(0, tweet);
            mAdapter.notifyItemInserted(0);
            mRvTweets.smoothScrollToPosition(0);
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