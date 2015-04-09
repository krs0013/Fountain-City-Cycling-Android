package edu.auburn.ppl.cyclecolumbus;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by kennystreit on 4/7/15.
 */
public class FragmentLeaderboard extends Fragment {

    private JSONParser jsonParser;
    //10.0.2.2 is the address used by the Android emulators to refer to the host address
    // change this to the IP of another host if required
    private static String apiCall = "http://FountainCityCycling.org/leaderboard_request/";
    private static String jsonResult = "success";

    /*
     * TODO:
     * - Create a LeaderBoardItem class with id, distance, and score
     * - Store each one into an ArrayList.
     * - Create a ListView layout (within leaderboard_layout)
     * - Loop throught ArrayList and add each one into the ListView
     * - *** Remember *** Add a refresh button!
     */


    public FragmentLeaderboard() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.leaderboard_layout, null);
        //Invoke the Json Parser
//        jsonParser = new JSONParser();
//
//        //Pass the name to the JSON method and create a JSON object from the return
//        JSONObject json = getLeaderboard();
//
//        // check the success of the JSON call
//        try {
//            if (json.getString(jsonResult) != null) {
//                String res = json.getString(jsonResult);
//                if(Integer.parseInt(res) == 1){
//                    //If it's a success create a new JSON object for the user element
//                    JSONObject json_leaderboard = json.getJSONObject("leaderBoard"); // TODO: DO SOMETHING HERE
//                    Log.d("KENNY", "Leaderboard JSON: " + jsonResult);
//
//                }else{
//                    // TODO: DO SOMETHING HERE
//                }
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

        return rootView;
    }

    //The below passes the tag and the user name over to the JSON parser class
//    public JSONObject getLeaderboard(){
//        List<NameValuePair> params = new ArrayList<NameValuePair>();
//        params.add(new BasicNameValuePair("tag", "leaderBoard"));
//        JSONObject json = jsonParser.getJSONFromUrl(apiCall, params);
//        return json;
//    }
}
