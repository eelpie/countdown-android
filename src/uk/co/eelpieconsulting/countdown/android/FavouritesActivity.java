package uk.co.eelpieconsulting.countdown.android;

import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FavouritesActivity extends Activity {

	private FavouriteStopsDAO favouriteStopsDAO;
	private TextView status;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);        
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());
        
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
		menu.add(0, 1, 0, R.string.near_me);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			this.startActivity(new Intent(this, NearbyMapActivity.class));
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
		showStops(favouriteStops);
	}

	private void showStops(Set<Stop> stops) {
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		for (Stop stop : stops) {
			stopsList.addView(StopDescriptionService.makeStopView(stop, getApplicationContext(), this));
		}
	}
	
}