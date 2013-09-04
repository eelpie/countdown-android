package uk.co.eelpieconsulting.countdown.android.services;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.location.LocationService;
import android.content.Context;
import android.location.Location;
import android.util.Log;

public class NearestFavouriteStopService {
	
	private static final String TAG = "NearestFavouriteStopService";
	
	private FavouriteStopsDAO favouriteStopsDAO;
	
	public Stop selectNearestFavouriteStopBasedOnLastKnownLocation(Context context) {
		favouriteStopsDAO = FavouriteStopsDAO.get(context);        
        if (favouriteStopsDAO.hasFavourites()) {
        	Log.d(TAG, "Favourites set; starting stop activity");
        	
    		final Location lastKnownLocation = LocationService.getBestLastKnownLocation(context);
    		if (lastKnownLocation != null) {
    			final String lastKnownLocationMessage = "Last known location is: " + DistanceMeasuringService.makeLocationDescription(lastKnownLocation);
    			Log.i(TAG, lastKnownLocationMessage);
    			Stop selectedStop = favouriteStopsDAO.getClosestFavouriteStopTo(lastKnownLocation);
    			Log.i(TAG, "Choosing closest favourite stop based on last known location: " + selectedStop.getName());
    			return selectedStop;
    		}
        }
		return null;
	}

}
