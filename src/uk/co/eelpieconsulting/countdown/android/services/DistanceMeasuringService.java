package uk.co.eelpieconsulting.countdown.android.services;

import java.text.DecimalFormat;

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
		StringBuilder description = new StringBuilder(LAT_LONG_FORMAT.format(location.getLatitude()) + ", " + LAT_LONG_FORMAT.format(location.getLongitude()));
		if (location.hasAccuracy()) {
			description.append(" +/- " + location.getAccuracy() + "m");
		}
		return description.toString();
	}

}
