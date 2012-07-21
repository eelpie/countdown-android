package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.busroutes.model.Stop;

public class StopDescriptionService {

	public static String makeStopDescription(Stop stop) {
		StringBuilder description = new StringBuilder(stop.getName() + (stop.getIndicator() != null ? " (" + stop.getIndicator() + ") " : "") + "\n");
		description.append(stop.getId() + "\n");
		
		if (stop.getTowards() != null) {
			description.append("Towards " + stop.getTowards() + "\n");
		}
		description.append(stop.getLatitude() + ", " + stop.getLongitude());
		return description.toString();
	}
	
}
