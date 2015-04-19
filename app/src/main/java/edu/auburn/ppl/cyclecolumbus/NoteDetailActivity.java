package edu.auburn.ppl.cyclecolumbus;

/**
 * Allows user to take a picture of the spot and take down a few quick notes
 * Image uploaded to the server.
 *
 * @author Ken Streit, Auburn University
 * @date February 4, 2015
 */

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.provider.Settings.System;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;

public class NoteDetailActivity extends Activity {
	long noteid;
	int noteType = 0;
	int isRecording;
	EditText noteDetails;
	ImageButton imageButton;
	ImageView imageView;
	String imageURL = "";
	byte[] noteImage;
	Bitmap photo;
	private static final int CAMERA_REQUEST = 1888;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_detail);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		Intent myIntent = getIntent();
		noteType = myIntent.getIntExtra("noteType", -1);
		noteid = myIntent.getLongExtra("noteid", -1);
		Log.v("KENNY", "Note ID in NoteDetail: " + noteid);
		Log.v("KENNY", "Note Type is: " + noteType);
		isRecording = myIntent.getIntExtra("isRecording", -1);

		noteDetails = (EditText) findViewById(R.id.editTextNoteDetail);
		imageView = (ImageView) findViewById(R.id.imageView);
		// imageView.setVisibility(4);
		imageButton = (ImageButton) findViewById(R.id.imageButton);
		this.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

		Button addPhotoButton = (Button) findViewById(R.id.addPhotoButton);
		addPhotoButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.v("KENNY", "Add Photo");
				Intent cameraIntent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(cameraIntent, CAMERA_REQUEST);
			}
		});
	}

    /******************************************************************************************
     * Used when user takes an image
     ******************************************************************************************
     * @param requestCode Image request code
     * @param resultCode Image result code
     * @param data Intent that is to be used to get the image
     ******************************************************************************************/
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
			photo = (Bitmap) data.getExtras().get("data");
			imageView.setImageBitmap(photo);
			Log.v("KENNY", "Image Photo: " + photo);
		}
	}

    /******************************************************************************************
     * Converts image (bitmap) to byte array
     ******************************************************************************************
     * @param bitmap Current image
     * @return Byte array of the image
     ******************************************************************************************/
	public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.PNG, 0, outputStream);
		return outputStream.toByteArray();
	}

    /******************************************************************************************
     * Send the note to server
     ******************************************************************************************
     * @param noteDetailsToUpload Details of the note
     * @param noteImage Optional image
     ******************************************************************************************/
	void submit(String noteDetailsToUpload, byte[] noteImage) {
		final Intent xi = new Intent(this, NoteMapActivity.class);

		NoteData note = NoteData.fetchNote(NoteDetailActivity.this, noteid);
		note.populateDetails();

		SimpleDateFormat sdfStart = new SimpleDateFormat("MMMM d, y  HH:mm");
		String fancyStartTime = sdfStart.format(note.startTime);
		Log.v("KENNY", "Start: " + fancyStartTime);

		SimpleDateFormat sdfStart2 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String date = sdfStart2.format(note.startTime);

		// Save the note details to the phone database. W00t!

		String deviceId = getDeviceId();
		if (photo != null) {
			noteImage = getBitmapAsByteArray(photo);
			imageURL = deviceId + "-" + date + "-type-" + noteType;
		} else {
			noteImage = null;
			imageURL = "";
		}

		note.updateNote(noteType, fancyStartTime, noteDetailsToUpload,
				imageURL, noteImage);

		note.updateNoteStatus(NoteData.STATUS_COMPLETE);

		// Now create the MainInput Activity so BACK btn works properly
		// Should not use this.

		// TODO: note uploader
		if (note.notestatus < NoteData.STATUS_SENT) {
			// And upload to the cloud database, too! W00t W00t!
			NoteUploader uploader = new NoteUploader(NoteDetailActivity.this);
			uploader.execute(note.noteid);
		}

		if (isRecording == 1) {
		} else {
			Intent i = new Intent(getApplicationContext(), TabsConfig.class);
			startActivity(i);

			// And, show the map!
			xi.putExtra("shownote", note.noteid);
			xi.putExtra("uploadNote", true);
			Log.v("KENNY", "Noteid: " + String.valueOf(note.noteid));
			startActivity(xi);
			overridePendingTransition(R.anim.slide_in_right,
					R.anim.slide_out_left);
		}

		NoteDetailActivity.this.finish();
	}

    /******************************************************************************************
     * Generate unique device ID
     * Must be length 32 to match iOS and to satisfy server request
     ******************************************************************************************
     * @return String form of device ID
     ******************************************************************************************/
	public String getDeviceId() {
		String androidId = System.getString(this.getContentResolver(),
				System.ANDROID_ID);
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

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.note_detail, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_skip_note_detail:
			// skip
			submit("", null);
			return true;
		case R.id.action_save_note_detail:
			// save
			submit(noteDetails.getEditableText().toString(), noteImage);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// 2.0 and above
	@Override
	public void onBackPressed() {
		// skip
		submit("", null);
	}

	// Before 2.0
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// skip
			submit("", null);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
