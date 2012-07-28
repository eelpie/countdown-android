package uk.co.eelpieconsulting.countdown.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.buses.client.CountdownApi;
import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.model.MultiStopMessage;
import uk.co.eelpieconsulting.countdown.android.services.MessageService;
import uk.co.eelpieconsulting.countdown.android.views.MessageDescriptionService;
import android.app.Activity;
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
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);        
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());
        
        fetchMessagesTasks = new ArrayList<FetchMessagesTask>();
        
        TextView status = (TextView) findViewById(R.id.status);
        status.setVisibility(View.GONE);        
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		getWindow().setTitle(getString(R.string.alerts));
		
		showFavourites();
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
	}
	
	private void showFavourites() {
		showStops(favouriteStopsDAO.getFavouriteStops());
	}

	private void showStops(Set<Stop> stops) {
		FetchMessagesTask fetchMessagesTask = new FetchMessagesTask(ApiFactory.getApi());
		fetchMessagesTask.execute(stops);
	}
	
	private class FetchMessagesTask extends AsyncTask<Set<Stop>, Integer, List<MultiStopMessage>> {

		private CountdownApi api;

		public FetchMessagesTask(CountdownApi api) {
			super();
			this.api = api;
		}

		@Override
		protected List<MultiStopMessage> doInBackground(Set<Stop>... params) {
			fetchMessagesTasks.add(this);			
			final Set<Stop> stops = params[0];
			try {							
				MessageService messageService = new MessageService(api);
				return messageService.getMessages(stops);
				
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