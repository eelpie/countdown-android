package uk.co.eelpieconsulting.countdown.android.services.location;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

public class LocationService {
	
	private static final String TAG = "LocationService";
	
	private static String[] providers = {LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};
	
	public static Location getBestLastKnownLocation(LocationManager locationManager) {
		List<Location> allAvailableLastKnownLocations = getAllAvailableLastKnownLocations(locationManager);		
		return chooseBestLocation(allAvailableLastKnownLocations);
	}
	
	public static void registerForLocationUpdates(Context context, LocationListener activity) {
		try {
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5 * 1000, 0, activity);
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5 * 1000, 0, activity);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
	}

	private static Location chooseBestLocation(List<Location> allAvailableLastKnownLocations) {
		Location bestLocation = null;
		for (Location location : allAvailableLastKnownLocations) {
			if (bestLocation == null) {
				bestLocation = location;
			} else {
				if (location.getTime() > bestLocation.getTime()) {
					bestLocation = location;				
				}
			}			
		}
		return bestLocation;
	}

	private static List<Location> getAllAvailableLastKnownLocations(LocationManager locationManager) {
		List<Location> availableLocations = new ArrayList<Location>();
		for (String provider : providers) {
			Location providerLocation = locationManager.getLastKnownLocation(provider);
			if (providerLocation != null) {
				Log.i(TAG, "Last known location for provider " + provider + ": " + DistanceMeasuringService.makeLocationDescription(providerLocation));
				availableLocations.add(providerLocation);
			}
		}
		return availableLocations;
	}

}
