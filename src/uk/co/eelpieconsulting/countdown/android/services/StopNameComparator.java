package uk.co.eelpieconsulting.countdown.android.services;

import java.util.Comparator;

import uk.co.eelpieconsulting.busroutes.model.Stop;

public class StopNameComparator implements Comparator<Stop> {
	
	public int compare(Stop lhs, Stop rhs) {
		return lhs.getName().compareTo(rhs.getName());
	}
	
}
