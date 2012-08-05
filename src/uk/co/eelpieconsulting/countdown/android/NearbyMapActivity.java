package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.services.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.views.balloons.LocationCircleOverlay;
import uk.co.eelpieconsulting.countdown.android.views.balloons.StopOverlayItem;
import uk.co.eelpieconsulting.countdown.android.views.balloons.StopsItemizedOverlay;
import uk.co.eelpieconsulting.countdown.android.views.maps.GeoPointFactory;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class NearbyMapActivity extends MapActivity implements LocationListener {

	private static final int FIVE_SECONDS = 5 * 1000;

	private static final String TAG = "StopsActivity";
	
	private static final int STOP_SEARCH_RADIUS = 250;
	
	private TextView status;

	private FetchNearbyStopsTask fetchNearbyStopsTask;

	private MapView mapView;

	private LocationCircleOverlay locationCircleOverlay;

	private Location currentLocation;
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapstops);        
		status = (TextView) findViewById(R.id.status);
		mapView = (MapView) findViewById(R.id.map);
		
		mapView.setBuiltInZoomControls(false);
		mapView.setClickable(true);
		
		locationCircleOverlay = new LocationCircleOverlay();
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setTitle(getString(R.string.near_me));
		if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
			final Stop selectedStop = (Stop) this.getIntent().getExtras().get("stop");
			final Location stopLocation = new Location("knownStopLocation");
			stopLocation.setLatitude(selectedStop.getLatitude());
			stopLocation.setLongitude(selectedStop.getLongitude());
			listNearbyStops(stopLocation);

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
		menu.add(0, 1, 0, R.string.favourites);		
		menu.add(0, 6, 0, R.string.alerts);
		menu.add(0, 7, 0, R.string.search);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			this.startActivity(new Intent(this, FavouritesActivity.class));
			return true;	
			
		case 6:
			this.startActivity(new Intent(this, AlertsActivity.class));
			return true;
			
		case 7:
			this.startActivity(new Intent(this, SearchActivity.class));
			return true;
		}
		return false;
	}
	
	public void onLocationChanged(Location location) {
		Log.i(TAG, "Handset location update received: " + DistanceMeasuringService.makeLocationDescription(location));
		status.setText("Location found: " + DistanceMeasuringService.makeLocationDescription(location));
		status.setVisibility(View.VISIBLE);
		
		if (currentLocation == null) {
			mapView.getController().animateTo(GeoPointFactory.createGeoPointForLatLong(location.getLatitude(), location.getLongitude()));
			mapView.getController().setZoom(17);		
		}
		
		locationCircleOverlay.setPoint(location);
		mapView.refreshDrawableState();
		
		boolean newLocationIsBetterEnoughToJustifyReload = currentLocation == null;	// TODO	Improves to be had here
		if (newLocationIsBetterEnoughToJustifyReload) {
			listNearbyStops(location);		
		}
		currentLocation = location;
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
		status.setText(getString(R.string.searching_for_stops_near) + ": " + DistanceMeasuringService.makeLocationDescription(location));
		status.setVisibility(View.VISIBLE);
						
		fetchNearbyStopsTask = new FetchNearbyStopsTask(ApiFactory.getApi(getApplicationContext()));
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
		
		Drawable drawable = getResources().getDrawable(R.drawable.marker);
		final StopsItemizedOverlay itemizedOverlay = new StopsItemizedOverlay(drawable, mapView);
		for (Stop stop : stops) {		
			itemizedOverlay.addOverlay(new StopOverlayItem(stop));
		}
		
		final List<Overlay> overlays = mapView.getOverlays();
		overlays.add(itemizedOverlay);
				
		overlays.add(locationCircleOverlay);
		
		mapView.refreshDrawableState();
	}
	
	private void registerForLocationUpdates() {
		status.setText(getString(R.string.waiting_for_location));
		status.setVisibility(View.VISIBLE);
		try {
			LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, FIVE_SECONDS, 2500, this);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, FIVE_SECONDS, STOP_SEARCH_RADIUS, this);
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

		private BusesClientService api;
		private Location location;

		public FetchNearbyStopsTask(BusesClientService api) {
			super();
			this.api = api;
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
				return api.findStopsWithin(location.getLatitude(), location.getLongitude(), STOP_SEARCH_RADIUS);				
			} catch (HttpFetchException e) {
				throw new RuntimeException(e);
			} catch (ParsingException e) {
				throw new RuntimeException(e);
			} catch (NetworkNotAvailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}		
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
}