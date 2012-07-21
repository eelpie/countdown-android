package uk.co.eelpieconsulting.countdown.android.services;

import java.util.Comparator;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import android.location.Location;

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
