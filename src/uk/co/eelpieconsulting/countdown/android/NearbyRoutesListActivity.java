package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.services.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.views.RouteClicker;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NearbyRoutesListActivity extends Activity implements LocationListener {

	private static final String TAG = "StopsActivity";
	
	private static final int STOP_SEARCH_RADIUS = 250;
	private static final String KNOWN_STOP_LOCATION = "knownStopLocation";
	
	private TextView status;

	private FetchNearbyRoutesTask fetchNearbyRoutesTask;

	private Stop selectedStop;

	private LinearLayout routesList;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);        
		status = (TextView) findViewById(R.id.status);
		routesList = (LinearLayout) findViewById(R.id.stopsList);
	}
    
	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setTitle(getString(R.string.near_me));
		if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
			selectedStop = (Stop) this.getIntent().getExtras().get("stop");
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
		if (fetchNearbyRoutesTask != null && fetchNearbyRoutesTask.getStatus().equals(Status.RUNNING)) {
			fetchNearbyRoutesTask.cancel(true);
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
			onSearchRequested();
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
		if (location.getProvider().equals(KNOWN_STOP_LOCATION)) {
			status.setText(getString(R.string.searching_for_routes_near) + " " + StopDescriptionService.makeStopTitle(selectedStop));
			status.setVisibility(View.VISIBLE);
		} else {
			status.setText(getString(R.string.routes_near) + " " + DistanceMeasuringService.makeLocationDescription(location));
			status.setVisibility(View.VISIBLE);
		}
		
		routesList.removeAllViews();
		
		fetchNearbyRoutesTask = new FetchNearbyRoutesTask(ApiFactory.getApi(getApplicationContext()));
		fetchNearbyRoutesTask.execute(location);		
		return;		
	}
	
	private void showRoutes(Location location, List<Route> routes) {
		if (routes == null) {
			status.setText("Routes could not be loaded");   // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		

		if (!location.getProvider().equals(KNOWN_STOP_LOCATION)) {
			status.setText(getString(R.string.routes_near) + " " + DistanceMeasuringService.makeLocationDescription(location));
			status.setVisibility(View.VISIBLE);
		} else {
			status.setVisibility(View.GONE);
		}
				
		final LayoutInflater mInflater = LayoutInflater.from(this.getApplicationContext());
		for (Route route : routes) {
			routesList.addView(createRouteView(mInflater, route));	
		}
		
		final TextView credit = new TextView(getApplicationContext());
		credit.setText(getString(R.string.tfl_credit));
		routesList.addView(credit);
	}
	
	private View createRouteView(LayoutInflater mInflater, Route route) {
		final View routeView = mInflater.inflate(R.layout.arrival, null);		

		final TextView routeTextView = (TextView) routeView.findViewById(R.id.routeName);
		routeTextView.setText(route.getRoute());			
		
		final TextView bodyTextView = (TextView) routeView.findViewById(R.id.body);
		bodyTextView.setText(getString(R.string.towards) + " " + route.getTowards());
		
		routeView.setOnClickListener(new RouteClicker(this, route));
		return routeView;
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
	
	private class FetchNearbyRoutesTask extends AsyncTask<Location, Integer, List<Route>> {

		private BusesClientService api;
		private Location location;

		public FetchNearbyRoutesTask(BusesClientService api) {
			super();
			this.api = api;
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
				return api.findRoutesWithin(location.getLatitude(), location.getLongitude(), STOP_SEARCH_RADIUS);				
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
	
}