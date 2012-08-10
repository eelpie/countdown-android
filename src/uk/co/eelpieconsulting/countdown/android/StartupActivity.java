package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StartupActivity extends Activity {
		
	private static final String TAG = "StartupActivity";
	
	private FavouriteStopsDAO favouriteStopsDAO;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());        
        if (favouriteStopsDAO.hasFavourites()) {
        	Log.d(TAG, "Favourites set; starting stop activity");
			this.startActivity(new Intent(this, StopActivity.class));
			
        } else {
        	Log.d(TAG, "No favourite set; starting favourites activity so that user can set some");
        	this.startActivity(new Intent(this, FavouritesActivity.class));
        }
    }
	
}
