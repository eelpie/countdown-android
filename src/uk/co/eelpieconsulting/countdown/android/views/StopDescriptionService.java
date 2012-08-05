package uk.co.eelpieconsulting.countdown.android.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.NearbyStopsListActivity;
import uk.co.eelpieconsulting.countdown.android.services.DistanceMeasuringService;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.widget.TextView;

public class StopDescriptionService {
	
	private static final String NEW_LINE = "\n";

	public static TextView makeStopView(Stop stop, Context context, Activity activity) {
		final TextView stopTextView = new TextView(context);
		stopTextView.setText(StopDescriptionService.makeStopDescription(stop));
		stopTextView.setOnClickListener(new StopClicker(activity, stop));
		return stopTextView;
	}
	
	public static String makeStopDescription(Stop stop) {
		final StringBuilder description = new StringBuilder(makeStopTitle(stop));
		description.append(NEW_LINE);
		if (stop.getTowards() != null) {
			description.append("Towards ");
			description.append(stop.getTowards());
			description.append(NEW_LINE);
		}
		description.append(routesDescription(stop.getRoutes()));	
		return description.toString();
	}
	
	public static String makeStopDescription(Stop stop, Location location) {
		StringBuilder description = new StringBuilder(makeStopDescription(stop));
		if (location != null) {
			if (DistanceMeasuringService.distanceTo(location, stop) < 1000) {
				description.append(NEW_LINE);
				description.append(DistanceMeasuringService.distanceToStopDescription(location, stop));
				if (location.getProvider().equals(NearbyStopsListActivity.KNOWN_STOP_LOCATION)) {
					final Stop selectedStop = (Stop) location.getExtras().getSerializable("stop");
					description.append(" metres away from " + StopDescriptionService.makeStopTitle(selectedStop) + "\n\n");					 
				} else {
					description.append(" metres away\n\n");
				}
			}
		}
		return description.toString();
	}
	
	public static String makeStopTitle(Stop stop) {
		return stop.getName() + (stop.getIndicator() != null ? " (" + stop.getIndicator() + ") " : "");
	}
	
	public static String routesDescription(Set<Route> routes) {
		final StringBuilder routesDescription = new StringBuilder();		
		for (String routeName : sortAndFilterOutDuplicateRouteNamesAtTerminals(routes)) {
			routesDescription.append("[" + routeName + "] ");			
		}
		return routesDescription.toString().trim();
	}

	private static List<String> sortAndFilterOutDuplicateRouteNamesAtTerminals(Set<Route> routes) {
		final List<String> routeNames = new ArrayList<String>();
		for (Route route : routes) {
			if (!routeNames.contains(route.getRoute())) {
				routeNames.add(route.getRoute());				
			}
		}
		Collections.sort(routeNames);
		return routeNames;
	}
	
}
