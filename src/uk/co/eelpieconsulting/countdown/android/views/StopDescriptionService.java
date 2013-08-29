package uk.co.eelpieconsulting.countdown.android.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.model.Article;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import uk.co.eelpieconsulting.countdown.android.services.location.KnownStopLocationProviderService;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.widget.TextView;

public class StopDescriptionService {
	
	private static final String NEW_LINE = "\n";

	public static TextView makeStopView(Stop stop, Context context, Activity activity) {
		final TextView stopTextView = new TextView(context);
		stopTextView.setText(StopDescriptionService.makeStopDescription(stop, true));
		stopTextView.setOnClickListener(new StopClicker(activity, stop));
		return stopTextView;
	}
	
	public static TextView makeArticleView(Article article, Context context) {
		final TextView articleTextView = new TextView(context);
		articleTextView.setText(article.getTitle() + NEW_LINE + NEW_LINE + article.getBody());
		return articleTextView;
	}
		
	public static String makeStopDescription(Stop stop, boolean showRouteNumbers) {
		final StringBuilder description = new StringBuilder(makeStopTitle(stop));
		description.append(NEW_LINE);
		if (stop.getTowards() != null) {
			description.append("Towards ");
			description.append(stop.getTowards());
			description.append(NEW_LINE);
		}
		if (showRouteNumbers) {
			description.append(routesDescription(stop.getRoutes()));
		}
		return description.toString();
	}
	
	public static String makeStopDescription(Stop stop, Location location, boolean showRouteNumbers) {
		StringBuilder description = new StringBuilder(makeStopDescription(stop, showRouteNumbers));
		if (location != null) {
			final float distanceTo = DistanceMeasuringService.distanceTo(location, stop);
			if (distanceTo > 0 && distanceTo < 1000) {
				description.append(NEW_LINE);
				description.append(DistanceMeasuringService.distanceToStopDescription(location, stop));
				
				if (location.getProvider().equals(KnownStopLocationProviderService.KNOWN_STOP_LOCATION)) {
					final Stop selectedStop = (Stop) location.getExtras().getSerializable("stop");
					description.append(" from " + StopDescriptionService.makeStopTitle(selectedStop) + "\n\n");
					
				} else {
					description.append("\n\n");
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
			routesDescription.append(routeName + " ");
		}
		return routesDescription.toString().trim();
	}
	
	public static String secondsToMinutes(long estimatedWait, Context context) {	// TODO Needs the context? No static getString?
		final long minutes = estimatedWait / 60;
		if (minutes == 0) {
			return  context.getString(R.string.due);
		}
		if (minutes == 1) {
			return "1 " + context.getString(R.string.minute);
		}
		return minutes + " " +  context.getString(R.string.minutes);
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
