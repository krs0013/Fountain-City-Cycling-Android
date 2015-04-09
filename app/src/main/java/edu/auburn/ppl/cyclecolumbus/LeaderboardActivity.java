package edu.auburn.ppl.cyclecolumbus;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by kennystreit on 4/7/15.
 */
public class LeaderboardActivity extends ListActivity {

    // Progress Dialog
    private ProgressDialog pDialog;

    SwipeRefreshLayout mSwipeRefreshLayout;
    ListAdapter adapter;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> leaderList;

    // url to get all products list
    private static String url_all_products = "http://FountainCityCycling.org/leaderboard_request/";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_LEADERS = "leaders";
    private static final String TAG_USER_ID = "user_id";
    private static final String TAG_SCORE = "score";

    // products JSONArray
    JSONArray products = null;

    //10.0.2.2 is the address used by the Android emulators to refer to the host address
    // change this to the IP of another host if required
    private static String apiCall = "http://FountainCityCycling.org/leaderboard_request/";
    private static String jsonResult = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard_layout);

        // Hashmap for ListView
        leaderList = new ArrayList<HashMap<String, String>>();

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        mSwipeRefreshLayout.setColorScheme(R.color.blue, R.color.green, R.color.new_orange);

        // Loading leaderboard in Background Thread
        new LeaderboardAsync().execute();

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });
    }

    //Pass the name to the JSON method and create a JSON object from the return
    public class LeaderboardAsync extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("KENNY", "In pre execute");
        }

        /**
         * getting All products from url
         */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tag", "leaderBoard"));
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_products, "GET", params);

            try {
                // Check your log cat for JSON reponse
                Log.d("KENNY", "JSON string: " + json.toString());

                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    products = json.getJSONArray(TAG_LEADERS);
                    // looping through All Products
                    for (int i = 0; i < products.length(); i++) {
                        JSONObject c = products.getJSONObject(i);

                        // Storing each json item in variable
                        String id = "Rider ID: " + c.getString(TAG_USER_ID);
                        String score = "Score: " + c.getString(TAG_SCORE);

                        // creating new HashMap
                        HashMap<String, String> map = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        map.put(TAG_USER_ID, id);
                        map.put(TAG_SCORE, score);

                        // adding HashList to ArrayList
                        leaderList.add(map);
                    }
                } else {
                    Log.d("KENNY", "It went into the else, so success WAS NOT 1!");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products
            //pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
                    adapter = new SimpleAdapter(
                            LeaderboardActivity.this, leaderList,
                            R.layout.listitem, new String[]{TAG_USER_ID,
                            TAG_SCORE},
                            new int[]{R.id.user_id, R.id.score});
                    // updating listview
                    setListAdapter(adapter);
                }
            });

        }
    }

    //The below passes the tag and the user name over to the JSON parser class
//    public JSONObject getLeaderboard(){
//        List<NameValuePair> params = new ArrayList<NameValuePair>();
//        params.add(new BasicNameValuePair("tag", "leaderBoard"));
//        JSONObject json = jsonParser.getJSONFromUrl(apiCall, params);
//        return json;
//    }

    // fake a network operation's delayed response
    // this is just for demonstration, not real code!
    private void refreshContent() {

        Toast.makeText(getApplicationContext(), "Refreshing...", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                leaderList.clear();
                new LeaderboardAsync().execute();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }
}
