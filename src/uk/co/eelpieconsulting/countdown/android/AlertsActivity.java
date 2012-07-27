package uk.co.eelpieconsulting.countdown.android;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.eelpieconsulting.buses.client.CountdownApi;
import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.Message;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.views.StopClicker;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
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
	
	private void renderMessages(Map<Stop, List<Message>> messages) {		
		if (messages == null) {
			return;
		}	
		
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		
		Set<Stop> stops = messages.keySet();
		for (Stop stop : stops) {
			if (!messages.get(stop).isEmpty()) {
				stopsList.addView(makeStopView(stop));			
				for (Message message : messages.get(stop)) {
					final boolean isCurrent = message.getStartDate() < (System.currentTimeMillis()) && message.getEndDate() > (System.currentTimeMillis());
					if (isCurrent) {
						TextView messageView = new TextView(getApplicationContext());
						messageView.setText(message.getMessage());
						messageView.setVisibility(View.VISIBLE);
						stopsList.addView(messageView);
					}
				}
			}
		}				
	}
	
	private void showFavourites() {
		showStops(favouriteStopsDAO.getFavouriteStops());
	}

	private void showStops(Set<Stop> stops) {
		FetchMessagesTask fetchMessagesTask = new FetchMessagesTask(ApiFactory.getApi());
		fetchMessagesTask.execute(stops);
	}

	private TextView makeStopView(Stop stop) {
		final TextView stopTextView = new TextView(this.getApplicationContext());
		stopTextView.setText(StopDescriptionService.makeStopDescription(stop) + "\n\n");
		stopTextView.setOnClickListener(new StopClicker(this, stop));
		return stopTextView;
	}
	
	private class FetchMessagesTask extends AsyncTask<Set<Stop>, Integer, Map<Stop, List<Message>>> {

		private CountdownApi api;

		public FetchMessagesTask(CountdownApi api) {
			super();
			this.api = api;
		}

		@Override
		protected Map<Stop, List<Message>> doInBackground(Set<Stop>... params) {
			fetchMessagesTasks.add(this);
			
			Map<Stop, List<Message>> result = new HashMap<Stop, List<Message>>();
			final Set<Stop> stops = params[0];
			try {				
				int[] stopIds = new int[stops.size()];
				Iterator<Stop> iterator = stops.iterator();
				for (int i = 0; i < stops.size(); i++) {
					stopIds[i] = iterator.next().getId();
				}				
				
				List<Message> messages = api.getMultipleStopMessages(stopIds);
				Map<Integer, Stop> stopsById = new HashMap<Integer, Stop>();
				for (Stop stop : stops) {
					stopsById.put(stop.getId(), stop);
					result.put(stop, new ArrayList<Message>());
				}
				
				for (Message message : messages) {
					final Stop messageStop = stopsById.get(message.getStopId());
					List<Message> list = result.get(messageStop);
					list.add(message);
					result.put(messageStop, list);
				}
				
				
			} catch (HttpFetchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(Map<Stop, List<Message>> messages) {
			renderMessages(messages);
			fetchMessagesTasks.remove(this);
		}	
		
	}
		
}