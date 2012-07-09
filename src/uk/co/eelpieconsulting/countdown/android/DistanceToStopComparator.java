package uk.co.eelpieconsulting.countdown.android;

import java.util.Comparator;

import android.location.Location;

import uk.co.eelpieconsulting.countdown.android.services.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.model.Stop;

public class DistanceToStopComparator implements Comparator<Stop> {
	
	private final Location location;
	
	public DistanceToStopComparator(Location location) {
		this.location = location;
	}
	
	public int compare(Stop lhs, Stop rhs) {
		if (DistanceMeasuringService.distanceTo(location, lhs) == DistanceMeasuringService.distanceTo(location, rhs)) {
			return 0;
		}
		if (DistanceMeasuringService.distanceTo(location, lhs) < DistanceMeasuringService.distanceTo(location, rhs)) {
			return -1;
		}
		return 1;
	}
	

}
