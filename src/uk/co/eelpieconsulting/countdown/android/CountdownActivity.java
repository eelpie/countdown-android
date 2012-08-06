package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.buses.client.model.Arrival;
import uk.co.eelpieconsulting.buses.client.model.StopBoard;
import uk.co.eelpieconsulting.busroutes.model.Message;
import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.daos.SeenMessagesDAO;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.MessageService;
import uk.co.eelpieconsulting.countdown.android.services.caching.MessageCache;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.location.LocationService;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.views.MessageDescriptionService;
import uk.co.eelpieconsulting.countdown.android.views.RouteClicker;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CountdownActivity extends Activity {
	
	private static final String TAG = "CountdownActivity";
	
	private BusesClientService api;
	private FavouriteStopsDAO favouriteStopsDAO;
	private MessageService messageService;
	
	private FetchArrivalsTask fetchArrivalsTask;
	private FetchMessagesTask fetchMessagesTask;
	
	private Stop selectedStop;

	private MenuItem favouriteMenuItem;
	private TextView status;

	private LinearLayout stopsList;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);
        status = (TextView) findViewById(R.id.status);
        
        api = ApiFactory.getApi(getApplicationContext());
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());        
        selectedStop = null;
        
		stopsList = (LinearLayout) findViewById(R.id.stopsList);
		
		messageService = new MessageService(ApiFactory.getApi(getApplicationContext()), new MessageCache(getApplicationContext()), new SeenMessagesDAO(getApplicationContext()));
    }
	
	@Override
	protected void onResume() {
		super.onResume();
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
        	selectedStop = (Stop) this.getIntent().getExtras().get("stop");
        }
        
        if (selectedStop == null && !favouriteStopsDAO.hasFavourites()) {
        	status.setText(R.string.no_favourites_warning);
        	status.setVisibility(View.VISIBLE);
        	return;
        }
        
        if (selectedStop == null) {
    		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        	final Location lastKnownLocation = LocationService.getBestLastKnownLocation(locationManager);        	
        	if (lastKnownLocation != null) {
				final String lastKnownLocationMessage = "Last known location is: " + DistanceMeasuringService.makeLocationDescription(lastKnownLocation);				
				Log.i(TAG, lastKnownLocationMessage);				
				selectedStop = favouriteStopsDAO.getClosestFavouriteStopTo(lastKnownLocation);
				Log.i(TAG, "Choosing closest favourite stop based on last known location: " + selectedStop.getName());
				        		
        	} else {
        		selectedStop = favouriteStopsDAO.getFirstFavouriteStop();
        		Log.i(TAG, "As no last known position is available defaulting to first favourite stop: " + selectedStop.getName());
        	}
        }
        
		final String title = StopDescriptionService.makeStopTitle(selectedStop);
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
		if (fetchMessagesTask != null && fetchMessagesTask.getStatus().equals(Status.RUNNING)) {
			fetchMessagesTask.cancel(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, R.string.favourites);
		menu.add(0, 4, 0, R.string.near_me);
		if (selectedStop != null) {
			menu.add(0, 5, 0, R.string.near_this);
			favouriteMenuItem = menu.add(0, 2, 0, chooseFavouriteAction());
		}
		menu.add(0, 6, 0, R.string.alerts);
		menu.add(0, 7, 0, R.string.search);
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
			
		case 4:
			this.startActivity(new Intent(this, NearbyTabActivity.class));
			return true;
			
		case 5:
			Intent intent = new Intent(this, NearThisTabActivity.class);
			intent.putExtra("stop", selectedStop);
			this.startActivity(intent);
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
	
	private void loadArrivals(int stopId) {
		fetchArrivalsTask = new FetchArrivalsTask(api);
		fetchArrivalsTask.execute(stopId);
	}
	
	private void loadMessages(int stopId) {
		fetchMessagesTask = new FetchMessagesTask(messageService);
		fetchMessagesTask.execute(stopId);
	}
	
	private void renderStopboard(StopBoard stopboard) {
		stopsList.removeAllViews();
		if (stopboard == null) {
			status.setText("Arrivals could not be loaded"); // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		final String towards = selectedStop.getTowards() != null ? "Towards " + selectedStop.getTowards() + "\n" : "";
		status.setText(towards + StopDescriptionService.routesDescription(selectedStop.getRoutes()));
		status.setVisibility(View.VISIBLE);
		
		LayoutInflater mInflater = LayoutInflater.from(this.getApplicationContext());
		for (Arrival arrival : stopboard.getArrivals()) {		
			stopsList.addView(createArrivalView(mInflater, arrival, selectedStop));
		}		
		loadMessages(selectedStop.getId());
	}

	private View createArrivalView(LayoutInflater mInflater, Arrival arrival, Stop stop) {
		final View arrivalView = mInflater.inflate(R.layout.arrival, null);		
		final TextView routeTextView = (TextView) arrivalView.findViewById(R.id.routeName);
		routeTextView.setText(arrival.getRoute().getRoute());			
		
		final TextView bodyTextView = (TextView) arrivalView.findViewById(R.id.body);
		bodyTextView.setText(arrival.getRoute().getTowards() + "\n" + StopDescriptionService.secondsToMinutes(arrival.getEstimatedWait(), getApplicationContext()));
		
		arrivalView.setOnClickListener(new RouteClicker(this, arrival.getRoute(), stop, null));
		return arrivalView;
	}
	
	private void renderMessages(List<MultiStopMessage> messages) {		
		if (messages == null) {
			return;
		}
				
		for (Message message : messages) {
			stopsList.addView(MessageDescriptionService.makeStopDescription(message, getApplicationContext()));	
		}
		
		final TextView credit = new TextView(getApplicationContext());
		credit.setText(getString(R.string.tfl_credit));
		stopsList.addView(credit);
	}
	
	private class FetchArrivalsTask extends AsyncTask<Integer, Integer, StopBoard> {

		private BusesClientService api;

		public FetchArrivalsTask(BusesClientService api) {
			super();
			this.api = api;
		}

		@Override
		protected StopBoard doInBackground(Integer... params) {
			final int stopId = params[0];
			try {
				return api.getStopBoard(stopId);
			} catch (HttpFetchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NetworkNotAvailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(StopBoard stopboard) {
			renderStopboard(stopboard);
		}
		
	}
	
	private class FetchMessagesTask extends AsyncTask<Integer, Integer, List<MultiStopMessage>> {

		private MessageService messageService;

		public FetchMessagesTask(MessageService messageService) {
			super();
			this.messageService = messageService;
		}

		@Override
		protected List<MultiStopMessage> doInBackground(Integer... params) {
			final int stopId = params[0];
			try {
				return messageService.getStopMessages(stopId);
			} catch (ContentNotAvailableException e) {
				Log.w(TAG, "Could new load messages: " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<MultiStopMessage> messages) {
			renderMessages(messages);
		}		
	}
	
}
