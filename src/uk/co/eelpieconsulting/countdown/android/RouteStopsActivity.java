package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.StopsService;
import uk.co.eelpieconsulting.countdown.android.services.caching.StopsCache;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.location.KnownStopLocationProviderService;
import uk.co.eelpieconsulting.countdown.android.views.StopsListAdapter;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class RouteStopsActivity extends Activity {
	
	private static final String TAG = "RouteStopsActivity";
	
	private TextView status;

	private FetchRouteStopsTask fetchStopsTask;

	private Route selectedRoute;
	private Location location;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopslist);
        status = (TextView) findViewById(R.id.status);        
        selectedRoute = null;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("route") != null) {
        	selectedRoute = (Route) this.getIntent().getExtras().get("route");
        }
        
        location = null;
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("location") != null) {
        	location = (Location) this.getIntent().getExtras().get("location");
        }
        
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
        	final Stop selectedStop = (Stop) this.getIntent().getExtras().get("stop");
        	location = KnownStopLocationProviderService.makeLocationForSelectedStop(selectedStop);
        }
                
        final String title = selectedRoute.getRoute() + " towards " + selectedRoute.getTowards();
        setTitle(title);
		
		status.setText("Loading route stops");
		status.setVisibility(View.VISIBLE);
		
		fetchStopsTask = new FetchRouteStopsTask(new StopsService(ApiFactory.getApi(getApplicationContext()), new StopsCache(getApplicationContext()), getApplicationContext()));
		fetchStopsTask.execute(selectedRoute);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (fetchStopsTask != null && fetchStopsTask.getStatus().equals(Status.RUNNING)) {
			fetchStopsTask.cancel(true);
		}
	}
		
	private void showStops(List<Stop> stops) {
		if (stops == null) {
			status.setText("Stops could not be loaded"); // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		Log.i(TAG, "Found " + stops.size() + " stops");		
		status.setVisibility(View.GONE);

		final Stop nearestStop = DistanceMeasuringService.findClosestOf(stops, location);		
		final StopsListAdapter stopsListAdapter = new StopsListAdapter(getApplicationContext(), R.layout.stoprow, this, location, nearestStop, false);
		for (Stop stop : stops) {
			stopsListAdapter.add(stop);
		}
		
		final ListView stopsList = (ListView) findViewById(R.id.list);
		stopsList.setAdapter(stopsListAdapter);
		
		final Stop highlightedStop = DistanceMeasuringService.findClosestOf(stops, location);
		if (highlightedStop != null) {
			stopsList.setSelection(stops.indexOf(highlightedStop));
		}
	}
	
	private class FetchRouteStopsTask extends AsyncTask<Route, Integer, List<Stop>> {

		private StopsService stopsService;

		public FetchRouteStopsTask(StopsService stopsService) {
			super();
			this.stopsService = stopsService;
		}
				
		@Override
		protected List<Stop> doInBackground(Route... params) {
			final Route route = params[0];
			try {
				return stopsService.getRouteStops(route.getRoute(), route.getRun());
				
			} catch (ContentNotAvailableException e) {
				Log.w(TAG, "Could not load route stops: " + e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(List<Stop> stops) {
			showStops(stops);
		}
		
	}
	
}
