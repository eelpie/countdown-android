package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.countdown.android.activities.maps.NearbyMapActivity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class NearbyTabActivity extends TabActivity {	// TODO Tab in name is confusing - is the a tab or the frame?
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.near);
        
	    getActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(getString(R.string.near_me));
        setupTabs();
	}
	
	private void setupTabs() {
		final TabHost tabHost = getTabHost();
 
        final TabSpec stopsTab = tabHost.newTabSpec("Stops");
        stopsTab.setIndicator("Stops");
        Intent stopsIntent = new Intent(this, NearbyStopsListActivity.class);
        stopsTab.setContent(stopsIntent);
 
        final TabSpec routesTab = tabHost.newTabSpec("Routes");
        routesTab.setIndicator("Routes");
        Intent routesIntent = new Intent(this, NearbyRoutesListActivity.class);
        routesTab.setContent(routesIntent);
        
        final TabSpec mapTab = tabHost.newTabSpec("Map");
        mapTab.setIndicator("Map");
        Intent mapIntent = new Intent(this, NearbyMapActivity.class);
        mapTab.setContent(mapIntent);        
 
        tabHost.addTab(stopsTab);
        tabHost.addTab(routesTab);
        tabHost.addTab(mapTab);
	}
	
}
