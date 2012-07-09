package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.countdown.model.Stop;

public class StopDescriptionService {

	public static String makeStopDescription(Stop stop) {
		StringBuilder description = new StringBuilder(stop.getName() + (stop.getStopIndicator() != null ? " (" + stop.getStopIndicator() + ") " : "") + "\n");
		if (stop.getTowards() != null) {
			description.append("Towards " + stop.getTowards() + "\n");
		}
		description.append(stop.getLatitude() + ", " + stop.getLongitude());
		return description.toString();
	}
	
}
