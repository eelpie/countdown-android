package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.StopsService;
import uk.co.eelpieconsulting.countdown.android.services.caching.StopsCache;
import uk.co.eelpieconsulting.countdown.android.views.StopsListAdapter;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
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

public class SearchActivity extends Activity {

	private static final String TAG = "SearchActivity";
	
	private TextView status;
	private ListView stopsList;

	private FetchSearchResultsTask fetchSearchResultsTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stopslist);
        status = (TextView) findViewById(R.id.status);
		stopsList = (ListView) findViewById(R.id.list);
	}
	
	@Override
	protected void onResume() {
		super.onResume();		
        getWindow().setTitle(getString(R.string.search));
		status.setVisibility(View.GONE);
		stopsList.setVisibility(View.GONE);
		
		final Intent intent = getIntent();
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
	}
	
	private void showStops(List<Stop> stops) {
		if (stops == null) {
			status.setText("Search results could not be loaded"); // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		status.setText("Found " + stops.size() + " " + (stops.size() != 1 ? "stops" : "stop"));
		status.setVisibility(View.VISIBLE);
				
		final StopsListAdapter stopsListAdapter = new StopsListAdapter(getApplicationContext(), R.layout.stoprow, this, null);
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

}
