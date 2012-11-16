package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.RouteNameComparator;
import uk.co.eelpieconsulting.countdown.android.services.RoutesService;
import uk.co.eelpieconsulting.countdown.android.services.caching.RoutesCache;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.location.KnownStopLocationProviderService;
import uk.co.eelpieconsulting.countdown.android.services.location.LocationService;
import uk.co.eelpieconsulting.countdown.android.views.RoutesListAdapter;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
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

public class NearbyRoutesListActivity extends Activity implements LocationListener {

	private static final String TAG = "StopsActivity";
		
	private RouteNameComparator routeNameComparator;
	private RoutesService routesService;
	
	private FetchNearbyRoutesTask fetchNearbyRoutesTask;
	private ResolveLocationTask resolveLocationTask;
	
	private TextView status;
	private Stop selectedStop;
	private ListView routesList;
	private Location currentLocation;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopslist);        
		status = (TextView) findViewById(R.id.status);
		routesList = (ListView) findViewById(R.id.list);

		routeNameComparator = new RouteNameComparator();

		routesService = new RoutesService(ApiFactory.getApi(getApplicationContext()), new RoutesCache(getApplicationContext()), getApplicationContext());
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		
		currentLocation = null;
		
		getWindow().setTitle(getString(R.string.near_me));
		routesList.setVisibility(View.GONE);
		
		if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
			selectedStop = (Stop) this.getIntent().getExtras().get("stop");
			listNearbyRoutes(KnownStopLocationProviderService.makeLocationForSelectedStop(selectedStop));

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
		if (fetchNearbyRoutesTask != null && fetchNearbyRoutesTask.getStatus().equals(Status.RUNNING)) {
			fetchNearbyRoutesTask.cancel(true);
		}
		if (resolveLocationTask != null && resolveLocationTask.getStatus().equals(Status.RUNNING)) {
			resolveLocationTask.cancel(true);
		}
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
	
	public void onLocationChanged(Location newLocation) {
		Log.i(TAG, "Handset location update received: " + DistanceMeasuringService.makeLocationDescription(newLocation));
		status.setText("Location found: " + DistanceMeasuringService.makeLocationDescription(newLocation));
		status.setVisibility(View.VISIBLE);
		
		if (LocationService.locationIsSignificantlyDifferentToCurrentLocationToWarrentReloadingResults(currentLocation, newLocation)) {
			listNearbyRoutes(newLocation);
			currentLocation = newLocation;
		}
		
		if (LocationService.isAccurateEnoughForNearbyRoutes(newLocation)) {
			LocationService.turnOffLocationUpdates(this.getApplicationContext(), this);			
		} else {
			status.setText("Hoping for more accurate location than: " + DistanceMeasuringService.makeLocationDescription(newLocation));
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
	
	private void listNearbyRoutes(Location location) {
		if (location.getProvider().equals(KnownStopLocationProviderService.KNOWN_STOP_LOCATION)) {
			status.setText(getString(R.string.searching_for_routes_near) + " " + StopDescriptionService.makeStopTitle(selectedStop));
			status.setVisibility(View.VISIBLE);
		} else {
			status.setText(getString(R.string.routes_near) + " " + DistanceMeasuringService.makeLocationDescription(location));
			status.setVisibility(View.VISIBLE);
		}
		
		fetchNearbyRoutesTask = new FetchNearbyRoutesTask(routesService);
		fetchNearbyRoutesTask.execute(location);		
		return;		
	}
	
	private void showRoutes(Location location, List<Route> routes) {
		if (routes == null) {
			status.setText("Routes could not be loaded");   // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		if (!location.getProvider().equals(KnownStopLocationProviderService.KNOWN_STOP_LOCATION)) {
			status.setText(getString(R.string.routes_near) + " " + DistanceMeasuringService.makeLocationDescription(location));
			status.setVisibility(View.VISIBLE);
		} else {
			status.setVisibility(View.GONE);
		}
								
		final RoutesListAdapter routesListAdapter = new RoutesListAdapter(getApplicationContext(), R.layout.arrival, this, location, selectedStop);
		for (Route route : routes) {
			routesListAdapter.add(route);			
		}
		
		routesListAdapter.sort(routeNameComparator);
		routesList.setAdapter(routesListAdapter);	
		routesList.setVisibility(View.VISIBLE);
		
		resolveLocationTask = new ResolveLocationTask(ApiFactory.getApi(getApplicationContext()));
		resolveLocationTask.execute(location);
	}
	
	private void showLocation(Location location, String locationName) {
		status.setText(getString(R.string.routes_near) + " " + DistanceMeasuringService.makeLocationDescription(locationName, location));
		status.setVisibility(View.VISIBLE);
	}
	
	private class FetchNearbyRoutesTask extends AsyncTask<Location, Integer, List<Route>> {

		private RoutesService routesService;
		private Location location;

		public FetchNearbyRoutesTask(RoutesService routesService) {
			super();
			this.routesService = routesService;
		}
		
		@Override
		protected void onPostExecute(List<Route> routes) {
			showRoutes(location, routes);
		}
		
		@Override
		protected List<Route> doInBackground(Location... params) {
			final Location location = params[0];
			this.location = location;
			try {				
				return routesService.findRoutesWithin(location.getLatitude(), location.getLongitude(), LocationService.NEAR_BY_RADIUS);
				
			} catch (ContentNotAvailableException e) {
				Log.w(TAG, "Could not load routes: " + e.getMessage());
			}
			return null;
		}		
	}
	
	private class ResolveLocationTask extends AsyncTask<Location, Integer, String> {

		private BusesClientService busesClient;
		private Location location;

		public ResolveLocationTask(BusesClientService busesClient) {
			super();
			this.busesClient = busesClient;
		}
		
		@Override
		protected void onPostExecute(String locationName) {
			showLocation(location, locationName);
		}
		
		@Override
		protected String doInBackground(Location... params) {
			final Location location = params[0];
			this.location = location;
			try {				
				return busesClient.resolveLocation(location.getLatitude(), location.getLongitude());		
			} catch (Exception e) {
				Log.w(TAG, "Could not resolve location: " + e.getMessage());
			}
			return null;
		}		
	}
	
}