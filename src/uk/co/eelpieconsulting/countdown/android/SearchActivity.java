package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.StopsService;
import uk.co.eelpieconsulting.countdown.android.services.caching.StopsCache;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.location.LocationService;
import uk.co.eelpieconsulting.countdown.android.views.StopsListAdapter;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

public class SearchActivity extends Activity implements LocationListener {

	private static final String TAG = "SearchActivity";
	
	private TextView status;
	private ListView stopsList;

	private FetchSearchResultsTask fetchSearchResultsTask;

	private Location location;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stopslist);
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setTitle(getString(R.string.search));
		
		status = (TextView) findViewById(R.id.status);
		stopsList = (ListView) findViewById(R.id.list);
		
		handleIntent(getIntent());
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		location = null;
		try {
			LocationService.registerForLocationUpdates(getApplicationContext(), this);						
			final Location bestLastKnownLocation = LocationService.getRecentBestLastKnownLocation(getApplicationContext());
			if (bestLastKnownLocation != null) {
				location = bestLastKnownLocation;
			}				
			
		} catch (NoProvidersException e) {
			status.setText(getString(R.string.no_location_providers));
			status.setVisibility(View.VISIBLE);
		}
		
		//status.setVisibility(View.GONE);
		//stopsList.setVisibility(View.GONE);		
	}

	private void handleIntent(final Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			
			Log.i(TAG, "Searching for: " + query);
			status.setText(getString(R.string.searching));
			status.setVisibility(View.VISIBLE);
			
			fetchSearchResultsTask = new FetchSearchResultsTask(new StopsService(ApiFactory.getApi(getApplicationContext()), new StopsCache(getApplicationContext()), getApplicationContext()));
			fetchSearchResultsTask.execute(query);
			
		} else {
			onSearchRequested();		
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (fetchSearchResultsTask != null && fetchSearchResultsTask.getStatus().equals(Status.RUNNING)) {
			fetchSearchResultsTask.cancel(true);
		}
		LocationService.turnOffLocationUpdates(this.getApplicationContext(), this);
	}
	
	private void showStops(List<Stop> stops) {
		if (stops == null) {
			status.setText("Search results could not be loaded"); // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		status.setText("Found " + stops.size() + " " + (stops.size() != 1 ? "stops" : "stop"));
		status.setVisibility(View.VISIBLE);
				
		Stop nearestStop = null;	// TODO potentially implement nearest stop?
		
		final Location searchLocation = LocationService.isAccurateEnoughForNearbyRoutes(location) ? location : null;		
		final StopsListAdapter stopsListAdapter = new StopsListAdapter(getApplicationContext(), R.layout.stoprow, this, searchLocation, nearestStop, true);
		for (Stop stop : stops) {
			stopsListAdapter.add(stop);			
		}
		
		stopsList.setAdapter(stopsListAdapter);	
		stopsList.setVisibility(View.VISIBLE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		
		return true;
	}
	
	private class FetchSearchResultsTask extends AsyncTask<String, Integer, List<Stop>> {

		private StopsService stopsService;

		public FetchSearchResultsTask(StopsService stopsService) {
			super();
			this.stopsService = stopsService;
		}
		
		@Override
		protected List<Stop> doInBackground(String... params) {
			fetchSearchResultsTask = this;
			final String query = params[0];
			try {				
				return stopsService.searchStops(query);
				
			} catch (ContentNotAvailableException e) {
				Log.w(TAG, "Could not search for stops: " + e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(List<Stop> stops) {
			showStops(stops);
		}
	}
	
	public void onLocationChanged(Location location) {
		Log.i(TAG, "Handset location update received: " + DistanceMeasuringService.makeLocationDescription(location));
		this.location = location;	 // TODO redraw
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

}
