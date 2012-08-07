package uk.co.eelpieconsulting.countdown.android;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.services.StopNameComparator;
import uk.co.eelpieconsulting.countdown.android.views.StopsListAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class FavouritesActivity extends Activity {

	private FavouriteStopsDAO favouriteStopsDAO;
	private StopNameComparator stopNameComparator;
	private TextView status;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopslist);        
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());
		stopNameComparator = new StopNameComparator();
        
        status = (TextView) findViewById(R.id.status);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setTitle(getString(R.string.favourites));
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
			this.startActivity(new Intent(this, NearbyTabActivity.class));
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
	
	private void showFavourites() {
		Set<Stop> favouriteStops = favouriteStopsDAO.getFavouriteStops();
		if (favouriteStops.isEmpty()) {
			status.setText(R.string.no_favourites_warning);
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		final List<Stop> sortedStops = new ArrayList<Stop>(favouriteStops);
		Collections.sort(sortedStops, stopNameComparator);		
		showStops(sortedStops);
	}
	
	private void showStops(List<Stop> stops) {
		final StopsListAdapter stopsListAdapter = new StopsListAdapter(getApplicationContext(), R.layout.stoprow, this, null);
		for (Stop stop : stops) {
			stopsListAdapter.add(stop);
		}
		
		final ListView stopsList = (ListView) findViewById(R.id.list);
		stopsList.setAdapter(stopsListAdapter);	
	}
	
}