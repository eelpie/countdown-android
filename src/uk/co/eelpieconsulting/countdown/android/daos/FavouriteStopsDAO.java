package uk.co.eelpieconsulting.countdown.android.daos;

import java.util.ArrayList;
import java.util.List;

import uk.co.eelpieconsulting.countdown.model.Stop;

public class FavouriteStopsDAO {
	
	public List<Stop> getFavouriteStops() {
		List<Stop> stops = new ArrayList<Stop>();		
		stops.add(new Stop(53550, "York Street / Twickenham", "towards Richmond", "H", 51.44753801609301, -0.32714600966082513));
		stops.add(new Stop(98001, "Slough Bus Station", null, null, 51.51176134396214, -0.5928687139796684));		
		return stops;
	}
	
}
