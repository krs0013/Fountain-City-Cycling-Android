package edu.auburn.ppl.cyclecolumbus;

/**
 * Created by kennystreit on 4/15/15.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class LeaderboardAdapter extends ArrayAdapter<String> {
    private final Context context;
    ArrayList<HashMap<String, String>> leaderList;
    private static final String TAG_USER_ID = "user_id";
    private static final String TAG_SCORE = "score";

    public LeaderboardAdapter(Context context, ArrayList<HashMap<String, String>> leaderList) {
        super(context, R.layout.listitem, new String[leaderList.size()]);
        this.context = context;
        this.leaderList = leaderList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.listitem, parent, false);
        TextView riderID = (TextView) rowView.findViewById(R.id.user_id);
        TextView score = (TextView) rowView.findViewById(R.id.score);
        riderID.setText("Rider ID: " + leaderList.get(position).get(TAG_USER_ID));
        //score.setText(leaderList.get(position).get(TAG_SCORE));

        String roundString = leaderList.get(position).get(TAG_SCORE);
        double tempScore = Double.parseDouble(roundString);
        int roundedScore = (int) tempScore;

        score.setText("Score: " + String.valueOf(roundedScore));

        return rowView;
    }
}
