package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.model.Arrival;
import uk.co.eelpieconsulting.buses.client.model.StopBoard;
import uk.co.eelpieconsulting.busroutes.model.Message;
import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.daos.SeenMessagesDAO;
import uk.co.eelpieconsulting.countdown.android.services.ArrivalsService;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.MessageService;
import uk.co.eelpieconsulting.countdown.android.services.caching.MessageCache;
import uk.co.eelpieconsulting.countdown.android.views.MessageDescriptionService;
import uk.co.eelpieconsulting.countdown.android.views.RouteClicker;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StopActivity extends Activity {
	
	private static final String TAG = "CountdownActivity";
	
	private FavouriteStopsDAO favouriteStopsDAO;
	private MessageService messageService;
	
	private FetchArrivalsTask fetchArrivalsTask;
	private FetchMessagesTask fetchMessagesTask;
	private ArrivalsService arrivalsService;
	
	private TextView status;
	private LinearLayout stopsList;
	private Menu menu;

	private Stop selectedStop;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		Log.i(TAG, "Creating with selected stop: " + selectedStop);
		Log.i(TAG, "Creating with savedInstanceState: " + savedInstanceState);

		
        setContentView(R.layout.stops);
        
	    getActionBar().setDisplayHomeAsUpEnabled(true);
        
        status = (TextView) findViewById(R.id.status);
        
        arrivalsService = ApiFactory.getArrivalsService(getApplicationContext());
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());        
        selectedStop = null;
        
		stopsList = (LinearLayout) findViewById(R.id.stopsList);
		
		messageService = new MessageService(ApiFactory.getApi(getApplicationContext()), new MessageCache(getApplicationContext()), new SeenMessagesDAO(getApplicationContext()), getApplicationContext());
		
		if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
			selectedStop = (Stop) this.getIntent().getExtras().get("stop");
		}
    }
	
	@Override
	protected void onResume() {	
		super.onResume();
		Log.i(TAG, "Resuming with selected stop: " + selectedStop);
		
		if (selectedStop != null) {
			final String title = StopDescriptionService.makeStopTitle(selectedStop);
			getWindow().setTitle(title);
		
			status.setText("Loading arrivals for stop: " + title);
			status.setVisibility(View.VISIBLE);
			loadArrivals(selectedStop.getId());
		}
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
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.stop_menu, menu);		
		this.menu = menu;
		
		setupFavouriteMenuOptions(menu, selectedStop);
		return true;	
	}
	
	private void setupFavouriteMenuOptions(Menu menu, Stop selectedStop) {		
		if (selectedStop == null) {
			menu.findItem(R.id.addfavourite).setVisible(false);
			menu.findItem(R.id.removefavourite).setVisible(false);
		} else {
			final boolean isFavourite = favouriteStopsDAO.isFavourite(selectedStop);
			menu.findItem(R.id.addfavourite).setVisible(!isFavourite);
			menu.findItem(R.id.removefavourite).setVisible(isFavourite);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
					
		case R.id.refresh:
			if (selectedStop != null) {
				loadArrivals(selectedStop.getId());
			}
			return true;
			
		case R.id.addfavourite:
			if (selectedStop != null) {
				if (!favouriteStopsDAO.isFavourite(selectedStop)) {					
					favouriteStopsDAO.addFavourite(selectedStop);
					final Toast toast = Toast.makeText(getApplicationContext(), StopDescriptionService.makeStopTitle(selectedStop) + " added to favourites", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					setupFavouriteMenuOptions(menu, selectedStop);
				}
			}			
			return true;
			
		case R.id.removefavourite:
			if (selectedStop != null) {
				if (favouriteStopsDAO.isFavourite(selectedStop)) {
					favouriteStopsDAO.removeFavourite(selectedStop);
					final Toast toast = Toast.makeText(getApplicationContext(), StopDescriptionService.makeStopTitle(selectedStop) + " removed from favourites", Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					setupFavouriteMenuOptions(menu, selectedStop);					
				}
			}			
			return true;
			
		case R.id.nearthis:
			Intent intent = new Intent(this, NearThisStopActivity.class);
			intent.putExtra("stop", selectedStop);
			this.startActivity(intent);
			return true;				
		}
		
		return false;
	}
	
	private void loadArrivals(int stopId) {
		stopsList.removeAllViews();
		
		fetchArrivalsTask = new FetchArrivalsTask(arrivalsService);
		fetchArrivalsTask.execute(stopId);
	}
	
	private void loadMessages(int stopId) {
		fetchMessagesTask = new FetchMessagesTask(messageService);
		fetchMessagesTask.execute(stopId);
	}
	
	private void renderStopboard(StopBoard stopboard, ContentNotAvailableException exception) {
		if (stopboard == null) {
			status.setText("Arrivals could not be loaded: " + exception.getMessage());
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

		private ArrivalsService arrivalsService;
		private ContentNotAvailableException exception;

		public FetchArrivalsTask(ArrivalsService arrivalsService) {
			super();
			this.arrivalsService = arrivalsService;
		}

		@Override
		protected StopBoard doInBackground(Integer... params) {
			final int stopId = params[0];
			try {
				return arrivalsService.getStopBoard(stopId);
			} catch (ContentNotAvailableException e) {
				this.exception = e;
				Log.w(TAG, "Arrivals data was not available: " + e.getMessage());
			}
			return null;
		}

		@Override
		protected void onPostExecute(StopBoard stopboard) {
			renderStopboard(stopboard, exception);
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
