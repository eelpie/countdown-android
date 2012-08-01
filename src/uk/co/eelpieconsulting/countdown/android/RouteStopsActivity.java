package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.MapActivity;

public class RouteStopsActivity extends MapActivity {
	
	private static final String TAG = "RouteActivity";
		
	private Route selectedRoute;

	private TextView status;

	private FetchRouteStopsTask fetchStopsTask;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);
        status = (TextView) findViewById(R.id.status);        
        selectedRoute = null;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("route") != null) {
        	selectedRoute = (Route) this.getIntent().getExtras().get("route");
        }
                
        final String title = selectedRoute.getRoute() + " towards " + selectedRoute.getTowards();
		getWindow().setTitle(title);
		
		status.setText("Loading route stops");
		status.setVisibility(View.VISIBLE);
		
		fetchStopsTask = new FetchRouteStopsTask(ApiFactory.getApi(getApplicationContext()));
		fetchStopsTask.execute(selectedRoute);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (fetchStopsTask != null && fetchStopsTask.getStatus().equals(Status.RUNNING)) {
			fetchStopsTask.cancel(true);
		}
	}
		
	private void showStops(List<Stop> stops) {
		if (stops == null) {
			status.setText("Stops could not be loaded"); // TODO why?
			status.setVisibility(View.VISIBLE);
			return;
		}
		
		Log.i(TAG, "Found " + stops.size() + " stops");
		
		status.setText("Displaying");
		status.setVisibility(View.VISIBLE);
		
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		for (Stop stop : stops) {
			Log.i(TAG, "Found: " + stop.toString());
			stopsList.addView(StopDescriptionService.makeStopView(stop, getApplicationContext(), this));
		}
		status.setVisibility(View.GONE);
	}
	
	private class FetchRouteStopsTask extends AsyncTask<Route, Integer, List<Stop>> {

		private BusesClientService api;

		public FetchRouteStopsTask(BusesClientService api) {
			super();
			this.api = api;
		}
		
		@Override
		protected void onPostExecute(List<Stop> stops) {
			showStops(stops);
		}
		
		@Override
		protected List<Stop> doInBackground(Route... params) {
			final Route route = params[0];
			try {
				return api.getRouteStops(route.getRoute(), route.getRun());
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
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}	
	
}
