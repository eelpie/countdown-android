package uk.co.eelpieconsulting.countdown.android;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class NearbyTabActivity extends TabActivity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.near);
        
        setTitle(getString(R.string.near_me));
        setupTabs();
    }

	private void setupTabs() {
		final TabHost tabHost = getTabHost();
 
        TabSpec stopsSpec = tabHost.newTabSpec("Stops");
        stopsSpec.setIndicator("Stops");
        Intent stopsIntent = new Intent(this, NearbyStopsListActivity.class);
        stopsSpec.setContent(stopsIntent);
 
        TabSpec routesSpec = tabHost.newTabSpec("Routes");
        routesSpec.setIndicator("Routes");
        Intent routesIntent = new Intent(this, NearbyStopsListActivity.class);
        routesSpec.setContent(routesIntent);
        
        TabSpec tabSpec = tabHost.newTabSpec("Map");
        tabSpec.setIndicator("Map");
        Intent mapIntent = new Intent(this, NearbyMapActivity.class);
        tabSpec.setContent(mapIntent);        

 
        tabHost.addTab(stopsSpec);
        tabHost.addTab(routesSpec);
        tabHost.addTab(tabSpec);
	}
	
}
