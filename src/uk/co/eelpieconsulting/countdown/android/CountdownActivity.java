package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.countdown.android.api.CountdownApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.services.DistanceMeasuringService;
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
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CountdownActivity extends Activity {
	
	private static final String TAG = "CountdownActivity";
	
	private CountdownApi api;
	private FavouriteStopsDAO favouriteStopsDAO;
	private FetchArrivalsTask fetchArrivalsTask;
	
	private Stop selectedStop;

	private MenuItem favouriteMenuItem;
	private TextView status;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);
        status = (TextView) findViewById(R.id.status);
        
        api = CountdownApiFactory.getApi();
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());        
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
				final String lastKnownLocationMessage = "Last known location is: " + DistanceMeasuringService.makeLocationDescription(lastKnownLocation);				
				Log.i(TAG, lastKnownLocationMessage);
				final Toast toast = Toast.makeText(getApplicationContext(), lastKnownLocationMessage, Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				
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
		status.setText("Loading arrivals for stop: " + selectedStop.getId());
		status.setVisibility(View.VISIBLE);
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
		menu.add(0, 1, 0, R.string.favourites);
		menu.add(0, 4, 0, R.string.find_stops);
		favouriteMenuItem = menu.add(0, 2, 0, chooseFavouriteAction());
		return true;
	}

	private int chooseFavouriteAction() {
		int favouriteAction = R.string.add_to_favourites;
		if (selectedStop != null) {
			if (favouriteStopsDAO.isFavourite(selectedStop)) {
				favouriteAction = R.string.remove_favourite;				
			}		
		}
		return favouriteAction;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			this.startActivity(new Intent(this, FavouritesActivity.class));
			return true;

		case 2:
			if (selectedStop != null) {
				if (favouriteStopsDAO.isFavourite(selectedStop)) {
					favouriteStopsDAO.removeFavourite(selectedStop);
					final Toast toast = Toast.makeText(getApplicationContext(), selectedStop.getName() + " removed from favourites", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					
				} else {
					favouriteStopsDAO.addFavourite(selectedStop);
					final Toast toast = Toast.makeText(getApplicationContext(), selectedStop.getName() + " added to favourites", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}			
			favouriteMenuItem.setTitle(getString(chooseFavouriteAction()));
			return true;
			
		case 3:			
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
			output.append(getText(R.string.estimated_wait) + ": " + secondsToMinutes(arrival));
			output.append("\n\n");
		}
		
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		TextView arrivalTextView = new TextView(getApplicationContext());
		arrivalTextView.setText(output.toString());
		stopsList.addView(arrivalTextView);
		status.setVisibility(View.GONE);
	}

	private String secondsToMinutes(Arrival arrival) {
		final long minutes = arrival.getEstimatedWait() / 60;
		if (minutes == 0) {
			return  getString(R.string.due);
		}
		if (minutes == 1) {
			return "1 " + getString(R.string.minute);
		}
		return minutes + " " +  getString(R.string.minutes);
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
