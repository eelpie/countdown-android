package uk.co.eelpieconsulting.countdown.android.views;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;

public class StopDescriptionService {
	
	public static TextView makeStopView(Stop stop, Context context, Activity activity) {
		final TextView stopTextView = new TextView(context);
		stopTextView.setText(StopDescriptionService.makeStopDescription(stop) + "\n\n");
		stopTextView.setOnClickListener(new StopClicker(activity, stop));
		return stopTextView;
	}

	public static String makeStopDescription(Stop stop) {
		final StringBuilder description = new StringBuilder(makeStopTitle(stop) + "\n");
		if (stop.getTowards() != null) {
			description.append("Towards " + stop.getTowards() + "\n");
		}
		description.append(routesDescription(stop.getRoutes()));	
		return description.toString();
	}
	
	public static String makeStopTitle(Stop stop) {
		return stop.getName() + (stop.getIndicator() != null ? " (" + stop.getIndicator() + ") " : "");
	}

	public static String routesDescription(Set<Route> routes) {
		final StringBuilder routesDescription = new StringBuilder();		
		for (String routeName : filterOutDuplicateRouteNamesAtTerminals(routes)) {
			routesDescription.append("[" + routeName + "] ");			
		}
		return routesDescription.toString().trim();
	}

	private static Set<String> filterOutDuplicateRouteNamesAtTerminals(Set<Route> routes) {
		final Set<String> routeNames = new HashSet<String>();
		for (Route route : routes) {
			routeNames.add(route.getRoute());
		}
		return routeNames;
	}
	
}
