package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.location.LocationService;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class StartupActivity extends Activity {
		
	private static final String TAG = "StartupActivity";
	
	private FavouriteStopsDAO favouriteStopsDAO;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Stop selectedStop = null;
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());        
        if (favouriteStopsDAO.hasFavourites()) {
        	Log.d(TAG, "Favourites set; starting stop activity");
        	
    		final Location lastKnownLocation = LocationService.getBestLastKnownLocation(getApplicationContext());
    		if (lastKnownLocation != null) {
    			final String lastKnownLocationMessage = "Last known location is: " + DistanceMeasuringService.makeLocationDescription(lastKnownLocation);
    			Log.i(TAG, lastKnownLocationMessage);
    			selectedStop = favouriteStopsDAO.getClosestFavouriteStopTo(lastKnownLocation);
    			Log.i(TAG, "Choosing closest favourite stop based on last known location: " + selectedStop.getName());
    		}
        }
        
        if (selectedStop != null) {
			final Intent intent = new Intent(this, StopActivity.class);
			intent.putExtra("stop", selectedStop);
			this.startActivity(intent);
			
        } else {
        	Log.d(TAG, "No favourite set; starting favourites activity so that user can set some");
        	this.startActivity(new Intent(this, FavouritesActivity.class));
        }
    }
	
}
