package edu.auburn.ppl.cyclecolumbus;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

public class NoteTypeActivity extends Activity {
	// HashMap<Integer, ToggleButton> purpButtons = new HashMap<Integer,
	// ToggleButton>();
	int noteType;

	long noteid;

	int isRecording;

	HashMap<Integer, String> noteTypeDescriptions = new HashMap<Integer, String>();

	String[] values;

	private MenuItem saveMenuItem;

    /******************************************************************************************
     * Text appears under a note reason and explains what it is for
     * This is one-click only
     ******************************************************************************************/
	void prepareNoteTypeButtons() {
		// Note Issue
		noteTypeDescriptions
				.put(0,
						"Here's a spot where the road needs to be repaired (pothole, rough concrete, gravel in the road, manhole cover, sewer grate) or where " +
                                "a bike cannot travel.");
		noteTypeDescriptions.put(1,
				"Here's a signal that you can activate with your bike. Mark it down if it is damaged to bring attention to the issue!");
		noteTypeDescriptions
				.put(2,
						"The bike lane is always blocked here, cars disobey \"no right on red\" anything where the cops can help make cycling safer.");
		noteTypeDescriptions.put(3,
				"You need a bike rack to secure your bike here.");
		noteTypeDescriptions
				.put(4,
						"Where the bike lane ends (abruptly) or is too narrow (pesky parked cars).");
		noteTypeDescriptions
				.put(5,
						"Anything else ripe for improvement: want a sharrow, a sign, a bike lane? Share the details.");

		// Note Asset
		noteTypeDescriptions
				.put(6,
						"Park them here and remember to secure your bike well! Please only include racks or other objects intended for bikes.");
		noteTypeDescriptions
				.put(7,
						"Have a flat, a broken chain, or spongy brakes? Or do you need a bike to jump into this world of cycling in the first place? Here's a shop ready to help.");
		noteTypeDescriptions
				.put(8,
						"Help us make cycling mainstream! Here's a place to refresh yourself before you re-enter the fashionable world of Fountain City.");
		noteTypeDescriptions
				.put(9,
						"Here's an access point under the tracks, through the park, onto a trail, or over a ravine.");
		noteTypeDescriptions
				.put(10,
						"Here's a spot to fill your bottle on those hot summer days. Stay hydrated, people! We need you.");
        noteTypeDescriptions
                .put(11,
                        "Take a load off and enjoy the view or take a nice ride through the park! Either way, this is a great place to mark down.");
		noteTypeDescriptions
				.put(12,
						"Anything else we should map to help your fellow cyclists? Share the details.");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_type);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Set up note type buttons
		noteType = -1;
		prepareNoteTypeButtons();

		Intent myIntent = getIntent(); // gets the previously created intent
		noteid = myIntent.getLongExtra("noteid", 0);

		isRecording = myIntent.getIntExtra("isRecording", -1);

		final ListView listView = (ListView) findViewById(R.id.listViewNoteType);
		values = new String[] { "Obstruction to riding",
                "Bicycle detection box", "Enforcement", "Bike parking",
                "Bike lane issue", "Note this issue", "Bike parking",
                "Bike shops", "Public restrooms", "Secret passage",
                "Water fountains", "Park", "Note this asset" };
		// final ArrayList<String> list = new ArrayList<String>();
		// for (int i = 0; i < values.length; ++i) {
		// list.add(values[i]);
		// }
		NoteTypeAdapter adapter = new NoteTypeAdapter(this, values);
		listView.setAdapter(adapter);
		// set default
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			private View oldSelection = null;

			public void clearSelection() {
				if (oldSelection != null) {
					oldSelection.setBackgroundColor(Color.parseColor("#ffffff"));
				}
			}

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				// view.setSelected(true);
				// view.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.bg_key));
				clearSelection();
				oldSelection = view;
				view.setBackgroundColor(Color.parseColor("#ff33b5e5"));
				// view.setBackgroundDrawable(parent.getResources().getDrawable(R.drawable.bg_key));
				noteType = position;
				((TextView) findViewById(R.id.textViewNoteTypeDesc))
						.setText(Html.fromHtml(noteTypeDescriptions
								.get(position)));
				saveMenuItem.setEnabled(true);
				// highlight
			}

		});

		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.note_type, menu);
		saveMenuItem = menu.getItem(1);
		saveMenuItem.setEnabled(false);
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_cancel_note_type:
			// cancel
			Toast.makeText(getBaseContext(), "Note discarded.",
					Toast.LENGTH_SHORT).show();

			// Cancel
			NoteData note = NoteData.fetchNote(NoteTypeActivity.this, noteid);
			Log.v("KENNY", "Note id: " + noteid);
			note.dropNote();

			NoteTypeActivity.this.finish();
			
			overridePendingTransition(android.R.anim.slide_in_left,
					android.R.anim.slide_out_right);
			return true;
		case R.id.action_save_note_type:
			// move to next view
			Intent intentToNoteDetail = new Intent(NoteTypeActivity.this,
					NoteDetailActivity.class);
			intentToNoteDetail.putExtra("noteType", noteType);

			intentToNoteDetail.putExtra("noteid", noteid);

			Log.v("KENNY", "Note ID in NoteType: " + noteid);

			if (isRecording == 1) {
				intentToNoteDetail.putExtra("isRecording", 1);
			} else {
				intentToNoteDetail.putExtra("isRecording", 0);
			}

			startActivity(intentToNoteDetail);
			overridePendingTransition(R.anim.slide_in_right,
					R.anim.slide_out_left);
			NoteTypeActivity.this.finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
		// cancel
		Toast.makeText(getBaseContext(), "Note discarded.", Toast.LENGTH_SHORT)
				.show();

		// Cancel
		NoteData note = NoteData.fetchNote(NoteTypeActivity.this, noteid);
		Log.v("KENNY", "Note id: " + noteid);
		note.dropNote();

		NoteTypeActivity.this.finish();
		
		overridePendingTransition(android.R.anim.slide_in_left,
				android.R.anim.slide_out_right);
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// cancel
			Toast.makeText(getBaseContext(), "Note discarded.",
					Toast.LENGTH_SHORT).show();

			// Cancel
			NoteData note = NoteData.fetchNote(NoteTypeActivity.this, noteid);
			Log.v("KENNY", "Note id: " + noteid);
			note.dropNote();

			NoteTypeActivity.this.finish();
			
			overridePendingTransition(android.R.anim.slide_in_left,
					android.R.anim.slide_out_right);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
