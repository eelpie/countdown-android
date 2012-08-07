package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.AsyncTask.Status;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SearchActivity extends Activity {

	private static final String TAG = "SearchActivity";
	
	private TextView status;

	private FetchSearchResultsTask fetchSearchResultsTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stops);
        status = (TextView) findViewById(R.id.status);
	}
	
	@Override
	protected void onResume() {
		super.onResume();		
        getWindow().setTitle(getString(R.string.search));
		
		final Intent intent = getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			
			Log.i(TAG, "Searching for: " + query);
			status.setText(getString(R.string.searching));
			status.setVisibility(View.VISIBLE);
			
			fetchSearchResultsTask = new FetchSearchResultsTask(ApiFactory.getApi(getApplicationContext()));
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
	}
	
	private void showStops(List<Stop> stops) {
		status.setText("Found " + stops.size() + " " + (stops.size() != 1 ? "stops" : "stop"));
		status.setVisibility(View.VISIBLE);
		
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		for (Stop stop : stops) {
			Log.i(TAG, "Found: " + stop.toString());
			stopsList.addView(StopDescriptionService.makeStopView(stop, getApplicationContext(), this));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.favourites:
			this.startActivity(new Intent(this, FavouritesActivity.class));
			return true;
		}
		return false;
	}
	
	private class FetchSearchResultsTask extends AsyncTask<String, Integer, List<Stop>> {

		private BusesClientService busesClientService;

		public FetchSearchResultsTask(BusesClientService busesClientService) {
			super();
			this.busesClientService = busesClientService;
		}
		
		@Override
		protected List<Stop> doInBackground(String... params) {
			fetchSearchResultsTask = this;
			final String query = params[0];
			try {				
				return busesClientService.searchStops(query);	// TODO move to caching service
				
			} catch (HttpFetchException e) {
				Log.w(TAG, "Could search for stops stops: " + e.getMessage());
			} catch (ParsingException e) {
				Log.w(TAG, "Could search for stops stops: " + e.getMessage());
			} catch (NetworkNotAvailableException e) {
				Log.w(TAG, "Could search for stops stops: " + e.getMessage());
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(List<Stop> stops) {
			showStops(stops);
		}
	}

}
