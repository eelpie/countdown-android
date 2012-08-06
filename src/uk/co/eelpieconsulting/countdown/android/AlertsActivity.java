package uk.co.eelpieconsulting.countdown.android;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.daos.SeenMessagesDAO;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.MessageService;
import uk.co.eelpieconsulting.countdown.android.services.caching.MessageCache;
import uk.co.eelpieconsulting.countdown.android.views.MessageDescriptionService;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlertsActivity extends Activity {
	
	private static final String TAG = "AlertsActivity";
	
	public static final int NOTIFICATION_ID = 1;
	
	private FavouriteStopsDAO favouriteStopsDAO;
	private MessageService messageService;
	
	private TextView status;	
	private FetchMessagesTask fetchMessageTask;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);
        
        favouriteStopsDAO = FavouriteStopsDAO.get(getApplicationContext());
		messageService = new MessageService(ApiFactory.getApi(getApplicationContext()), new MessageCache(getApplicationContext()), new SeenMessagesDAO(getApplicationContext()));

        status = (TextView) findViewById(R.id.status);
        status.setVisibility(View.GONE);        
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setTitle(getString(R.string.alerts));
		
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancel(NOTIFICATION_ID);
		
		showAlerts();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (fetchMessageTask != null && fetchMessageTask.getStatus().equals(Status.RUNNING)) {
			fetchMessageTask.cancel(true);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, R.string.favourites);
		menu.add(0, 4, 0, R.string.near_me);		
		menu.add(0, 7, 0, R.string.search);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			this.startActivity(new Intent(this, FavouritesActivity.class));
			return true;
			
		case 4:
			this.startActivity(new Intent(this, NearbyTabActivity.class));
			return true;
			
		case 7:
			this.startActivity(new Intent(this, SearchActivity.class));
			return true;
		}
		return false;
	}
	
	private void showAlerts() {
		final Set<Stop> favouriteStops = favouriteStopsDAO.getFavouriteStops();
		if (favouriteStops.isEmpty()) {
			status.setText(R.string.no_favourites_warning);
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		status.setText(R.string.loading_messages);
		status.setVisibility(View.VISIBLE);
		FetchMessagesTask fetchMessagesTask = new FetchMessagesTask(messageService);
		fetchMessagesTask.execute(favouriteStops);
	}
	
	private void renderMessages(List<MultiStopMessage> messages) {		
		if (messages == null) {
			status.setText(R.string.could_not_load_messages);
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		if (messages.isEmpty()) {
			status.setText(R.string.no_alerts);
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		status.setText(R.string.alerts_effecting_your_stops);
		status.setVisibility(View.VISIBLE);
		
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();		
		for (MultiStopMessage messageToDisplay : messages) {			
			final TextView messageView = MessageDescriptionService.makeMessageView(messageToDisplay, getApplicationContext());			
			stopsList.addView(messageView);
		}
		
		final TextView credit = new TextView(getApplicationContext());
		credit.setText(getString(R.string.tfl_credit));
		stopsList.addView(credit);		
	}
	
	private class FetchMessagesTask extends AsyncTask<Set<Stop>, Integer, List<MultiStopMessage>> {

		private final MessageService messageService;

		public FetchMessagesTask(MessageService messageService) {
			super();
			this.messageService = messageService;
		}

		@Override
		protected List<MultiStopMessage> doInBackground(Set<Stop>... params) {
			fetchMessageTask = this;		
			final Set<Stop> stops = params[0];
			int[] stopIds = new int[stops.size()];
			Iterator<Stop> iterator = stops.iterator();
			for (int i = 0; i < stops.size(); i++) {
				stopIds[i] = iterator.next().getId();				
			}
			
			try {
				return messageService.getStopMessages(stopIds);
			} catch (ContentNotAvailableException e) {
				Log.w(TAG, "Messages could not be fetched: Cause: " + e.getMessage());
				return null;
			}
		}

		@Override
		protected void onPostExecute(List<MultiStopMessage> messages) {
			renderMessages(messages);
		}	
		
	}
		
}