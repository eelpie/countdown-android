package uk.co.eelpieconsulting.countdown.android.model;

import java.util.List;

import uk.co.eelpieconsulting.countdown.model.Stop;

public class Route {

	private final String name;
	private final int run;
	private final List<Stop> stops;
	
	public Route(String name, int run, List<Stop> stops) {
		this.name = name;
		this.run = run;
		this.stops = stops;
	}

	public String getName() {
		return name;
	}

	public int getRun() {
		return run;
	}

	public List<Stop> getStops() {
		return stops;
	}
	
	@Override
	public String toString() {
		return "Route [name=" + name + ", run=" + run + ", stops=" + stops + "]";
	}
	
}
