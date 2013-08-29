package uk.co.eelpieconsulting.countdown.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.services.NearestFavouriteStopService;
import uk.co.eelpieconsulting.countdown.android.services.StopNameComparator;
import uk.co.eelpieconsulting.countdown.android.views.StopsListAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class FavouritesActivity extends Activity {
	
	private static final String TAG = "FavouritesActivity";

	private FavouriteStopsDAO favouriteStopsDAO;
	private StopNameComparator stopNameComparator;
	private TextView status;
	private ListView stopsList;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());
        stopNameComparator = new StopNameComparator();
        
        setContentView(R.layout.stopslist);        
        setTitle(getString(R.string.favourites));
        
        status = (TextView) findViewById(R.id.status);
        stopsList = (ListView) findViewById(R.id.list);
        
        final boolean wasCalledFromStartup = this.getIntent().getExtras() != null && this.getIntent().getExtras().get("startup") != null;
        if (wasCalledFromStartup) {
        	final Stop selectedStop = new NearestFavouriteStopService().selectNearestFavouriteStopBasedOnLastKnownLocation(getApplicationContext());
        	if (selectedStop != null) {
        		Log.d(TAG, "Redirecting user to closest favourite stop on application startup");
        		final Intent intent = new Intent(this, StopActivity.class);
        		intent.putExtra("stop", selectedStop);
        		this.startActivity(intent);
        		return;
        	}
        }
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		status.setVisibility(View.GONE);
		stopsList.setVisibility(View.GONE);
		
		showFavourites();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.favourites_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {			
		case R.id.nearby:
			this.startActivity(new Intent(this, NearbyActivity.class));
			return true;	
						
		case R.id.alerts:
			this.startActivity(new Intent(this, AlertsActivity.class));
			return true;
			
		case R.id.search:
			this.startActivity(new Intent(this, SearchActivity.class));
			return true;
			
		case R.id.about:
			this.startActivity(new Intent(this, AboutActivity.class));
			return true;
		}
		return false;
	}
	
	private void showFavourites() {		
		Set<Stop> favouriteStops = favouriteStopsDAO.getFavouriteStops();
		if (favouriteStops.isEmpty()) {
			status.setText(R.string.no_favourites_warning);
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		showStops(new ArrayList<Stop>(favouriteStops));
	}
	
	private void showStops(List<Stop> stops) {
		final StopsListAdapter stopsListAdapter = new StopsListAdapter(getApplicationContext(), R.layout.stoprow, this, null, null, true);
		for (Stop stop : stops) {
			stopsListAdapter.add(stop);
		}
		
		stopsListAdapter.sort(stopNameComparator);
		stopsList.setAdapter(stopsListAdapter);	
		stopsList.setVisibility(View.VISIBLE);
	}
	
}