package uk.co.eelpieconsulting.countdown.android;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.buses.client.BusesClient;
import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.views.MessageDescriptionService;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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

	private FavouriteStopsDAO favouriteStopsDAO;
	private List<FetchMessagesTask> fetchMessagesTasks;
	private TextView status;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);        
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());
        
        fetchMessagesTasks = new ArrayList<FetchMessagesTask>();
        
        status = (TextView) findViewById(R.id.status);
        status.setVisibility(View.GONE);        
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setTitle(getString(R.string.alerts));		
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
			this.startActivity(new Intent(this, NearbyActivity.class));
			return true;
		}
		return false;
	}
	
	private void renderMessages(List<MultiStopMessage> messages) {		
		if (messages == null) {
			return;
		}
		
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();		
		for (MultiStopMessage messageToDisplay : messages) {			
			final TextView messageView = MessageDescriptionService.makeMessageView(messageToDisplay, getApplicationContext());			
			stopsList.addView(messageView);
		}
		
		sendNotification(getApplicationContext(), messages);
	}
	
	private void sendNotification(Context context, List<MultiStopMessage> messages) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.notification_icon;
		final CharSequence tickerText = "New alerts";
		Notification notification = new Notification(icon, tickerText, new Date().getTime());

		CharSequence contentTitle = messages.size() + " new alerts";
		CharSequence contentText =  messages.get(0).getMessage();

		Intent notificationIntent = new Intent(context, AlertsActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);		
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificationManager.notify(0, notification);
	}
	
	private void showAlerts() {
		final Set<Stop> favouriteStops = favouriteStopsDAO.getFavouriteStops();
		if (favouriteStops.isEmpty()) {
			status.setText(R.string.no_favourites_warning);
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		FetchMessagesTask fetchMessagesTask = new FetchMessagesTask(ApiFactory.getApi());
		fetchMessagesTask.execute(favouriteStops);
	}

	private class FetchMessagesTask extends AsyncTask<Set<Stop>, Integer, List<MultiStopMessage>> {

		private BusesClient api;

		public FetchMessagesTask(BusesClient api) {
			super();
			this.api = api;
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
			
			try {
				return api.getMultipleStopMessages(stopIds);
				
			} catch (HttpFetchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
			return null;
		}

		@Override
		protected void onPostExecute(List<MultiStopMessage> messages) {
			renderMessages(messages);
			fetchMessagesTasks.remove(this);
		}	
		
	}
		
}