package uk.co.eelpieconsulting.countdown.android.services.location;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import android.location.Location;

public class DistanceMeasuringService {
	
	private final static DecimalFormat LAT_LONG_FORMAT = new DecimalFormat("0.0000");

	private final static BearingLabelService bearingLabelService = new BearingLabelService();
	
	public static String distanceToStopDescription(Location location, Stop stop) {
		final String distanceTo = roundDistanceBasedOnLocationAccuracy(location, distanceTo(location, stop));
		final float bearing = DistanceMeasuringService.bearingTo(location, KnownStopLocationProviderService.makeLocationForSelectedStop(stop));
		return distanceTo + " metres " + bearingLabelService.bearingLabelFor(bearing).toLowerCase(Locale.ENGLISH);
	}
	
	public static Stop findClosestOf(List<Stop> stops, Location location) {
		Float closestDistance = null;
		Stop closestStop = null;
		for (Stop stop : stops) {
			if (location != null) {
				float distanceTo = DistanceMeasuringService.distanceTo(location, stop);
				if (closestDistance == null || closestDistance > distanceTo) {
					closestStop = stop;
					closestDistance = distanceTo;
				}
			}
		}
		return closestStop;
	}
	
	public static float distanceTo(Location location, Stop stop) {	// TODO stops probably shouldn't be mentioned in this class.
		return distanceBetween(location.getLatitude(), location.getLongitude(), stop.getLatitude(), stop.getLongitude());		
	}
	
	public static float distanceBetween(Location start, Location end) {
		return distanceBetween(start.getLatitude(), start.getLongitude(), end.getLatitude(), end.getLongitude());	
	}
	
	public static float bearingTo(Location location, Location destination) {
		float degreesEastOfNorth = location.bearingTo(destination);
		if (degreesEastOfNorth < 0) {
			return degreesEastOfNorth + 360;
		}
		return degreesEastOfNorth;				
	}
 	
	public static String makeLocationDescription(Location location) {	// TODO move to view factory
		final String description = roundLatLong(location.getLatitude()) + ", " + roundLatLong(location.getLongitude());
		return makeLocationDescription(description, location);		
	}
	
	public static String makeLocationDescription(String locationName, Location location) {
		StringBuilder description = new StringBuilder(locationName);
		if (location.hasAccuracy()) {
			description.append(" +/- " + roundDistanceBasedOnLocationAccuracy(location, location.getAccuracy()) + "m");
		}
		return description.toString();
	}
	
	public static String roundLatLong(double value) {
		return LAT_LONG_FORMAT.format(value);
	}
	
	private static String roundDistanceBasedOnLocationAccuracy(
			Location location, final float distanceTo) {
		if (location.hasAccuracy()) {
			float accuracy = location.getAccuracy();
			if (accuracy <= 20) {
				return Integer.toString(roundToPlusMinus1(distanceTo));
			}
			if (accuracy <= 200) {
				return Integer.toString(roundToPlusMinus10(distanceTo));
			}			
			if (accuracy > 200) {
				return Integer.toString(roundToPlusMinus100(distanceTo));
			}
		}	
		return Integer.toString(roundToPlusMinus10(distanceTo));
	}

	private static int roundToPlusMinus1(final float distanceTo) {
		return Math.round(distanceTo);
	}

	private static int roundToPlusMinus10(final float distanceTo) {
		return Math.round((distanceTo / 10)) * 10;
	}

	private static int roundToPlusMinus100(final float distanceTo) {
		return Math.round((distanceTo / 100)) * 100;
	}

	private static float distanceBetween(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
		float[] results = new float[1];
		Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
		return results[0];
	}
	
}
