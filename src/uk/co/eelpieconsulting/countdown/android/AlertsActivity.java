package uk.co.eelpieconsulting.countdown.android;

import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.Message;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.views.StopClicker;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import uk.co.eelpieconsulting.countdown.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.countdown.exceptions.ParsingException;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlertsActivity extends Activity {

	private FavouriteStopsDAO favouriteStopsDAO;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);        
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());
        
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
	
	private void renderMessages(List<Message> messages, TextView messageText) {		
		if (messages == null) {
			return;
		}
		StringBuilder output = new StringBuilder();
		for (Message message : messages) {
			final boolean isCurrent = message.getStartDate() < (System.currentTimeMillis()) && message.getEndDate() > (System.currentTimeMillis());
			if (isCurrent) {			
				output.append(message.getMessage() + "\n");
			}
		}
		messageText.setText(output);
		messageText.setVisibility(View.VISIBLE);		
	}
	
	private class FetchMessagesTask extends AsyncTask<Integer, Integer, List<Message>> {

		private uk.co.eelpieconsulting.countdown.api.CountdownApi api;
		private final TextView target;

		public FetchMessagesTask(uk.co.eelpieconsulting.countdown.api.CountdownApi api, TextView target) {
			super();
			this.api = api;
			this.target = target;
		}

		@Override
		protected List<Message> doInBackground(Integer... params) {
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
			renderMessages(messages, target);
		}	
		
	}
	
	private void showFavourites() {
		showStops(favouriteStopsDAO.getFavouriteStops());
	}

	private void showStops(Set<Stop> stops) {
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		for (Stop stop : stops) {
			stopsList.addView(makeStopView(stop));
			TextView messageView = new TextView(getApplicationContext());
			stopsList.addView(messageView);
			
			FetchMessagesTask fetchMessagesTask = new FetchMessagesTask(new uk.co.eelpieconsulting.countdown.api.CountdownApi("http://countdown.api.tfl.gov.uk"), messageView);
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
	
}