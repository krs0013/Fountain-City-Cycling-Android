package edu.auburn.ppl.cyclecolumbus;

/**
 * This class is used in the "Settings" tab in the app.  It will save the users
 * personal information if they want to save it.  Also will explain how the app is
 * intended to be used if they click the "Get started with Fountain City Cycling" button.
 *
 * @author Ken Streit, Auburn University
 * @date February 4, 2015
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Map;
import java.util.Map.Entry;

public class FragmentUserInfo extends Fragment {

	public final static int PREF_AGE = 1;
	public final static int PREF_ZIPHOME = 2;
	public final static int PREF_ZIPWORK = 3;
	public final static int PREF_ZIPSCHOOL = 4;
	public final static int PREF_EMAIL = 5;
	public final static int PREF_GENDER = 6;
	public final static int PREF_CYCLEFREQ = 7;
	public final static int PREF_ETHNICITY = 8;
	public final static int PREF_INCOME = 9;
	public final static int PREF_RIDERTYPE = 10;
	public final static int PREF_RIDERHISTORY = 11;

	private static final String TAG = "UserPrefActivity";

	public FragmentUserInfo() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final View rootView = inflater.inflate(R.layout.activity_user_info,
				container, false);

		final Button GetStarted = (Button) rootView
				.findViewById(R.id.buttonGetStarted);
		GetStarted.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
                View instructions = rootView.findViewById(R.id.instructions_scroll);
                View x = rootView.findViewById(R.id.x_rl);
                rootView.findViewById(R.id.ScrollView01).setVisibility(View.GONE);

                FadeSingleton fade = FadeSingleton.getInstance();
                int mShortAnimationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);
                mShortAnimationDuration *= 2;
                fade.fadeIn(instructions, mShortAnimationDuration);
                fade.fadeIn(x, mShortAnimationDuration);
                setXClickListener(rootView, fade, mShortAnimationDuration);
			}
		});

		SharedPreferences settings = getActivity().getSharedPreferences(
				"PREFS", 0);
		Map<String, ?> prefs = settings.getAll();
		for (Entry<String, ?> p : prefs.entrySet()) {
			int key = Integer.parseInt(p.getKey());
			// CharSequence value = (CharSequence) p.getValue();

			switch (key) {
			case PREF_AGE:
				((Spinner) rootView.findViewById(R.id.ageSpinner))
						.setSelection(((Integer) p.getValue()).intValue());
				break;
			case PREF_ETHNICITY:
				((Spinner) rootView.findViewById(R.id.ethnicitySpinner))
						.setSelection(((Integer) p.getValue()).intValue());
				break;
			case PREF_INCOME:
				((Spinner) rootView.findViewById(R.id.incomeSpinner))
						.setSelection(((Integer) p.getValue()).intValue());
				break;
			case PREF_RIDERTYPE:
				((Spinner) rootView.findViewById(R.id.ridertypeSpinner))
						.setSelection(((Integer) p.getValue()).intValue());
				break;
			case PREF_RIDERHISTORY:
				((Spinner) rootView.findViewById(R.id.riderhistorySpinner))
						.setSelection(((Integer) p.getValue()).intValue());
				break;
			case PREF_ZIPHOME:
				((EditText) rootView.findViewById(R.id.TextZipHome))
						.setText((CharSequence) p.getValue());
				break;
			case PREF_ZIPWORK:
				((EditText) rootView.findViewById(R.id.TextZipWork))
						.setText((CharSequence) p.getValue());
				break;
			case PREF_ZIPSCHOOL:
				((EditText) rootView.findViewById(R.id.TextZipSchool))
						.setText((CharSequence) p.getValue());
				break;
			case PREF_EMAIL:
				((EditText) rootView.findViewById(R.id.TextEmail))
						.setText((CharSequence) p.getValue());
				break;
			case PREF_CYCLEFREQ:
				((Spinner) rootView.findViewById(R.id.cyclefreqSpinner))
						.setSelection(((Integer) p.getValue()).intValue());
				// ((SeekBar)
				// findViewById(R.id.SeekCycleFreq)).setProgress(((Integer)
				// p.getValue()).intValue());
				break;
			case PREF_GENDER:
				((Spinner) rootView.findViewById(R.id.genderSpinner))
						.setSelection(((Integer) p.getValue()).intValue());
				break;
			// int x = ((Integer) p.getValue()).intValue();
			// if (x == 2) {
			// ((RadioButton) findViewById(R.id.ButtonMale)).setChecked(true);
			// } else if (x == 1) {
			// ((RadioButton) findViewById(R.id.ButtonFemale)).setChecked(true);
			// }
			// break;
			}
		}

		final EditText edittextEmail = (EditText) rootView
				.findViewById(R.id.TextEmail);

		edittextEmail.setImeOptions(EditorInfo.IME_ACTION_DONE);

		setHasOptionsMenu(true);
		return rootView;
	}

    /**
     * If pressed and the single image is appeared first, it sets it to GONE
     * If pressed and all species images are visible, it goes back to the main menu.
     */
    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*************************************************************************************************
     * Use this to change around the left and right arrows in the instructions
     *************************************************************************************************
     *************************************************************************************************/
    private void setXClickListener(final View view, final FadeSingleton fade, final int mShortAnimationDuration) {
        final View x = view.findViewById(R.id.x_rl);
        x.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View instructions = view.findViewById(R.id.instructions_scroll);
                View scroll1 = view.findViewById(R.id.ScrollView01);

                fade.fadeIn(scroll1, mShortAnimationDuration);
                fade.fadeOut(instructions, mShortAnimationDuration);
                fade.fadeOut(x, mShortAnimationDuration);
            }
        });
    }
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// savePreferences();
	}

	public void savePreferences() {
		// Toast.makeText(getActivity(), "savePreferences()",
		// Toast.LENGTH_LONG).show();

		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		SharedPreferences settings = getActivity().getSharedPreferences(
				"PREFS", 0);
		SharedPreferences.Editor editor = settings.edit();

		editor.putInt("" + PREF_AGE,
				((Spinner) getActivity().findViewById(R.id.ageSpinner))
						.getSelectedItemPosition());
		editor.putInt("" + PREF_ETHNICITY, ((Spinner) getActivity()
				.findViewById(R.id.ethnicitySpinner)).getSelectedItemPosition());
		editor.putInt("" + PREF_INCOME,
				((Spinner) getActivity().findViewById(R.id.incomeSpinner))
						.getSelectedItemPosition());
		editor.putInt("" + PREF_RIDERTYPE, ((Spinner) getActivity()
				.findViewById(R.id.ridertypeSpinner)).getSelectedItemPosition());
		editor.putInt("" + PREF_RIDERHISTORY, ((Spinner) getActivity()
				.findViewById(R.id.riderhistorySpinner))
				.getSelectedItemPosition());

		editor.putString("" + PREF_ZIPHOME, ((EditText) getActivity()
				.findViewById(R.id.TextZipHome)).getText().toString());
		editor.putString("" + PREF_ZIPWORK, ((EditText) getActivity()
				.findViewById(R.id.TextZipWork)).getText().toString());
		editor.putString("" + PREF_ZIPSCHOOL, ((EditText) getActivity()
				.findViewById(R.id.TextZipSchool)).getText().toString());
		editor.putString("" + PREF_EMAIL, ((EditText) getActivity()
				.findViewById(R.id.TextEmail)).getText().toString());

		editor.putInt("" + PREF_CYCLEFREQ, ((Spinner) getActivity()
				.findViewById(R.id.cyclefreqSpinner)).getSelectedItemPosition());
		// editor.putInt("" + PREF_CYCLEFREQ, ((SeekBar)
		// findViewById(R.id.SeekCycleFreq)).getProgress());

		editor.putInt("" + PREF_GENDER,
				((Spinner) getActivity().findViewById(R.id.genderSpinner))
						.getSelectedItemPosition());

		// Don't forget to commit your edits!!!
		editor.commit();
		Toast.makeText(getActivity(), "User information saved.",
				Toast.LENGTH_SHORT).show();
		// Toast.makeText(getActivity(), ""+((Spinner)
		// getActivity().findViewById(R.id.ageSpinner)).getSelectedItemPosition(),
		// Toast.LENGTH_LONG).show();
	}

	/* Creates the menu items */
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu items for use in the action bar
		inflater.inflate(R.menu.user_info, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_save_user_info:
			savePreferences();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}