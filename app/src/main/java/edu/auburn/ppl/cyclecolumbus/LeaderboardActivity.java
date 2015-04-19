package edu.auburn.ppl.cyclecolumbus;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
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

    SwipeRefreshLayout mSwipeRefreshLayout;
    ListAdapter adapter;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();

    ArrayList<HashMap<String, String>> leaderList;

    // url to get leader list
    private static String url_leaderboard = "http://FountainCityCycling.org/leaderboard_request/";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_LEADERS = "leaders";
    private static final String TAG_USER_ID = "user_id";
    private static final String TAG_SCORE = "score";
    private static final String TAG_DEVICE = "device";

    private String myDeviceID = "";
    private String user_id = "";

    // products JSONArray
    JSONArray tempLeaders = null;

    //10.0.2.2 is the address used by the Android emulators to refer to the host address
    // change this to the IP of another host if required
    private static String apiCall = "http://FountainCityCycling.org/leaderboard_request/";
    private static String jsonResult = "success";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leaderboard_layout);

        myDeviceID = getDeviceId();

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

        Button whatsMyId = (Button) findViewById(R.id.whats_my_id);
        whatsMyId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_id.equals("")) {
                    Toast.makeText(getApplicationContext(),
                            "You must record a trip to be on the Leader Board", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Your Rider ID is: " + user_id, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Pass the name to the JSON method and create a JSON object from the return
    public class LeaderboardAsync extends AsyncTask<String, String, String> {

        String[] array = {};

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("KENNY", "In pre execute");
        }

        /******************************************************************************************
         * Retrieves all of the leaders in the leader board
         * Really, any rider to take a ride will be in this leaderboard
         ******************************************************************************************/
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("tag", "leaderBoard"));
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_leaderboard, "GET", params);

            try {
                // Check your log cat for JSON reponse
                Log.d("KENNY", "JSON string: " + json.toString());

                // Checking for SUCCESS TAG
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // Leader List found
                    // Getting Array of leaders
                    tempLeaders = json.getJSONArray(TAG_LEADERS);
                    // looping through all leaders
                    for (int i = 0; i < tempLeaders.length(); i++) {
                        JSONObject c = tempLeaders.getJSONObject(i);

                        // Storing each json item in variable
                        String id = c.getString(TAG_USER_ID);
                        String score = c.getString(TAG_SCORE);

                        String tempDeviceID = c.getString(TAG_DEVICE);

                        if (tempDeviceID.equals(myDeviceID)) {
                            user_id = c.getString(TAG_USER_ID);
                        }

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

        /******************************************************************************************
         * After completing background task, now set the adapter with the information retrieved
         ******************************************************************************************/
        protected void onPostExecute(String file_url) {
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {
                    /**
                     * Updating parsed JSON data into ListView
                     * */
//                    adapter = new SimpleAdapter(
//                            LeaderboardActivity.this, leaderList,
//                            R.layout.listitem, new String[]{TAG_USER_ID,
//                            TAG_SCORE},
//                            new int[]{R.id.user_id, R.id.score});
                    // updating listview
                    setListAdapter(new LeaderboardAdapter(getApplicationContext(), leaderList));
                }
            });

        }
    }

    /******************************************************************************************
     * Refreshes the leaderboard scores
     * Can see in real-time the new scores if people are riding
     * The 2000 is sort of a 'dummy' value to make refresh seem like its doing a lot haha
     ******************************************************************************************/
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

    /******************************************************************************************
     * Used to retrieve the Rider ID (since device ID is also unique and can be easily retrieved)
     ******************************************************************************************
     * @return String form of the device ID
     ******************************************************************************************/
    public String getDeviceId() {
        String androidId = Settings.System.getString(this.getContentResolver(),
                Settings.System.ANDROID_ID);
        String androidBase = "androidDeviceId-";

        if (androidId == null) { // This happens when running in the Emulator
            final String emulatorId = "android-RunningAsTestingDeleteMe";
            return emulatorId;
        }
        String deviceId = androidBase.concat(androidId);

        // Fix String Length
        int a = deviceId.length();
        if (a < 32) {
            for (int i = 0; i < 32 - a; i++) {
                deviceId = deviceId.concat("0");
            }
        } else {
            deviceId = deviceId.substring(0, 32);
        }

        return deviceId;
    }
}
