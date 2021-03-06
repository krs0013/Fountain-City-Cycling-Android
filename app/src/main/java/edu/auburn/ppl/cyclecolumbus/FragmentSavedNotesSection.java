package edu.auburn.ppl.cyclecolumbus;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class FragmentSavedNotesSection extends Fragment {

	ListView listSavedNotes;
	ActionMode mActionModeNote;
	ArrayList<Long> noteIdArray = new ArrayList<Long>();
	private MenuItem saveMenuItemDelete, saveMenuItemUpload;

	Long storedID;

	Cursor allNotes;

	public SavedNotesAdapter sna;

	public FragmentSavedNotesSection() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_saved_notes, null);

		Log.v("KENNY", "Cycle: SavedNotes onCreateView");

		setHasOptionsMenu(true);

		listSavedNotes = (ListView) rootView
				.findViewById(R.id.listViewSavedNotes);
		populateNoteList(listSavedNotes);
		
		final DbAdapter mDb = new DbAdapter(getActivity());
		mDb.open();

		// Clean up any bad notes from crashes
		int cleanedNotes = mDb.cleanNoteTables();
		if (cleanedNotes > 0) {
			Toast.makeText(getActivity(),
					"" + cleanedNotes + " bad notes(s) removed.",
					Toast.LENGTH_SHORT).show();
		}
		mDb.close();
		
		noteIdArray.clear();

		return rootView;
	}

    /******************************************************************************************
     * Used when the user is trying to edit the note list
     ******************************************************************************************/
	private ActionMode.Callback mActionModeCallbackNote = new ActionMode.Callback() {

		// Called when the action mode is created; startActionMode() was called
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.saved_notes_context_menu, menu);
			return true;
		}

		// Called each time the action mode is shown. Always called after
		// onCreateActionMode, but
		// may be called multiple times if the mode is invalidated.
		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			Log.v("KENNY", "Prepare");
			saveMenuItemDelete = menu.getItem(0);
			saveMenuItemDelete.setEnabled(false);
			saveMenuItemUpload = menu.getItem(1);

			int flag = 1;
			for (int i = 0; i < listSavedNotes.getCount(); i++) {
				allNotes.moveToPosition(i);
				flag = flag
						* (allNotes.getInt(allNotes
								.getColumnIndex("notestatus")) - 1);
				if (flag == 0) {
					storedID = allNotes.getLong(allNotes.getColumnIndex("_id"));
					Log.v("KENNY", "" + storedID);
					break;
				}
			}
			if (flag == 1) {
				saveMenuItemUpload.setEnabled(false);
			} else {
				saveMenuItemUpload.setEnabled(true);
			}

			mode.setTitle(noteIdArray.size() + " Selected");
			return false; // Return false if nothing is done
		}

		// Called when the user selects a contextual menu item
		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.action_delete_saved_notes:
                try {
                    // delete selected notes
                    for (int i = 0; i < noteIdArray.size(); i++) {
                        deleteNote(noteIdArray.get(i));
                    }
                    mode.finish(); // Action picked, so close the CAB
                } catch (NullPointerException e) {
                    // This crashed on Nexus so I added try-catch
                    Log.d("KENNY", "Caught Note delete crash");
                    e.printStackTrace();
                }
				return true;
            case R.id.action_upload_saved_notes:
                try {
                    retryNoteUpload(storedID);
                    mode.finish(); // Action picked, so close the CAB
                } catch (NullPointerException e) {
                    // This crashed on Nexus so I added try-catch
                    Log.d("KENNY", "Caught Note upload crash");
                    Toast.makeText(getActivity(), "Note has already been uploaded", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
				return true;
			default:
				return false;
			}
		}

		// Called when the user exits the action mode
		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionModeNote = null;
			noteIdArray.clear();
			for (int i = 0; i < listSavedNotes.getCount(); i++) {
				if (listSavedNotes.getChildCount() != 0) {
					listSavedNotes.getChildAt(i).setBackgroundColor(
							Color.parseColor("#80ffffff"));
				}
			}
		}
	};

    /******************************************************************************************
     * Generates the list of notes that the user has made
     ******************************************************************************************
     * @param lv ListView with the notes
     ******************************************************************************************/
	void populateNoteList(ListView lv) {
		// Get list from the real phone database. W00t!
		final DbAdapter mDb = new DbAdapter(getActivity());
		mDb.open();

		try {
			allNotes = mDb.fetchAllNotes();

			String[] from = new String[] { "notetype", "noterecorded",
					"notestatus" };
			int[] to = new int[] { R.id.TextViewType, R.id.TextViewStart };

			sna = new SavedNotesAdapter(getActivity(),
					R.layout.saved_notes_list_item, allNotes, from, to,
					CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

			lv.setAdapter(sna);
		} catch (SQLException sqle) {
			// Do nothing, for now!
		}
		mDb.close();

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int pos,
					long id) {
				allNotes.moveToPosition(pos);
				if (mActionModeNote == null) {
					if (allNotes.getInt(allNotes.getColumnIndex("notestatus")) == 2) {
						Intent i = new Intent(getActivity(),
								NoteMapActivity.class);
						i.putExtra("shownote", id);
						startActivity(i);
					} else if (allNotes.getInt(allNotes
							.getColumnIndex("notestatus")) == 1) {

						buildAlertMessageUnuploadedNoteClicked(id);
					}

				} else {
					// highlight
					if (noteIdArray.indexOf(id) > -1) {
						noteIdArray.remove(id);
						v.setBackgroundColor(Color.parseColor("#80ffffff"));
					} else {
						noteIdArray.add(id);
						v.setBackgroundColor(Color.parseColor("#ff33b5e5"));
					}
					if (noteIdArray.size() == 0) {
                        try {
                            saveMenuItemDelete.setEnabled(false);    // Crashed on some android devices, but still works catching it
                        } catch (NullPointerException e) {
                            Log.d("KENNY", "Crashed doing saveMenuItemDelete.setEnabled(false) in NOTES");
                            e.printStackTrace();
                        }
					} else {
                        try {
                            saveMenuItemDelete.setEnabled(true);    // Crashed on some android devices, but still works catching it
                        } catch (NullPointerException e) {
                            Log.d("KENNY", "Crashed doing saveMenuItemDelete.setEnabled(true) in NOTES");
                            e.printStackTrace();
                        }
					}

					mActionModeNote.setTitle(noteIdArray.size() + " Selected");
				}
			}
		});

		registerForContextMenu(lv);
	}

	private void buildAlertMessageUnuploadedNoteClicked(final long position) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		builder.setTitle("Upload Note");
		builder.setMessage("Do you want to upload this note?");
		builder.setNegativeButton("Upload",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						retryNoteUpload(position);
					}
				});

		builder.setPositiveButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// continue
					}
				});
		final AlertDialog alert = builder.create();
		alert.show();
	}

    /******************************************************************************************
     * If note did not upload, the user can try again
     * It still is saved in-app, but did not previously go to the external database
     ******************************************************************************************
     * @param noteId Unique ID to retry upload
     ******************************************************************************************/
	private void retryNoteUpload(long noteId) {
		NoteUploader uploader = new NoteUploader(getActivity());
		FragmentSavedNotesSection f3 = (FragmentSavedNotesSection) getActivity()
				.getSupportFragmentManager().findFragmentByTag(
						"android:switcher:" + R.id.pager + ":2");
		uploader.setSavedNotesAdapter(sna);
		uploader.setFragmentSavedNotesSection(f3);
		uploader.setListView(listSavedNotes);
		uploader.execute();
	}

    /******************************************************************************************
     * Executed when the user has it highlighted and selects delete
     * Can delete multiple notes
     ******************************************************************************************
     * @param noteId Unique note ID to delte
     ******************************************************************************************/
	private void deleteNote(long noteId) {
		DbAdapter mDbHelper = new DbAdapter(getActivity());
		mDbHelper.open();
		mDbHelper.deleteNote(noteId);
		mDbHelper.close();
		listSavedNotes.invalidate();
		populateNoteList(listSavedNotes);
	}

	// show edit button and hidden delete button
	@Override
	public void onResume() {
		super.onResume();
		Log.v("KENNY", "Cycle: SavedNotes onResume");
		populateNoteList(listSavedNotes);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.v("KENNY", "Cycle: SavedNotes onPause");
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.v("KENNY", "Cycle: SavedNotes onDestroyView");
	}

	/* Creates the menu items */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.saved_notes, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_edit_saved_notes:
			// edit
			if (mActionModeNote != null) {
				return false;
			}

			// Start the CAB using the ActionMode.Callback defined above
			mActionModeNote = getActivity().startActionMode(
					mActionModeCallbackNote);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
