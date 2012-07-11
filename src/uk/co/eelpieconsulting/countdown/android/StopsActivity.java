package uk.co.eelpieconsulting.countdown.android;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.co.eelpieconsulting.countdown.android.api.CountdownApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.DistanceToStopComparator;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import uk.co.eelpieconsulting.countdown.api.CountdownApi;
import uk.co.eelpieconsulting.countdown.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.countdown.exceptions.ParsingException;
import uk.co.eelpieconsulting.countdown.model.Stop;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StopsActivity extends Activity implements LocationListener {

	private static final String TAG = "StopsActivity";
	
	private static final int STOP_SEARCH_RADIUS = 250;
	
	private CountdownApi api;
	private TextView status;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);
        
        api = CountdownApiFactory.getApi();        		
		status = (TextView) findViewById(R.id.status);
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setTitle(getString(R.string.find_stops));
        registerForLocationUpdates();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		turnOffLocationUpdates();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, R.string.favourites);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			this.startActivity(new Intent(this, FavouritesActivity.class));
			return true;
		}
		return false;
	}
	
	public void onLocationChanged(Location location) {
		Log.i(TAG, "Handset location update received: " + DistanceMeasuringService.makeLocationDescription(location));
		status.setText("Location found: " + DistanceMeasuringService.makeLocationDescription(location));
		
		listNearbyStops(location);
		
		if (location.hasAccuracy() && location.getAccuracy() < STOP_SEARCH_RADIUS) {	
				turnOffLocationUpdates();
		} else {
			status.setText("Hoping for more accurate location than: " + DistanceMeasuringService.makeLocationDescription(location));
		}	
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub		
	}
	
	private void listNearbyStops(Location location) {
		try {
			showStops(location, loadStops(location));
			return;
			
		} catch (HttpFetchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		status.setText("Failed to load stops");
	}
	
	private List<Stop> loadStops(Location location) throws HttpFetchException, ParsingException {
		status.setText(getString(R.string.searching_for_stops_near) + ": " + DistanceMeasuringService.makeLocationDescription(location));
		return api.findStopsWithin(location.getLatitude(), location.getLongitude(), STOP_SEARCH_RADIUS);
	}
	
	private void showStops(Location location, List<Stop> stops) {
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		status.setText(getString(R.string.stops_near) + ": " + DistanceMeasuringService.makeLocationDescription(location));
		
		Collections.sort(stops, (Comparator<? super Stop>) new DistanceToStopComparator(location));
		
		for (Stop stop : stops) {
			final TextView stopTextView = new TextView(this.getApplicationContext());

			String stopDescription = StopDescriptionService.makeStopDescription(stop);
			stopDescription = stopDescription + "\n" + DistanceMeasuringService.distanceTo(location, stop) + " metres away" + "\n\n";
			
			stopTextView.setText(stopDescription);
			stopTextView.setOnClickListener(new StopClicker(stop));
			stopsList.addView(stopTextView);
		}
	}
	
	private void registerForLocationUpdates() {
		status.setText(getString(R.string.waiting_for_location));
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000, 2500, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, STOP_SEARCH_RADIUS, this);
	}

	private void turnOffLocationUpdates() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(this);
	}
	
	private class StopClicker implements OnClickListener {
		private Stop stop;

		public StopClicker(Stop stop) {
			this.stop = stop;
		}

		public void onClick(View view) {
			Intent intent = getIntentForContentsType(view.getContext(), stop);
			intent.putExtra("stop", stop);
			startActivity(intent);
		}

		private Intent getIntentForContentsType(Context context, Stop stop) {
			return new Intent(context, CountdownActivity.class);
		}
	}
	
}