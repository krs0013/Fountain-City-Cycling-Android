package edu.auburn.ppl.cyclecolumbus;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class SavedNotesAdapter extends SimpleCursorAdapter {
	private final Context context;
	private final String[] from;
	private final int[] to;
	Cursor cursor;

    /* Constructor */
	public SavedNotesAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, int flags) {
		super(context, R.layout.saved_notes_list_item, c, from, to, flags);
		this.context = context;
		this.from = from;
		this.to = to;
		this.cursor = c;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.saved_notes_list_item, parent,
				false);
		TextView textViewStart = (TextView) rowView
				.findViewById(R.id.TextViewStart);
		TextView textViewType = (TextView) rowView
				.findViewById(R.id.TextViewType);
		ImageView imageNoteType = (ImageView) rowView
				.findViewById(R.id.ImageNoteType);

		cursor.moveToPosition(position);

		SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  HH:mm");
		// sdfStart.setTimeZone(TimeZone.getTimeZone("UTC"));
		Double startTime = cursor.getDouble(cursor
				.getColumnIndex("noterecorded"));
		String start = sdfStart.format(startTime);

		textViewStart.setText(start);

		String[] noteTypeText = new String[] { "Obstruction to riding",
				"Bicycle detection box", "Enforcement", "Bike parking",
				"Bike lane issue", "Note this issue", "Bike parking",
				"Bike shops", "Public restrooms", "Secret passage",
				"Water fountains", "Park", "Note this asset" };

		textViewType.setText(noteTypeText[cursor.getInt(cursor
				.getColumnIndex("notetype"))]);

		int status = cursor.getInt(cursor.getColumnIndex("notestatus"));
		Log.v("KENNY", "Status: " + status);

        /*** 2 = good, 1 = bad ***/
		if (status == 2) {
			switch (cursor.getInt(cursor.getColumnIndex("notetype"))) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				imageNoteType.setImageResource(R.drawable.noteissuepicker_high);
				break;
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
            case 11:
			case 12:
				imageNoteType.setImageResource(R.drawable.noteassetpicker_high);
				break;
			}
		} else if (status == 1) {
			imageNoteType.setImageResource(R.drawable.failedupload_high);
		}
		return rowView;
	}
}