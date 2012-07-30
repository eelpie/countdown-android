package uk.co.eelpieconsulting.countdown.android.views;

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
		final StringBuilder description = new StringBuilder(stop.getName() + (stop.getIndicator() != null ? " (" + stop.getIndicator() + ") " : "") + "\n");		
		if (stop.getTowards() != null) {
			description.append("Towards " + stop.getTowards() + "\n");
		}
		description.append(routesDescription(stop.getRoutes()));	
		return description.toString();
	}

	public static String routesDescription(Set<Route> routes) {
		StringBuilder routesDescription = new StringBuilder();
		for (Route route : routes) {
			routesDescription.append("[" + route.getRoute() + "] ");
		}
		return routesDescription.toString().trim();
	}
	
}
