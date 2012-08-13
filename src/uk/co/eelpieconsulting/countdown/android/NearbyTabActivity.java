package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.countdown.android.activities.maps.NearbyMapActivity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.nearby_menu, menu);
		return true;
	}
	
	private void setupTabs() {
		final TabHost tabHost = getTabHost();
 
        TabSpec stopsSpec = tabHost.newTabSpec("Stops");
        stopsSpec.setIndicator("Stops");
        Intent stopsIntent = new Intent(this, NearbyStopsListActivity.class);
        stopsSpec.setContent(stopsIntent);
 
        TabSpec routesSpec = tabHost.newTabSpec("Routes");
        routesSpec.setIndicator("Routes");
        Intent routesIntent = new Intent(this, NearbyRoutesListActivity.class);
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
