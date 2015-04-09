/**  Fountain City Cycling, Copyright 2014 Georgia Institute of Technology
 *                                    Atlanta, GA. USA
 *
 *   @author Christopher Le Dantec <ledantec@gatech.edu>
 *   @author Anhong Guo <guoanhong@gatech.edu>
 *
 *   Updated/Modified for Atlanta's app deployment. Based on the
 *   CycleTracks codebase for SFCTA.
 *
 *   CycleTracks, Copyright 2009,2010 San Francisco County Transportation Authority
 *                                    San Francisco, CA, USA
 *
 *   @author Billy Charlton <billy.charlton@sfcta.org>
 *
 *   This file is part of CycleTracks.
 *
 *   CycleTracks is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CycleTracks is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CycleTracks.  If not, see <http://www.gnu.org/licenses/>.
 */
//
package edu.auburn.ppl.cyclecolumbus;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class TripMapActivity extends Activity {
	// private MapView mapView;
	GoogleMap map;
	// List<Overlay> mapOverlays;
	// xwDrawable drawable;
	ArrayList<CyclePoint> gpspoints;
	// float[] lineCoords;
	Polyline polyline;

	private LatLngBounds.Builder bounds;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_trip_map);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Toast.makeText(this, "trip map", Toast.LENGTH_LONG).show();

		try {
			// Set zoom controls
			map = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.tripMap)).getMap();

			Bundle cmds = getIntent().getExtras();
			long tripid = cmds.getLong("showtrip");

			TripData trip = TripData.fetchTrip(this, tripid);

			// Show trip details
			TextView t1 = (TextView) findViewById(R.id.TextViewMapPurpose);
			TextView t2 = (TextView) findViewById(R.id.TextViewMapInfo);
			TextView t3 = (TextView) findViewById(R.id.TextViewMapFancyStart);
			t1.setText(trip.purp);
			t2.setText(trip.info);
			t3.setText(trip.fancystart);

			gpspoints = trip.getPoints();

            Log.v("KENNY", "Trip ID: " + tripid);

			Log.v("Jason", gpspoints.toString());

			Log.v("Jason", String.valueOf(trip.startpoint.latitude * 1E-6));
			Log.v("Jason", String.valueOf(trip.startpoint.longitude * 1E-6));
			Log.v("Jason", String.valueOf(trip.endpoint.latitude * 1E-6));
			Log.v("Jason", String.valueOf(trip.endpoint.longitude * 1E-6));

			if (trip.startpoint != null) {
				map.addMarker(new MarkerOptions()
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pingreen))
						.anchor(0.0f, 1.0f) // Anchors the marker on the bottom
											// left
						.position(
								new LatLng(trip.startpoint.latitude * 1E-6,
										trip.startpoint.longitude * 1E-6)));
			}
			if (trip.endpoint != null) {
				map.addMarker(new MarkerOptions()
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.pinpurple))
						.anchor(0.0f, 1.0f) // Anchors the marker on the bottom
											// left
						.position(
								new LatLng(trip.endpoint.latitude * 1E-6,
										trip.endpoint.longitude * 1E-6)));
			}

			bounds = new LatLngBounds.Builder();

			PolylineOptions rectOptions = new PolylineOptions();
			rectOptions.geodesic(true).color(Color.BLUE);

			Log.v("Jason", String.valueOf(gpspoints.size()));

			for (int i = 0; i < gpspoints.size(); i++) {
				LatLng point = new LatLng(gpspoints.get(i).latitude * 1E-6,
						gpspoints.get(i).longitude * 1E-6);
				bounds.include(point);
				rectOptions.add(point);
			}

			polyline = map.addPolyline(rectOptions);

			map.setOnCameraChangeListener(new OnCameraChangeListener() {

				@Override
				public void onCameraChange(CameraPosition arg0) {
					// Move camera.
					map.moveCamera(CameraUpdateFactory.newLatLngBounds(
							bounds.build(), 50));
					// Remove listener to prevent position reset on camera move.
					map.setOnCameraChangeListener(null);
				}
			});

			if (trip.status < TripData.STATUS_SENT && cmds != null
					&& cmds.getBoolean("uploadTrip", false)) {
				// And upload to the cloud database, too! W00t W00t!
				TripUploader uploader = new TripUploader(TripMapActivity.this);
				uploader.execute(trip.tripid);
			}

		} catch (Exception e) {
			Log.e("GOT!", e.toString());
		}
	}

	// Make sure overlays get zapped when we go BACK
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && map != null) {
			// map.getOverlays().clear();
			polyline.remove();
		}
		return super.onKeyDown(keyCode, event);
	}

	/* Creates the menu items */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.trip_map, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Handles item selections */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_close_trip_map:
			// close -> go back to FragmentMainInput
			if (map != null) {
				polyline.remove();
			}

			onBackPressed();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
