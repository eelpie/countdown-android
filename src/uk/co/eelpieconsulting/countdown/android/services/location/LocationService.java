package uk.co.eelpieconsulting.countdown.android.services.location;

import java.util.ArrayList;
import java.util.List;

import uk.co.eelpieconsulting.countdown.android.NoProvidersException;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

public class LocationService {	// TODO Why are there 2 classes with this name?
	
	private static final String TAG = "LocationService";

	private static final int FIVE_SECONDS = 5 * 1000;
	
	private static final String[] PROVIDERS = {LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};
	
	public static Location getBestLastKnownLocation(LocationManager locationManager) {
		List<Location> allAvailableLastKnownLocations = getAllAvailableLastKnownLocations(locationManager);		
		return chooseBestLocation(allAvailableLastKnownLocations);
	}
	
	public static void registerForLocationUpdates(Context context, LocationListener listener) throws NoProvidersException {
		final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);			
		try {
			boolean provideEnabled = false;
			
			String[] providers = new String[]{LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};
			for (String provider : providers) {
				if (locationManager.isProviderEnabled(provider)) {
					locationManager.requestLocationUpdates(provider, FIVE_SECONDS, 0, listener);
					provideEnabled = true;
				}				
			}
			if (provideEnabled) {
				return;
			}
			
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		
		locationManager.removeUpdates(listener);
		throw new NoProvidersException();
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
		for (String provider : PROVIDERS) {
			Location providerLocation = locationManager.getLastKnownLocation(provider);
			if (providerLocation != null) {
				Log.i(TAG, "Last known location for provider " + provider + ": " + DistanceMeasuringService.makeLocationDescription(providerLocation));
				availableLocations.add(providerLocation);
			}
		}
		return availableLocations;
	}

}
