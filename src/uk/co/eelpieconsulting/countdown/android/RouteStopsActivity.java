package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.StopsService;
import uk.co.eelpieconsulting.countdown.android.services.caching.StopsCache;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RouteStopsActivity extends Activity {
	
	private static final String TAG = "RouteStopsActivity";
		
	private Route selectedRoute;

	private TextView status;

	private FetchRouteStopsTask fetchStopsTask;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);
        status = (TextView) findViewById(R.id.status);        
        selectedRoute = null;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("route") != null) {
        	selectedRoute = (Route) this.getIntent().getExtras().get("route");
        }
                
        final String title = selectedRoute.getRoute() + " towards " + selectedRoute.getTowards();
		getWindow().setTitle(title);
		
		status.setText("Loading route stops");
		status.setVisibility(View.VISIBLE);
		
		fetchStopsTask = new FetchRouteStopsTask(new StopsService(ApiFactory.getApi(getApplicationContext()), new StopsCache(getApplicationContext())));
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
		
		status.setText("Displaying");
		status.setVisibility(View.VISIBLE);
		
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		for (Stop stop : stops) {
			stopsList.addView(StopDescriptionService.makeStopView(stop, getApplicationContext(), this));
		}
		status.setVisibility(View.GONE);
		
		final TextView credit = new TextView(getApplicationContext());
		credit.setText(getString(R.string.tfl_credit));
		stopsList.addView(credit);
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
	
}
