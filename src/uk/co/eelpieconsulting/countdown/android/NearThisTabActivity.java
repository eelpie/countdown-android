package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class NearThisTabActivity extends TabActivity {
	
	private Stop selectedStop;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.near);
        
        if (this.getIntent().getExtras() != null && this.getIntent().getExtras().get("stop") != null) {
			selectedStop = (Stop) this.getIntent().getExtras().get("stop");
        }
        
        setTitle("Near " + StopDescriptionService.makeStopDescription(selectedStop));
        setupTabs();
    }
	
	private void setupTabs() {
		final TabHost tabHost = getTabHost();
 
        TabSpec stopsSpec = tabHost.newTabSpec("Stops");
        stopsSpec.setIndicator("Stops");
        Intent stopsIntent = new Intent(this, NearbyStopsListActivity.class);
        stopsIntent.putExtra("stop", selectedStop);
        stopsSpec.setContent(stopsIntent);
 
        TabSpec routesSpec = tabHost.newTabSpec("Routes");
        routesSpec.setIndicator("Routes");
        Intent routesIntent = new Intent(this, NearbyRoutesListActivity.class);
        routesIntent.putExtra("stop", selectedStop);
        routesSpec.setContent(routesIntent);
        
        TabSpec tabSpec = tabHost.newTabSpec("Map");
        tabSpec.setIndicator("Map");
        Intent mapIntent = new Intent(this, NearbyMapActivity.class);
        mapIntent.putExtra("stop", selectedStop);
        tabSpec.setContent(mapIntent);        
 
        tabHost.addTab(stopsSpec);
        tabHost.addTab(routesSpec);
        tabHost.addTab(tabSpec);
	}
	
}
