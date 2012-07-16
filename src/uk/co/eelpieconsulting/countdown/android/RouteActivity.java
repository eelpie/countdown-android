package uk.co.eelpieconsulting.countdown.android;

import java.io.InputStream;
import java.util.List;

import uk.co.eelpieconsulting.countdown.android.api.RoutesService;
import uk.co.eelpieconsulting.countdown.android.model.Route;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import uk.co.eelpieconsulting.countdown.model.Stop;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RouteActivity extends Activity {
	
	private static final String TAG = "RouteActivity";
		
	private String selectedRoute;

	private TextView status;

	private RoutesService routesService;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);
        status = (TextView) findViewById(R.id.status);
        
        routesService = new RoutesService();
        
        selectedRoute = null;
    }
	
	@Override
	protected void onResume() {
		super.onResume();
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("route") != null) {
        	selectedRoute = (String) this.getIntent().getExtras().get("route");
        }
        
        Log.i(TAG, "Selected route: " + selectedRoute);
        
        final String title = selectedRoute;
		getWindow().setTitle(title);
		status.setVisibility(View.GONE);
		
		final InputStream routes = getResources().openRawResource(R.raw.routes);
		final Route route = routesService.getRoute(routes, selectedRoute);		
		showStops(route.getStops());		
	}
	
	private void showStops(List<Stop> stops) {
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		for (Stop stop : stops) {
			stopsList.addView(makeStopView(stop));
		}
	}

	private TextView makeStopView(Stop stop) {
		final TextView stopTextView = new TextView(this.getApplicationContext());
		stopTextView.setText(StopDescriptionService.makeStopDescription(stop) + "\n\n");
		stopTextView.setOnClickListener(new StopClicker(stop));
		return stopTextView;
	}
	
	private class StopClicker implements OnClickListener {
		private Stop stop;

		public StopClicker(Stop stop) {
			this.stop = stop;
		}

		public void onClick(View view) {
			Intent intent = getIntentForContentsType(view.getContext(), stop);
			intent.putExtra("stop", stop);
			startActivity(intent);
		}

		private Intent getIntentForContentsType(Context context, Stop stop) {
			return new Intent(context, CountdownActivity.class);
		}
	}
}
