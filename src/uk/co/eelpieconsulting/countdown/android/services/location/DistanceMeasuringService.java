package uk.co.eelpieconsulting.countdown.android.services.location;

import java.text.DecimalFormat;
import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import android.location.Location;

public class DistanceMeasuringService {
	
	private static DecimalFormat LAT_LONG_FORMAT = new DecimalFormat("0.0000"); 
	
	public static String distanceToStopDescription(Location location, Stop stop) {
		final float distanceTo = distanceTo(location, stop);
		if (location.hasAccuracy()) {
			if (location.getAccuracy() <= 20) {
				return Integer.toString(roundToPlusMinus1(distanceTo));
			}
			if (location.getAccuracy() <= 200) {
				return Integer.toString(roundToPlusMinus10(distanceTo));
			}			
			if (location.getAccuracy() > 200) {
				return "Approximately " + roundToPlusMinus100(distanceTo);
			}
		}	
		return Integer.toString(roundToPlusMinus10(distanceTo));
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
	
	private static int roundToPlusMinus1(final float distanceTo) {
		return Math.round(distanceTo);
	}

	private static int roundToPlusMinus10(final float distanceTo) {
		return Math.round((distanceTo / 10)) * 10;
	}
	
	private static int roundToPlusMinus100(final float distanceTo) {
		return Math.round((distanceTo / 100)) * 100;
	}
		
	public static float distanceTo(Location location, Stop stop) {
		float[] results = new float[1];
		Location.distanceBetween(location.getLatitude(), location.getLongitude(), stop.getLatitude(), stop.getLongitude(), results);
		return results[0];
	}
		
	public static String makeLocationDescription(Location location) {
		StringBuilder description = new StringBuilder(roundLatLong(location.getLatitude()) + ", " + roundLatLong(location.getLongitude()));
		if (location.hasAccuracy()) {
			description.append(" +/- " + location.getAccuracy() + "m");
		}
		return description.toString();
	}
	
	public static String roundLatLong(double value) {
		return LAT_LONG_FORMAT.format(value);
	}
	
}
