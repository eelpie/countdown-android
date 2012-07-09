package uk.co.eelpieconsulting.countdown.android.services;

import uk.co.eelpieconsulting.countdown.model.Stop;
import android.location.Location;

public class DistanceMeasuringService {
	
	public static float distanceTo(Location location, Stop stop) {
		float[] results = new float[1];
		Location.distanceBetween(location.getLatitude(), location.getLongitude(), stop.getLatitude(), stop.getLongitude(), results);
		return results[0];
	}
	
	public static String makeLocationDescription(Location location) {
		StringBuilder description = new StringBuilder(location.getLatitude() + ", " + location.getLongitude());
		if (location.hasAccuracy()) {
			description.append(" +/- " + location.getAccuracy() + "m");
		}		
		description.append(", " + Long.toString((System.currentTimeMillis() - location.getTime()) / 1000) + " seconds ago");
		return description.toString();
	}

}
