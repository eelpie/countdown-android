package uk.co.eelpieconsulting.countdown.android.views.balloons;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import uk.co.eelpieconsulting.countdown.android.views.maps.GeoPointFactory;

import com.google.android.maps.OverlayItem;

public class StopOverlayItem extends OverlayItem {

	private final Stop stop;
		
	public StopOverlayItem(Stop stop) {
		super(GeoPointFactory.createGeoPointForLatLong(stop.getLatitude(), stop.getLongitude()), StopDescriptionService.makeStopTitle(stop), (stop.getTowards() != null ? "Towards " + stop.getTowards() + "\n" : "") + StopDescriptionService.routesDescription(stop.getRoutes()));
		this.stop = stop;
	}
	
	public Stop getStop() {
		return stop;
	}

}
