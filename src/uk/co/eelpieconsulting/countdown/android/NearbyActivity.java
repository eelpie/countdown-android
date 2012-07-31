package uk.co.eelpieconsulting.countdown.android;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import uk.co.eelpieconsulting.buses.client.BusesClient;
import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.DistanceToStopComparator;
import uk.co.eelpieconsulting.countdown.android.views.GeoPointFactory;
import uk.co.eelpieconsulting.countdown.android.views.SimpleItemizedOverlay;
import uk.co.eelpieconsulting.countdown.android.views.StopClicker;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class NearbyActivity extends MapActivity implements LocationListener {

	private static final String TAG = "StopsActivity";
	
	private static final int STOP_SEARCH_RADIUS = 250;
	
	private TextView status;

	private FetchNearbyStopsTask fetchNearbyStopsTask;

	private MapView mapView;
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapstops);        
		status = (TextView) findViewById(R.id.status);
		mapView = (MapView) findViewById(R.id.map);
		
		mapView.setBuiltInZoomControls(false);
		mapView.setClickable(true);		
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
		status.setText(getString(R.string.searching_for_stops_near) + ": " + DistanceMeasuringService.makeLocationDescription(location));
		status.setVisibility(View.VISIBLE);
		
		mapView.getController().animateTo(GeoPointFactory.createGeoPointForLatLong(location.getLatitude(), location.getLongitude()));
        mapView.getController().setZoom(18);
				
		fetchNearbyStopsTask = new FetchNearbyStopsTask(ApiFactory.getApi());
		fetchNearbyStopsTask.execute(location);		
		return;		
	}
	
	private void showStops(Location location, List<Stop> stops) {		
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		status.setText(getString(R.string.stops_near) + ": " + DistanceMeasuringService.makeLocationDescription(location));
		
		Collections.sort(stops, (Comparator<? super Stop>) new DistanceToStopComparator(location));
		
		Drawable drawable = getResources().getDrawable(R.drawable.marker);
		final SimpleItemizedOverlay itemizedOverlay = new SimpleItemizedOverlay(drawable, mapView);
		for (Stop stop : stops) {
			final TextView stopTextView = new TextView(this.getApplicationContext());

			String stopDescription = StopDescriptionService.makeStopDescription(stop);
			stopDescription = stopDescription + "\n" + DistanceMeasuringService.distanceTo(location, stop) + " metres away\n\n";
			
			stopTextView.setText(stopDescription);
			stopTextView.setOnClickListener(new StopClicker(this, stop));
			stopsList.addView(stopTextView);
			
			final GeoPoint point = GeoPointFactory.createGeoPointForLatLong(stop.getLatitude(), stop.getLongitude());
			OverlayItem overlayItem = new OverlayItem(point, stop.getName(), "Towards " + stop.getTowards());
			itemizedOverlay.addOverlay(overlayItem);
		}

		final List<Overlay> overlays = mapView.getOverlays();
		overlays.add(itemizedOverlay);
		mapView.refreshDrawableState();
	}
	
	private void registerForLocationUpdates() {
		status.setText(getString(R.string.waiting_for_location));
		status.setVisibility(View.VISIBLE);
		
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000, 2500, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, STOP_SEARCH_RADIUS, this);
	}

	private void turnOffLocationUpdates() {
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(this);
	}
	
	private class FetchNearbyStopsTask extends AsyncTask<Location, Integer, List<Stop>> {

		private BusesClient api;
		private Location location;

		public FetchNearbyStopsTask(BusesClient api) {
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
			}
		}		
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
}