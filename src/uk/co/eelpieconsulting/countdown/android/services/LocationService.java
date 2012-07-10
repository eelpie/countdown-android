package uk.co.eelpieconsulting.countdown.android.services;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class LocationService {
	
	private static final String TAG = "LocationService";
	
	private static String[] providers = {LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};
	
	public static Location getBestLastKnownLocation(LocationManager locationManager) {
		List<Location> allAvailableLastKnownLocations = getAllAvailableLastKnownLocations(locationManager);		
		return chooseBestLocation(allAvailableLastKnownLocations);
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
				Log.i(TAG, "Last known location for provider " + provider + ":" + DistanceMeasuringService.makeLocationDescription(providerLocation));
				availableLocations.add(providerLocation);
			}
		}
		return availableLocations;
	}

}
