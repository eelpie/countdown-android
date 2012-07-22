package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.CountdownApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.services.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.LocationService;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import uk.co.eelpieconsulting.countdown.api.CountdownApi;
import uk.co.eelpieconsulting.countdown.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.countdown.exceptions.ParsingException;
import uk.co.eelpieconsulting.countdown.model.Arrival;
import uk.co.eelpieconsulting.countdown.model.StopBoard;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
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
       
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60 * 5000, 2500, new NullLocationListener());

    }
	
	@Override
	protected void onResume() {
		super.onResume();
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
        	selectedStop = (Stop) this.getIntent().getExtras().get("stop");
        }
        
        if (selectedStop == null) {
    		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        	final Location lastKnownLocation = LocationService.getBestLastKnownLocation(locationManager);        	
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
        	status.setText("No favourite stops set. Use find stops to locate nearby stops.");
        	return;
        }
        
		final String title = selectedStop.getName() + (selectedStop.getIndicator() != null ? " (" + selectedStop.getIndicator() + ") " : "");
		getWindow().setTitle(title);
		status.setText("Loading arrivals for stop: " + title);
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
		if (selectedStop != null) {
			menu.add(0, 5, 0, R.string.near_this);
			favouriteMenuItem = menu.add(0, 2, 0, chooseFavouriteAction());
		}
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
			this.startActivity(new Intent(this, NearbyActivity.class));
			return true;
			
		case 5:
			Intent intent = new Intent(this, NearbyActivity.class);
			intent.putExtra("stop", selectedStop);
			this.startActivity(intent);
			return true;
		}
		return false;
	}
	
	private void loadArrivals(int stopId) {
		fetchArrivalsTask = new FetchArrivalsTask(api);
		fetchArrivalsTask.execute(stopId);
	}
		
	private void renderStopboard(StopBoard stopboard) {		
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		
		final String towards = selectedStop.getTowards() != null ? "Towards " + selectedStop.getTowards() + "\n" : "";
		status.setText(towards + StopDescriptionService.routesDescription(selectedStop.getRoutes()));
		status.setVisibility(View.VISIBLE);
		
		LayoutInflater mInflater = LayoutInflater.from(this.getApplicationContext());
		for (Arrival arrival : stopboard.getArrivals()) {		
			final View arrivalView = mInflater.inflate(R.layout.arrival, null);		
			final TextView routeTextView = (TextView) arrivalView.findViewById(R.id.routeName);
			routeTextView.setText(arrival.getRoute().getRoute());			
			
			final TextView bodyTextView = (TextView) arrivalView.findViewById(R.id.body);
			bodyTextView.setText(arrival.getRoute().getTowards() + "\n" + secondsToMinutes(arrival));
			
			arrivalView.setOnClickListener(new RouteClicker(arrival.getRoute()));			
			
			stopsList.addView(arrivalView);
		}		
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
	
	private class RouteClicker implements OnClickListener {
		
		private Route route;

		public RouteClicker(Route route) {
			this.route = route;
		}

		public void onClick(View view) {
			Intent intent = getIntentForContentsType(view.getContext(), route);
			intent.putExtra("route", route);
			startActivity(intent);
		}

		private Intent getIntentForContentsType(Context context, Route route) {
			return new Intent(context, RouteActivity.class);
		}
	}
	
	private class NullLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			Log.i(TAG, "Location changed to: " + DistanceMeasuringService.makeLocationDescription(location));			
		}

		public void onProviderDisabled(String provider) {			
		}

		public void onProviderEnabled(String provider) {			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
		
	}
	
}
