package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.api.CountdownApi;
import uk.co.eelpieconsulting.countdown.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.countdown.exceptions.ParsingException;
import uk.co.eelpieconsulting.countdown.model.Arrival;
import uk.co.eelpieconsulting.countdown.model.Stop;
import uk.co.eelpieconsulting.countdown.model.StopBoard;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class CountdownActivity extends Activity {

	private CountdownApi api;
	private FavouriteStopsDAO favouriteStopsDAO;
	private TextView arrivalsTextView;
	private FetchArrivalsTask fetchArrivalsTask;
	
	private Stop selectedStop;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        api = new CountdownApi("http://countdown.api.tfl.gov.uk");
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());        
        arrivalsTextView = (TextView) findViewById(R.id.arrivals);     
        selectedStop = null;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
        	selectedStop = (Stop) this.getIntent().getExtras().get("stop");
        }
        
        if (selectedStop == null) {
        	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);        	
        	Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        	if (lastKnownLocation != null) {
        		selectedStop = favouriteStopsDAO.getClosestFavouriteStopTo(lastKnownLocation);
        	} else {
        		selectedStop = favouriteStopsDAO.getFirstFavouriteStop();
        	}
        }
        
        if (selectedStop == null) {
        	return;
        }
        
		final String title = selectedStop.getName() + (selectedStop.getStopIndicator() != null ? " (" + selectedStop.getStopIndicator() + ") " : "");
		getWindow().setTitle(title);
		arrivalsTextView.setText("Loading arrivals for stop: " + selectedStop.getId());
		loadArrivals(selectedStop.getId());
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (fetchArrivalsTask != null && fetchArrivalsTask.getStatus().equals(Status.RUNNING)) {
			fetchArrivalsTask.cancel(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "Favourites");
		menu.add(0, 4, 0, "Find Stops");
		if (selectedStop != null) {
			if (!favouriteStopsDAO.isFavourite(selectedStop)) {
				menu.add(0, 2, 0, "Add to Favourites");
			} else {
				menu.add(0, 3, 0, "Remove Favourite");
			}
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			this.startActivity(new Intent(this, FavouritesActivity.class));
			return true;

		case 2:			
			favouriteStopsDAO.addFavourite(selectedStop);
			return true;
			
		case 3:			
			favouriteStopsDAO.removeFavourite(selectedStop);
			return true;
			
		case 4:
			this.startActivity(new Intent(this, StopsActivity.class));
			return true;
		}
		return false;
	}
	
	private void loadArrivals(int stopId) {
		fetchArrivalsTask = new FetchArrivalsTask(api);
		fetchArrivalsTask.execute(stopId);
	}
		
	private void renderStopboard(StopBoard stopboard) {
		final StringBuilder output = new StringBuilder();
		for (Arrival arrival : stopboard.getArrivals()) {
			output.append(arrival.getRouteName() + " to " + arrival.getDestination() + "\n");
			output.append("Estimated wait: " + secondsToMinutes(arrival));
			output.append("\n\n");
		}
		arrivalsTextView.setText(output.toString());		
	}

	private String secondsToMinutes(Arrival arrival) {
		final long minutes = arrival.getEstimatedWait() / 60;
		if (minutes == 0) {
			return "due";			
		}
		return minutes + " minutes";
	}
	
	private class FetchArrivalsTask extends AsyncTask<Integer, Integer, StopBoard> {

		private CountdownApi api;

		public FetchArrivalsTask(CountdownApi api) {
			super();
			this.api = api;
		}

		@Override
		protected StopBoard doInBackground(Integer... params) {
			final int stopId = params[0];
			try {				
				return api.getStopBoard(stopId);
			} catch (HttpFetchException e) {
				throw new RuntimeException(e);
			} catch (ParsingException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		protected void onPostExecute(StopBoard stopboard) {
			renderStopboard(stopboard);
		}
		
	}
	
}
