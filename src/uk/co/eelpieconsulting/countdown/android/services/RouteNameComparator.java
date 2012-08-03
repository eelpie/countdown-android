package uk.co.eelpieconsulting.countdown.android.services;

import java.util.Comparator;

import uk.co.eelpieconsulting.busroutes.model.Route;

public class RouteNameComparator implements Comparator<Route> {
	
	public int compare(Route lhs, Route rhs) {
		return lhs.getRoute().compareTo(rhs.getRoute());
	}
	
}
