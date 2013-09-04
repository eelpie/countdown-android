package uk.co.eelpieconsulting.countdown.android;

import java.util.Date;
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
import uk.co.eelpieconsulting.countdown.android.views.MessagesListAdapter;
import android.app.Activity;
import android.app.NotificationManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class AlertsActivity extends Activity {
	
	private static final String TAG = "AlertsActivity";
	
	public static final int NOTIFICATION_ID = 1;
	
	private FavouriteStopsDAO favouriteStopsDAO;
	private MessageService messageService;
	
	private TextView status;
	private ListView messagesList;
	private FetchMessagesTask fetchMessageTask;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stopslist);
        
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    setTitle(R.string.alerts);
	    
        favouriteStopsDAO = FavouriteStopsDAO.get(getApplicationContext());
		messageService = new MessageService(ApiFactory.getApi(getApplicationContext()), new MessageCache(getApplicationContext()), new SeenMessagesDAO(getApplicationContext()), getApplicationContext());

		messagesList = (ListView) findViewById(R.id.list);
        status = (TextView) findViewById(R.id.status);
        status.setVisibility(View.GONE);        
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		
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
			status.announceForAccessibility(getString(R.string.could_not_load_messages));			
			return;
		}
		
		if (messages.isEmpty()) {
			status.setText(R.string.no_alerts);
			status.setVisibility(View.VISIBLE);			
			status.announceForAccessibility(getString(R.string.no_alerts));
			return;
		}
		
		final String alertsEffectingYourStopsMessage = messages.size() == 1 ? "There is 1 " + getString(R.string.alerts_effecting_your_stops) : 
			"There are " + messages.size() + " " + getString(R.string.alerts_effecting_your_stops);
				
		status.setText(alertsEffectingYourStopsMessage);
		status.setVisibility(View.VISIBLE);
		status.announceForAccessibility(alertsEffectingYourStopsMessage);
		
		final MessagesListAdapter messagesListAdapter = new MessagesListAdapter(getApplicationContext(), R.layout.stoprow);
		for (MultiStopMessage message : messages) {
			messagesListAdapter.add(message);
		}
		
		messagesList.setAdapter(messagesListAdapter);	
		messagesList.setVisibility(View.VISIBLE);
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
				final long start = new Date().getTime();
				
				final List<MultiStopMessage> stopMessages = messageService.getStopMessages(stopIds);
				
				while (new Date().getTime() < (start + 1000)) {
					try {
						Thread.sleep(100);	// TODO pause to make accessibility annoucements play nice
					} catch (InterruptedException e) {					
					}					
				}
				
				return stopMessages;
				
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