package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

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
	
	private CountdownApi api;
	private TextView status;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);
        
        api = new CountdownApi("http://countdown.api.tfl.gov.uk");
		status = (TextView) findViewById(R.id.status);
	}
    
	@Override
	protected void onResume() {
		super.onResume();
        registerForLocationUpdates();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "Arrivals");
		menu.add(0, 2, 0, "Stops");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			this.startActivity(new Intent(this, CountdownActivity.class));
			return true;

		case 2:
			this.startActivity(new Intent(this, StopsActivity.class));
			return true;
		}
		return false;
	}
	
	public void onLocationChanged(Location location) {
		Log.i(TAG, "Handset location update received: " + location);
		status.setText("Location found");
		listNearbyStops(location.getLatitude(), location.getLongitude());
		turnOffLocationUpdates();
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
	
	private void listNearbyStops(double latitude, double longitude) {
		try {
			List<Stop> stops = loadStops(latitude, longitude);
			status.setText("Stops near: " + latitude + ", " + longitude);
			showStops(stops);
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
	
	private List<Stop> loadStops(double latitude, double longitude) throws HttpFetchException, ParsingException {
		status.setText("Searching for stops near: " + latitude + ", " + longitude);
		return api.findStopsWithin(latitude, longitude, 200);
	}
	
	private void showStops(List<Stop> favouriteStops) {
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		for (Stop stop : favouriteStops) {
			final TextView stopTextView = new TextView(this.getApplicationContext());
			stopTextView.setText(stop.toString());
			stopTextView.setOnClickListener(new StopClicker(stop));
			stopsList.addView(stopTextView);
		}
	}
	
	private void registerForLocationUpdates() {
		status.setText("Waiting for location");
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60 * 1000, 500, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60 * 1000, 500, this);
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