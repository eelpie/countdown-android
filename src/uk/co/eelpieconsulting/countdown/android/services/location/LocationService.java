package uk.co.eelpieconsulting.countdown.android.services.location;

import java.util.ArrayList;
import java.util.List;

import uk.co.eelpieconsulting.countdown.android.NoProvidersException;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

public class LocationService {
		
	public static final int NEAR_BY_RADIUS = 250;

	private static final String TAG = "LocationService";

	private static final int FIVE_SECONDS = 5 * 1000;
	private static final int TEN_MINUTES = 1000 * 60 * 10;
	
	private static final String[] PROVIDERS = {LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER};
	
	public static Location getBestLastKnownLocation(Context context) {
		final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);			
		return chooseBestLocation(getAllAvailableLastKnownLocations(locationManager));
	}
	
	public static Location getRecentBestLastKnownLocation(Context context) {
		final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		final List<Location> allAvailableLastKnownLocations = getAllAvailableLastKnownLocations(locationManager);
		final List<Location> recentLocations = getRecentLocationsFor(allAvailableLastKnownLocations);
		return chooseBestLocation(recentLocations);
	}
		
	
	private static List<Location> getRecentLocationsFor(List<Location> allAvailableLastKnownLocations) {
		List<Location> recentLocations = new ArrayList<Location>();
		for (Location location : allAvailableLastKnownLocations) {
			boolean isRecentEnough = System.currentTimeMillis() - location.getTime() < TEN_MINUTES;
			Log.d(TAG, "Is recent enough: " + location + " (" + isRecentEnough + ")");
			if (isRecentEnough) {				
				recentLocations.add(location);
			}
		}
		return recentLocations;
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
	
	public static boolean isAccurateEnoughForNearbyRoutes(Location location) {
		return location.hasAccuracy() && location.getAccuracy() < NEAR_BY_RADIUS;
	}
	
	public static boolean locationIsSignificantlyDifferentToCurrentLocationToWarrentReloadingResults(Location currentLocation, Location newLocation) {
		if (currentLocation == null) {
			return true;
		}
		float distanceBetween = DistanceMeasuringService.distanceBetween(currentLocation, newLocation);
		final boolean isSignificant = distanceBetween > (NEAR_BY_RADIUS / 2) && distanceBetween > newLocation.getAccuracy();
		Log.d(TAG, "Distance from last location: " + distanceBetween + " (is significant: " + isSignificant + ")");
		return isSignificant;
	}
	
	public static boolean locationIsSignificatelyDifferentOrBetterToWarrentMovingPoint(Location currentLocation, Location newLocation) {
		if (currentLocation == null) {
			return true;
		}
		if (newLocation.getAccuracy() < NEAR_BY_RADIUS) {
			return true;
		}
		
		if (newLocation.getTime() - currentLocation.getTime() > 60000) {
			return true;
		}
		return false;
	}
	
	private static Location chooseBestLocation(List<Location> availableLocations) {
		Log.i(TAG, "Locations to choose best from: " + availableLocations);
		Location bestLocation = null;
		for (Location location : availableLocations) {
			if (bestLocation == null) {
				bestLocation = location;

			} else {				
				if (bestLocation.getTime() + 60000 < location.getTime()) {
					bestLocation = location;				
				}				
				if (location.getAccuracy() < bestLocation.getAccuracy()) {
					bestLocation = location;
				}
			}			
		}
		
		Log.i(TAG, "Best location of " + availableLocations + " is: " + bestLocation);
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
		
		Log.i(TAG, "Available last known locations: " + availableLocations);
		return availableLocations;
	}
	
}
