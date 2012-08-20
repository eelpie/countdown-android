package uk.co.eelpieconsulting.countdown.android.activities.maps;

import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.AlertsActivity;
import uk.co.eelpieconsulting.countdown.android.FavouritesActivity;
import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.SearchActivity;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.StopsService;
import uk.co.eelpieconsulting.countdown.android.services.caching.StopsCache;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.views.balloons.RouteOverlayItem;
import uk.co.eelpieconsulting.countdown.android.views.balloons.StopOverlayItem;
import uk.co.eelpieconsulting.countdown.android.views.balloons.StopsItemizedOverlay;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.maps.Overlay;

public class RouteMapActivity extends BaseMapActivity {

	private static final String TAG = "RouteMapActivity";
		
	private TextView status;

	private Route selectedRoute;
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		status = (TextView) findViewById(R.id.status);
		
	    if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("route") != null) {
        	selectedRoute = (Route) this.getIntent().getExtras().get("route");
	    }
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		status.setText(getString(R.string.waiting_for_location));
		status.setVisibility(View.VISIBLE);
		
		currentLocation = null;
		mapView.getController().setZoom(12);
		   
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
		
		locationCircleOverlay.setPoint(location);
		mapView.postInvalidate();
		
		if (currentLocation == null) {
			zoomMapToLocation(location);		
		}
		
		currentLocation = location;
	}
	
	private void showRouteStops(List<Stop> stops) {
		if (stops == null) {
			status.setText("Route stops could not be loaded"); // TODO why?
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
		overlays.add(new RouteOverlayItem(stops));
		mapView.postInvalidate();
	}
	
	private class FetchRouteStopsTask extends AsyncTask<Route, Integer, List<Stop>> {

		private StopsService stopsService;

		public FetchRouteStopsTask(StopsService api) {
			super();
			this.stopsService = api;
		}
		
		@Override
		protected void onPostExecute(List<Stop> stops) {
			showRouteStops(stops);
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