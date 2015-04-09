package edu.auburn.ppl.cyclecolumbus;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class FragmentMainInput extends Fragment implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener,
		OnMyLocationButtonClickListener {

	public static final String ARG_SECTION_NUMBER = "section_number";

	Intent fi;
	TripData trip;
	NoteData note;
	boolean isRecording = false;
	Timer timer;
	float curDistance;

	TextView txtDuration;
	TextView txtDistance;
	TextView txtCurSpeed;

	int zoomFlag = 1;

	Location currentLocation = new Location("");

	final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	// Need handler for callbacks to the UI thread
	final Handler mHandler = new Handler();
	final Runnable mUpdateTimer = new Runnable() {
		public void run() {
			updateTimer();
		}
	};

	DbAdapter mDb;
	GoogleMap map;
	UiSettings mUiSettings;
	private LocationClient mLocationClient;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	public FragmentMainInput() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v("Jason", "Cycle: MainInput onCreateView");

		View rootView = inflater.inflate(R.layout.activity_main_input,
				container, false);
		setUpMapIfNeeded();

		Intent rService = new Intent(getActivity(), RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				int state = rs.getState();
				if (state > RecordingService.STATE_IDLE) {
					if (state == RecordingService.STATE_FULL) {
						startActivity(new Intent(getActivity(),
								TripPurposeActivity.class));
					}
					getActivity().finish();
				}
				getActivity().unbindService(this); // race? this says
													// we no longer care
			}
		};
		// This needs to block until the onServiceConnected (above) completes.
		// Thus, we can check the recording status before continuing on.
		getActivity().bindService(rService, sc, Context.BIND_AUTO_CREATE);

		// Log.d("Jason", "Start2");

		// And set up the record button
		Button startButton = (Button) rootView.findViewById(R.id.buttonStart);
		startButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (isRecording == false) {
					// Before we go to record, check GPS status
					final LocationManager manager = (LocationManager) getActivity()
							.getSystemService(Context.LOCATION_SERVICE);
					if (!manager
							.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
						buildAlertMessageNoGps();
					} else {
						startRecording();
					}
				} else if (isRecording == true) {
					// pop up: save, discard, cancel
					buildAlertMessageSaveClicked();
				}
			}
		});

        ImageView leaderboard = (ImageView) rootView.findViewById(R.id.leaderboard_icon);
        leaderboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LeaderboardActivity.class);
                startActivity(intent);
            }
        });

		Button noteThisButton = (Button) rootView
				.findViewById(R.id.buttonNoteThis);
		noteThisButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				final LocationManager manager = (LocationManager) getActivity()
						.getSystemService(Context.LOCATION_SERVICE);
				if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					buildAlertMessageNoGps();
				} else {
					fi = new Intent(getActivity(), NoteTypeActivity.class);
					// update note entity
					note = NoteData.createNote(getActivity());

					fi.putExtra("noteid", note.noteid);

					Log.v("Jason", "Note ID in MainInput: " + note.noteid);

					if (isRecording == true) {
						fi.putExtra("isRecording", 1);
					} else {
						fi.putExtra("isRecording", 0);
					}

					note.updateNoteStatus(NoteData.STATUS_INCOMPLETE);

					double currentTime = System.currentTimeMillis();

					if (currentLocation != null) {
						note.addPointNow(currentLocation, currentTime);

						// Log.v("Jason", "Note ID: "+note);

						startActivity(fi);
						getActivity().overridePendingTransition(
								R.anim.slide_in_right, R.anim.slide_out_left);
						// getActivity().finish();
					} else {
						Toast.makeText(getActivity(),
								"No GPS data acquired; nothing to submit.",
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		// copy from Recording Activity
		txtDuration = (TextView) rootView
				.findViewById(R.id.textViewElapsedTime);
		txtDistance = (TextView) rootView.findViewById(R.id.textViewDistance);
		txtCurSpeed = (TextView) rootView.findViewById(R.id.textViewSpeed);

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

		return rootView;
	}

	public void updateStatus(int points, float distance, float spdCurrent,
			float spdMax) {
		this.curDistance = distance;

		txtCurSpeed.setText(String.format("%1.1f mph", spdCurrent));

		float miles = 0.0006212f * distance;
		txtDistance.setText(String.format("%1.1f miles", miles));
	}

	void cancelRecording() {
		final Button startButton = (Button) getActivity().findViewById(
				R.id.buttonStart);
		startButton.setText("Start");
		// startButton.setBackgroundColor(0x4d7d36);
		Intent rService = new Intent(getActivity(), RecordingService.class);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;
				rs.cancelRecording();
				getActivity().unbindService(this);
			}
		};
		// This should block until the onServiceConnected (above) completes.
		getActivity().bindService(rService, sc, Context.BIND_AUTO_CREATE);

		isRecording = false;

		txtDuration = (TextView) getActivity().findViewById(
				R.id.textViewElapsedTime);
		txtDuration.setText("00:00:00");
		txtDistance = (TextView) getActivity().findViewById(
				R.id.textViewDistance);
		txtDistance.setText("0.0 miles");

		txtCurSpeed = (TextView) getActivity().findViewById(R.id.textViewSpeed);
		txtCurSpeed.setText("0.0 mph");
	}

	void startRecording() {
		// Query the RecordingService to figure out what to do.
		final Button startButton = (Button) getActivity().findViewById(
				R.id.buttonStart);
		Intent rService = new Intent(getActivity(), RecordingService.class);
		getActivity().startService(rService);
		ServiceConnection sc = new ServiceConnection() {
			public void onServiceDisconnected(ComponentName name) {
			}

			public void onServiceConnected(ComponentName name, IBinder service) {
				IRecordService rs = (IRecordService) service;

				switch (rs.getState()) {
				case RecordingService.STATE_IDLE:
					trip = TripData.createTrip(getActivity());
					rs.startRecording(trip);
					isRecording = true;
					startButton.setText("Save");
					break;
				case RecordingService.STATE_RECORDING:
					long id = rs.getCurrentTrip();
					trip = TripData.fetchTrip(getActivity(), id);
					isRecording = true;
					startButton.setText("Save");
					break;
				case RecordingService.STATE_FULL:
					// Should never get here, right?
					break;
				}
				rs.setListener((FragmentMainInput) getActivity()
						.getSupportFragmentManager().findFragmentByTag(
								"android:switcher:" + R.id.pager + ":0"));
				getActivity().unbindService(this);
			}
		};
		getActivity().bindService(rService, sc, Context.BIND_AUTO_CREATE);

		isRecording = true;
	}

	private void buildAlertMessageNoGps() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		builder.setMessage(
				"Your phone's GPS is disabled. Fountain City Cycling needs GPS to determine your location.\n\nGo to System Settings now to enable GPS?")
				.setCancelable(false)
				.setPositiveButton("GPS Settings...",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								final ComponentName toLaunch = new ComponentName(
										"com.android.settings",
										"com.android.settings.SecuritySettings");
								final Intent intent = new Intent(
										Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								intent.addCategory(Intent.CATEGORY_LAUNCHER);
								intent.setComponent(toLaunch);
								intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivityForResult(intent, 0);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		final AlertDialog alert = builder.create();
		alert.show();
	}

	private void buildAlertMessageSaveClicked() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(
				getActivity());
		builder.setTitle("Save Trip");
		builder.setMessage("Do you want to save this trip?");
		builder.setNegativeButton("Save",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// save
						// If we have points, go to the save-trip activity
						// trip.numpoints > 0
						if (trip.numpoints > 0) {
							// Handle pause time gracefully
							if (trip.pauseStartedAt > 0) {
								trip.totalPauseTime += (System
										.currentTimeMillis() - trip.pauseStartedAt);
							}
							if (trip.totalPauseTime > 0) {
								trip.endTime = System.currentTimeMillis()
										- trip.totalPauseTime;
							}
							// Save trip so far (points and extent, but no
							// purpose or
							// notes)
							fi = new Intent(getActivity(),
									TripPurposeActivity.class);
							trip.updateTrip("", "", "", "");

							startActivity(fi);
							getActivity().overridePendingTransition(
									R.anim.slide_in_right,
									R.anim.slide_out_left);
							getActivity().finish();
						}
						// Otherwise, cancel and go back to main screen
						else {
							Toast.makeText(getActivity(),
									"No GPS data acquired; nothing to submit.",
									Toast.LENGTH_SHORT).show();

							cancelRecording();
						}
					}
				});

		builder.setNeutralButton("Discard",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						// discard
						cancelRecording();
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

	void updateTimer() {
		if (trip != null && isRecording) {
			double dd = System.currentTimeMillis() - trip.startTime
					- trip.totalPauseTime;

			txtDuration.setText(sdf.format(dd));

			// double avgSpeed = 3600.0 * 0.6212 * this.curDistance / dd;
			// txtAvgSpeed.setText(String.format("%1.1f mph", avgSpeed));
		}
	}

	// onResume is called whenever this activity comes to foreground.
	// Use a timer to update the trip duration.
	@Override
	public void onResume() {
		super.onResume();

		Log.v("Jason", "Cycle: MainInput onResume");

		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				mHandler.post(mUpdateTimer);
			}
		}, 0, 1000); // every second

		setUpMapIfNeeded();
		if (map != null) {
			// Keep the UI Settings state in sync with the checkboxes.
			mUiSettings.setZoomControlsEnabled(true);
			mUiSettings.setCompassEnabled(true);
			mUiSettings.setMyLocationButtonEnabled(true);
			map.setMyLocationEnabled(true);
			mUiSettings.setScrollGesturesEnabled(true);
			mUiSettings.setZoomGesturesEnabled(true);
			mUiSettings.setTiltGesturesEnabled(true);
			mUiSettings.setRotateGesturesEnabled(true);
		}
		setUpLocationClientIfNeeded();
		mLocationClient.connect();
	}

	// Don't do pointless UI updates if the activity isn't being shown.
	@Override
	public void onPause() {
		super.onPause();
		Log.v("Jason", "Cycle: MainInput onPause");
		// Background GPS.
		if (timer != null)
			timer.cancel();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.v("Jason", "Cycle: MainInput onDestroyView");
	}

	private void setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (map == null) {
			// Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (map != null) {
				map.setMyLocationEnabled(true);
				map.setOnMyLocationButtonClickListener(this);
				mUiSettings = map.getUiSettings();
				// centerMapOnMyLocation();
			}
		}
	}

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getActivity(), this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	/**
	 * Implementation of {@link LocationListener}.
	 */
	@Override
	public void onLocationChanged(Location location) {
		// onMyLocationButtonClick();
		currentLocation = location;

		// Log.v("Jason", "Current Location: "+currentLocation);

		if (zoomFlag == 1) {
			LatLng myLocation;

			if (location != null) {
				myLocation = new LatLng(location.getLatitude(),
						location.getLongitude());
				map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation,
						16));
				zoomFlag = 0;
			}
		}
	}

	/**
	 * Callback called when connected to GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		mLocationClient.requestLocationUpdates(REQUEST, this); // LocationListener
	}

	/**
	 * Callback called when disconnected from GCore. Implementation of
	 * {@link ConnectionCallbacks}.
	 */
	@Override
	public void onDisconnected() {
		// Do nothing
	}

	/**
	 * Implementation of {@link OnConnectionFailedListener}.
	 */
	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Do nothing
	}

	@Override
	public boolean onMyLocationButtonClick() {
		// Toast.makeText(getActivity(), "MyLocation button clicked",
		// Toast.LENGTH_SHORT).show();
		// Return false so that we don't consume the event and the default
		// behavior still occurs
		// (the camera animates to the user's current position).
		return false;
	}
}