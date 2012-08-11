package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.StopsService;
import uk.co.eelpieconsulting.countdown.android.services.caching.StopsCache;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceToStopComparator;
import uk.co.eelpieconsulting.countdown.android.services.location.KnownStopLocationProviderService;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import uk.co.eelpieconsulting.countdown.android.views.StopsListAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class NearbyStopsListActivity extends Activity implements LocationListener {

	private static final String TAG = "StopsActivity";
	
	private static final int STOP_SEARCH_RADIUS = 250;
	
	private TextView status;
	private FetchNearbyStopsTask fetchNearbyStopsTask;
	private Stop selectedStop;

	private ListView stopsList;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopslist);        
		status = (TextView) findViewById(R.id.status);
		stopsList = (ListView) findViewById(R.id.list);
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setTitle(getString(R.string.near_me));
		stopsList.setVisibility(View.GONE);
		
		if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
			selectedStop = (Stop) this.getIntent().getExtras().get("stop");
			listNearbyStops(KnownStopLocationProviderService.makeLocationForSelectedStop(selectedStop));

		} else {
			registerForLocationUpdates();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (fetchNearbyStopsTask != null && fetchNearbyStopsTask.getStatus().equals(Status.RUNNING)) {
			fetchNearbyStopsTask.cancel(true);
		}
		turnOffLocationUpdates();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.nearby_menu, menu);
		return true;		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.favourites:
			this.startActivity(new Intent(this, FavouritesActivity.class));
			return true;	
			
		case R.id.alerts:
			this.startActivity(new Intent(this, AlertsActivity.class));
			return true;
			
		case R.id.search:
			this.startActivity(new Intent(this, SearchActivity.class));
			return true;
		}
		return false;
	}
	
	public void onLocationChanged(Location location) {
		Log.i(TAG, "Handset location update received: " + DistanceMeasuringService.makeLocationDescription(location));
		status.setText("Location found: " + DistanceMeasuringService.makeLocationDescription(location));
		status.setVisibility(View.VISIBLE);
		
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
		if (selectedStop != null) {
			status.setText(getString(R.string.searching_for_stops_near) + ": " + StopDescriptionService.makeStopTitle(selectedStop));
			status.setVisibility(View.VISIBLE);
		} else {
			status.setText(getString(R.string.searching_for_stops_near) + ": " + DistanceMeasuringService.makeLocationDescription(location));
			status.setVisibility(View.VISIBLE);
		}
				
		fetchNearbyStopsTask = new FetchNearbyStopsTask(new StopsService(ApiFactory.getApi(getApplicationContext()), new StopsCache(getApplicationContext())));
		fetchNearbyStopsTask.execute(location);		
		return;		
	}
	
	private void showStops(Location location, List<Stop> stops) {
		if (stops == null) {
			status.setText("Stops could not be loaded"); // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		if (!location.getProvider().equals(KnownStopLocationProviderService.KNOWN_STOP_LOCATION)) {
			status.setText(getString(R.string.stops_near) + " " + DistanceMeasuringService.makeLocationDescription(location));
			status.setVisibility(View.VISIBLE);
		} else {
			status.setVisibility(View.GONE);
		}
		
		final StopsListAdapter stopsListAdapter = new StopsListAdapter(getApplicationContext(), R.layout.stoprow, this, location);
		for (Stop stop : stops) {
			final boolean isTheNearThisStopItself = selectedStop != null && selectedStop.equals(stop);
			if (!isTheNearThisStopItself) {
				stopsListAdapter.add(stop);
			}
		}
		
		stopsListAdapter.sort(new DistanceToStopComparator(location));
		stopsList.setAdapter(stopsListAdapter);	
		stopsList.setVisibility(View.VISIBLE);
	}
	
	private void registerForLocationUpdates() {
		status.setText(getString(R.string.waiting_for_location));
		status.setVisibility(View.VISIBLE);
		try {
			LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000, 2500, this);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, STOP_SEARCH_RADIUS, this);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
	}

	private void turnOffLocationUpdates() {	// TODO Warn if not location
		try {
			LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			locationManager.removeUpdates(this);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
	}
	
	private class FetchNearbyStopsTask extends AsyncTask<Location, Integer, List<Stop>> {

		private StopsService stopsService;
		private Location location;

		public FetchNearbyStopsTask(StopsService stopsService) {
			super();
			this.stopsService = stopsService;
		}
		
		@Override
		protected void onPostExecute(List<Stop> stops) {
			showStops(location, stops);
		}
		
		@Override
		protected List<Stop> doInBackground(Location... params) {
			final Location location = params[0];
			this.location = location;
			try {				
				return stopsService.findStopsWithin(location.getLatitude(), location.getLongitude(), STOP_SEARCH_RADIUS);				
			} catch (ContentNotAvailableException e) {
				Log.w(TAG, "Could not load nearby stops: " + e.getMessage());
			}
			return null;
		}		
	}
	
}