package uk.co.eelpieconsulting.countdown.android;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.Message;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.views.StopClicker;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import uk.co.eelpieconsulting.countdown.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.countdown.exceptions.ParsingException;
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
	
	private void renderMessages(List<Message> messages, TextView stopTextView, TextView messageText) {		
		if (messages == null) {
			return;
		}
		StringBuilder output = new StringBuilder();
		for (Message message : messages) {
			final boolean isCurrent = message.getStartDate() < (System.currentTimeMillis()) && message.getEndDate() > (System.currentTimeMillis());
			if (isCurrent) {
				output.append(message.getMessage() + "\n");
				stopTextView.setVisibility(View.VISIBLE);
			}
		}
		messageText.setText(output);
		messageText.setVisibility(View.VISIBLE);		
	}
	
	private void showFavourites() {
		showStops(favouriteStopsDAO.getFavouriteStops());
	}

	private void showStops(Set<Stop> stops) {
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		final uk.co.eelpieconsulting.countdown.api.CountdownApi api = ApiFactory.getCountdownApi();		
		for (Stop stop : stops) {
			final TextView stopView = makeStopView(stop);
			stopView.setVisibility(View.GONE);
			
			stopsList.addView(stopView);
			TextView messageView = new TextView(getApplicationContext());
			stopsList.addView(messageView);
			
			FetchMessagesTask fetchMessagesTask = new FetchMessagesTask(api, stopView, messageView);
			fetchMessagesTask.execute(stop.getId());
			// TODO close tasks on pause
		}
	}

	private TextView makeStopView(Stop stop) {
		final TextView stopTextView = new TextView(this.getApplicationContext());
		stopTextView.setText(StopDescriptionService.makeStopDescription(stop) + "\n\n");
		stopTextView.setOnClickListener(new StopClicker(this, stop));
		return stopTextView;
	}
	
	private class FetchMessagesTask extends AsyncTask<Integer, Integer, List<Message>> {

		private uk.co.eelpieconsulting.countdown.api.CountdownApi api;
		private final TextView stopView;
		private final TextView messageView;

		public FetchMessagesTask(uk.co.eelpieconsulting.countdown.api.CountdownApi api, TextView stopView, TextView messageView) {
			super();
			this.api = api;
			this.stopView = stopView;
			this.messageView = messageView;
		}

		@Override
		protected List<Message> doInBackground(Integer... params) {
			fetchMessagesTasks.add(this);
			final int stopId = params[0];
			try {				
				return api.getStopMessages(stopId);
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
		protected void onPostExecute(List<Message> messages) {
			renderMessages(messages, stopView, messageView);
			fetchMessagesTasks.remove(this);
		}	
		
	}
	
	
}