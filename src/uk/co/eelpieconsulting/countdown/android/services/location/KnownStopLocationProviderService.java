package uk.co.eelpieconsulting.countdown.android.services.location;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import android.location.Location;
import android.os.Bundle;

public class KnownStopLocationProviderService {
	
	public static final String KNOWN_STOP_LOCATION = "knownStopLocation";
	
	public static Location makeLocationForSelectedStop(Stop stop) {
		final Location stopLocation = new Location(KNOWN_STOP_LOCATION);
		stopLocation.setAccuracy(1);
		stopLocation.setLatitude(stop.getLatitude());
		stopLocation.setLongitude(stop.getLongitude());
		Bundle extras = new Bundle();
		extras.putSerializable("stop", stop);
		stopLocation.setExtras(extras);
		return stopLocation;
	}

}
