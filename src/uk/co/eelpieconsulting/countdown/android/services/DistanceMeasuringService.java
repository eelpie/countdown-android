package uk.co.eelpieconsulting.countdown.android.services;

import uk.co.eelpieconsulting.countdown.model.Stop;
import android.location.Location;

public class DistanceMeasuringService {
	
	public double distanceTo(Location location, Stop stop) {	// TODO correct implementation
		double latitudeDelta = location.getLatitude() - stop.getLatitude();
		double longitudeDelta = location.getLongitude() - stop.getLongitude();
		double delta = latitudeDelta + longitudeDelta;
		if (delta < 0) {
			delta = delta * -1;
		}
		return delta;
	}

}
