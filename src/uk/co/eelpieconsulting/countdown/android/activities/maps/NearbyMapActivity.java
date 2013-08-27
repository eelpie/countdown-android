package uk.co.eelpieconsulting.countdown.android.activities.maps;

import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.AlertsActivity;
import uk.co.eelpieconsulting.countdown.android.FavouritesActivity;
import uk.co.eelpieconsulting.countdown.android.NoProvidersException;
import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.SearchActivity;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.StopsService;
import uk.co.eelpieconsulting.countdown.android.services.caching.StopsCache;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.location.KnownStopLocationProviderService;
import uk.co.eelpieconsulting.countdown.android.services.location.LocationService;
import uk.co.eelpieconsulting.countdown.android.views.balloons.StopOverlayItem;
import uk.co.eelpieconsulting.countdown.android.views.balloons.StopsItemizedOverlay;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.Overlay;

public class NearbyMapActivity extends BaseMapActivity {

	private static final String TAG = "NearbyMapActivity";
	
	private StopsCache stopsCache;
	private StopsService stopsService;
	
	private FetchNearbyStopsTask fetchNearbyStopsTask;
	
	private TextView status;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        status = (TextView) findViewById(R.id.status);
				
		stopsCache = new StopsCache(getApplicationContext());
		stopsService = new StopsService(ApiFactory.getApi(getApplicationContext()), stopsCache, getApplicationContext());
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		
		currentLocation = null;
		
		getWindow().setTitle(getString(R.string.near_me));
		if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
			final Stop selectedStop = (Stop) this.getIntent().getExtras().get("stop");
			final Location stopLocation = KnownStopLocationProviderService.makeLocationForSelectedStop(selectedStop);			
			zoomMapToLocation(stopLocation);
			listNearbyStops(stopLocation);
			
		} else {
			status.setText(getString(R.string.waiting_for_location));
			status.setVisibility(View.VISIBLE);
			try {
				LocationService.registerForLocationUpdates(getApplicationContext(), this);						
				final Location bestLastKnownLocation = LocationService.getRecentBestLastKnownLocation(getApplicationContext());
				if (bestLastKnownLocation != null) {
					onLocationChanged(bestLastKnownLocation);
				}				
				
			} catch (NoProvidersException e) {
				status.setText(getString(R.string.no_location_providers));
				status.setVisibility(View.VISIBLE);
			}
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
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
	
	public void onLocationChanged(Location newLocation) {
		Log.i(TAG, "Handset location update received: " + DistanceMeasuringService.makeLocationDescription(newLocation));
		status.setVisibility(View.VISIBLE);
		
		if (LocationService.locationIsSignificantlyDifferentToCurrentLocationToWarrentReloadingResults(currentLocation, newLocation)) {
			status.setText("Location found: " + DistanceMeasuringService.makeLocationDescription(newLocation));
			zoomMapToLocation(newLocation);		
		
		} else if (LocationService.locationIsSignificatelyDifferentOrBetterToWarrentMovingPoint(currentLocation, newLocation)) {
			// TODO current map point should be considered here as well as current data set point.
			locationCircleOverlay.setPoint(newLocation);
			mapView.postInvalidate();
		}
		
		if (LocationService.locationIsSignificantlyDifferentToCurrentLocationToWarrentReloadingResults(currentLocation, newLocation)) {
			listNearbyStops(newLocation);		
			currentLocation = newLocation;
		}
	}
	
	private void listNearbyStops(Location location) {		
		status.setText(getString(R.string.searching_for_stops_near) + ": " + DistanceMeasuringService.makeLocationDescription(location));
		status.setVisibility(View.VISIBLE);
						
		fetchNearbyStopsTask = new FetchNearbyStopsTask(stopsService);
		fetchNearbyStopsTask.execute(location);		
		return;		
	}
	
	private void showStops(Location location, List<Stop> stops) {		
		if (stops == null) {
			status.setText("Stops could not be loaded"); // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		status.setText(getString(R.string.stops_near) + ": " + DistanceMeasuringService.makeLocationDescription(location));		
		if (!stops.isEmpty()) {
			Drawable drawable = getResources().getDrawable(R.drawable.marker);
			final StopsItemizedOverlay itemizedOverlay = new StopsItemizedOverlay(drawable, mapView);
			for (Stop stop : stops) {		
				itemizedOverlay.addOverlay(new StopOverlayItem(stop));
			}
			
			final List<Overlay> overlays = mapView.getOverlays();
			overlays.add(itemizedOverlay);
			
			mapView.postInvalidate();
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
		protected List<Stop> doInBackground(Location... params) {
			final Location location = params[0];
			this.location = location;
			try {				
				return stopsService.findStopsWithin(location.getLatitude(), location.getLongitude(), LocationService.NEAR_BY_RADIUS);				
			} catch (ContentNotAvailableException e) {
				Log.w(TAG, "Could not find stops within: " + e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(List<Stop> stops) {
			Log.d(TAG, "Stops to show: " + stops);
			showStops(location, stops);
		}
		
	}
	
}