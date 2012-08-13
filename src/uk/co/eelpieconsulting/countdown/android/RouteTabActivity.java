package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.activities.maps.RouteMapActivity;
import android.app.TabActivity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class RouteTabActivity extends TabActivity {
	
	private Route selectedRoute;
	private Stop selectedStop;
	private Location location;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.near);
        
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("route") != null) {
        	selectedRoute = (Route) this.getIntent().getExtras().get("route");
        }
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
        	selectedStop = (Stop) this.getIntent().getExtras().get("stop");
        }
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("location") != null) {
        	location = (Location) this.getIntent().getExtras().get("location");
        }

        final String title = selectedRoute.getRoute() + " towards " + selectedRoute.getTowards();
        setTitle(title);

        setupTabs();
    }
	
	private void setupTabs() {
		final TabHost tabHost = getTabHost();
 
		final TabSpec stopsSpec = tabHost.newTabSpec("Stops");
		stopsSpec.setIndicator("Stops");
		Intent stopsIntent = new Intent(this, RouteStopsActivity.class);
		stopsIntent.putExtra("route", selectedRoute);
		stopsIntent.putExtra("stop", selectedStop);
		stopsIntent.putExtra("location", location);
		stopsSpec.setContent(stopsIntent);
		
		final TabSpec mapSpec = tabHost.newTabSpec("Map");
		mapSpec.setIndicator("Map");
		Intent mapIntent = new Intent(this, RouteMapActivity.class);
		mapIntent.putExtra("route", selectedRoute);
		stopsIntent.putExtra("stop", selectedStop);
		mapSpec.setContent(mapIntent);
		        
        tabHost.addTab(stopsSpec);
        tabHost.addTab(mapSpec);
	}
	
}
