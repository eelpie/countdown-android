package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.buses.client.model.StopsNear;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.StopsService;
import uk.co.eelpieconsulting.countdown.android.services.caching.StopsCache;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceToStopComparator;
import uk.co.eelpieconsulting.countdown.android.services.location.KnownStopLocationProviderService;
import uk.co.eelpieconsulting.countdown.android.services.location.LocationService;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import uk.co.eelpieconsulting.countdown.android.views.StopsListAdapter;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class NearbyStopsListActivity extends Activity implements LocationListener {

	private static final String TAG = "StopsActivity";
		
	private TextView status;
	private FetchNearbyStopsTask fetchNearbyStopsTask;
	
	private Stop selectedStop;
	private Location currentLocation;
	
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
		stopsList.setVisibility(View.GONE);
		currentLocation = null;
		
		if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
			selectedStop = (Stop) this.getIntent().getExtras().get("stop");
			listNearbyStops(KnownStopLocationProviderService.makeLocationForSelectedStop(selectedStop));

		} else {
			status.setText(getString(R.string.waiting_for_location));
			status.setVisibility(View.VISIBLE);
			try {
				final Location bestLastKnownLocation = LocationService.getRecentBestLastKnownLocation(this);
				if (bestLastKnownLocation != null) {
					onLocationChanged(bestLastKnownLocation);
					if (LocationService.isAccurateEnoughForNearbyRoutes(currentLocation)) {
						return;
					}
				} 
				
				LocationService.registerForLocationUpdates(getApplicationContext(), this);				
				
			} catch (NoProvidersException e) {
				status.setText(getString(R.string.no_location_providers));
				status.setVisibility(View.VISIBLE);			
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		LocationService.turnOffLocationUpdates(this.getApplicationContext(), this);
		if (fetchNearbyStopsTask != null && fetchNearbyStopsTask.getStatus().equals(Status.RUNNING)) {
			fetchNearbyStopsTask.cancel(true);
		}	
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
		
		if (LocationService.locationIsSignificantlyDifferentToCurrentLocationToWarrentReloadingResults(currentLocation, location)) {
			listNearbyStops(location);
			currentLocation = location;
		}
		
		if (location.hasAccuracy() && location.getAccuracy() < LocationService.NEAR_BY_RADIUS) {
			LocationService.turnOffLocationUpdates(this.getApplicationContext(), this);
			
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
				
		fetchNearbyStopsTask = new FetchNearbyStopsTask(new StopsService(ApiFactory.getApi(getApplicationContext()), new StopsCache(getApplicationContext()), getApplicationContext()));
		fetchNearbyStopsTask.execute(location);		
		return;		
	}
	
	private void showStops(Location location, StopsNear stopsNear) {
		if (stopsNear == null) {
			status.setText("Stops could not be loaded"); // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		if (!location.getProvider().equals(KnownStopLocationProviderService.KNOWN_STOP_LOCATION)) {
			status.setText(getString(R.string.stops_near) + " " + DistanceMeasuringService.makeLocationDescription(stopsNear.getLocation(), location));
			status.setVisibility(View.VISIBLE);
		} else {
			status.setVisibility(View.GONE);
		}
		
		final StopsListAdapter stopsListAdapter = new StopsListAdapter(getApplicationContext(), R.layout.stoprow, this, location);
		for (Stop stop : stopsNear.getStops()) {
			final boolean isTheNearThisStopItself = selectedStop != null && selectedStop.equals(stop);
			if (!isTheNearThisStopItself) {
				stopsListAdapter.add(stop);
			}
		}
		
		stopsListAdapter.sort(new DistanceToStopComparator(location));
		stopsList.setAdapter(stopsListAdapter);	
		stopsList.setVisibility(View.VISIBLE);
	}
	
	private class FetchNearbyStopsTask extends AsyncTask<Location, Integer, StopsNear> {

		private StopsService stopsService;
		private Location location;

		public FetchNearbyStopsTask(StopsService stopsService) {
			super();
			this.stopsService = stopsService;
		}
		
		@Override
		protected void onPostExecute(StopsNear stopsNear) {
			showStops(location, stopsNear);
		}
		
		@Override
		protected StopsNear doInBackground(Location... params) {
			final Location location = params[0];
			this.location = location;
			try {				
				return stopsService.findStopsNear(location.getLatitude(), location.getLongitude(), LocationService.NEAR_BY_RADIUS);				
			} catch (ContentNotAvailableException e) {
				Log.w(TAG, "Could not load nearby stops: " + e.getMessage());
			}
			return null;
		}		
	}
	
}