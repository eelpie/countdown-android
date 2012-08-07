package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.StopsService;
import uk.co.eelpieconsulting.countdown.android.services.caching.StopsCache;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
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
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class RouteMapActivity extends MapActivity implements LocationListener {

	private static final String TAG = "StopsActivity";
	
	private static final int STOP_SEARCH_RADIUS = 250;
	
	private TextView status;
	private MapView mapView;

	private Route selectedRoute;
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapstops);
		status = (TextView) findViewById(R.id.status);
		mapView = (MapView) findViewById(R.id.map);
		
		mapView.setBuiltInZoomControls(false);
		mapView.setClickable(true);
		
	    if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("route") != null) {
        	selectedRoute = (Route) this.getIntent().getExtras().get("route");
	    }
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		registerForLocationUpdates();
		
		FetchRouteStopsTask fetchRouteStopsTask = new FetchRouteStopsTask(new StopsService(ApiFactory.getApi(getApplicationContext()), new StopsCache(getApplicationContext())));
		fetchRouteStopsTask.execute(selectedRoute);
	}
	
	@Override
	protected void onPause() {
		super.onPause();		
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
				
		if (location.hasAccuracy() && location.getAccuracy() < STOP_SEARCH_RADIUS) {
			zoomToUserLocation(location);
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
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	private void zoomToUserLocation(Location location) {		
		mapView.getController().animateTo(GeoPointFactory.createGeoPointForLatLong(location.getLatitude(), location.getLongitude()));
        mapView.getController().setZoom(12);
		return;
	}
	
	private void showStops(List<Stop> stops) {
		if (stops == null) {
			status.setText("Stops could not be loaded"); // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		Drawable drawable = getResources().getDrawable(R.drawable.marker);
		final StopsItemizedOverlay itemizedOverlay = new StopsItemizedOverlay(drawable, mapView);
		for (Stop stop : stops) {		
			itemizedOverlay.addOverlay(new StopOverlayItem(stop));
		}
		
		final List<Overlay> overlays = mapView.getOverlays();
		overlays.add(itemizedOverlay);
		mapView.postInvalidate();
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
	
	private class FetchRouteStopsTask extends AsyncTask<Route, Integer, List<Stop>> {

		private StopsService stopsService;

		public FetchRouteStopsTask(StopsService api) {
			super();
			this.stopsService = api;
		}
		
		@Override
		protected void onPostExecute(List<Stop> stops) {
			showStops(stops);
		}
		
		@Override
		protected List<Stop> doInBackground(Route... params) {
			final Route route = params[0];
			try {
				return stopsService.getRouteStops(route.getRoute(), route.getRun());
			} catch (ContentNotAvailableException e) {
				Log.w(TAG, "Could not load stops for route: " + e.getMessage());
			}
			return null;
		}		
	}
	
}