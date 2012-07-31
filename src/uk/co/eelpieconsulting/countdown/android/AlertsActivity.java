package uk.co.eelpieconsulting.countdown.android;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.daos.SeenMessagesDAO;
import uk.co.eelpieconsulting.countdown.android.services.MessageService;
import uk.co.eelpieconsulting.countdown.android.views.MessageDescriptionService;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlertsActivity extends Activity {

	public static final int NOTIFICATION_ID = 1;
	
	private FavouriteStopsDAO favouriteStopsDAO;
	private List<FetchMessagesTask> fetchMessagesTasks;
	private TextView status;

	private MessageService messageService;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);
        
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());
		messageService = new MessageService(ApiFactory.getApi(), new SeenMessagesDAO(getApplicationContext()));

        fetchMessagesTasks = new ArrayList<FetchMessagesTask>();
        
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
		if (!fetchMessagesTasks.isEmpty()) {
			for (FetchMessagesTask fetchMessagesTask : fetchMessagesTasks) {
				if (fetchMessagesTask != null && fetchMessagesTask.getStatus().equals(Status.RUNNING)) {
					fetchMessagesTask.cancel(true);
				}				
			}
		}
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
	
	private void showAlerts() {
		final Set<Stop> favouriteStops = favouriteStopsDAO.getFavouriteStops();
		if (favouriteStops.isEmpty()) {
			status.setText(R.string.no_favourites_warning);
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		FetchMessagesTask fetchMessagesTask = new FetchMessagesTask(messageService);
		fetchMessagesTask.execute(favouriteStops);
	}
	
	private void renderMessages(List<MultiStopMessage> messages) {		
		if (messages == null) {
			return;
		}
		
		if (messages.isEmpty()) {
			status.setText(R.string.no_alerts);
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();		
		for (MultiStopMessage messageToDisplay : messages) {			
			final TextView messageView = MessageDescriptionService.makeMessageView(messageToDisplay, getApplicationContext());			
			stopsList.addView(messageView);
		}
		
		messageService.markAsSeen(messages);		
	}
	

	private class FetchMessagesTask extends AsyncTask<Set<Stop>, Integer, List<MultiStopMessage>> {

		private MessageService messageService;

		public FetchMessagesTask(MessageService messageService) {
			super();
			this.messageService = messageService;
		}

		@Override
		protected List<MultiStopMessage> doInBackground(Set<Stop>... params) {
			fetchMessagesTasks.add(this);			
			final Set<Stop> stops = params[0];
			int[] stopIds = new int[stops.size()];
			Iterator<Stop> iterator = stops.iterator();
			for (int i = 0; i < stops.size(); i++) {
				stopIds[i] = iterator.next().getId();				
			}
			
			return messageService.getStopMessages(stopIds);			
		}

		@Override
		protected void onPostExecute(List<MultiStopMessage> messages) {
			renderMessages(messages);
			fetchMessagesTasks.remove(this);
		}	
		
	}
		
}