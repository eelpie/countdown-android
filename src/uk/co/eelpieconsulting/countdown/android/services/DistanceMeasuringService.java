package uk.co.eelpieconsulting.countdown.android.services;

import uk.co.eelpieconsulting.countdown.model.Stop;
import android.location.Location;

public class DistanceMeasuringService {
	
	public float distanceTo(Location location, Stop stop) {
		float[] results = new float[1];
		Location.distanceBetween(location.getLatitude(), location.getLongitude(), stop.getLatitude(), stop.getLongitude(), results);
		return results[0];
	}

}
