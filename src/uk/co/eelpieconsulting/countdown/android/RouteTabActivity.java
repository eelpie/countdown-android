package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.busroutes.model.Route;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class RouteTabActivity extends TabActivity {
	
	private Route selectedRoute;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.near);
        
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("route") != null) {
        	selectedRoute = (Route) this.getIntent().getExtras().get("route");
        }

        final String title = selectedRoute.getRoute() + " towards " + selectedRoute.getTowards();
        setTitle(title);

        setupTabs();
    }
	
	private void setupTabs() {
		final TabHost tabHost = getTabHost();
 
		final TabSpec mapSpec = tabHost.newTabSpec("Map");
		mapSpec.setIndicator("Map");
		Intent mapIntent = new Intent(this, RouteMapActivity.class);
		mapIntent.putExtra("route", selectedRoute);
		mapSpec.setContent(mapIntent);
		
        final TabSpec stopsSpec = tabHost.newTabSpec("Stops");
        stopsSpec.setIndicator("Stops");
        Intent stopsIntent = new Intent(this, RouteStopsActivity.class);
        stopsIntent.putExtra("route", selectedRoute);
        stopsSpec.setContent(stopsIntent);
        
        
        tabHost.addTab(mapSpec);
        tabHost.addTab(stopsSpec);
	}
	
}
