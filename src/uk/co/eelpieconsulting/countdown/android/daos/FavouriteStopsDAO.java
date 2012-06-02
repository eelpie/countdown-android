package uk.co.eelpieconsulting.countdown.android.daos;

import java.util.ArrayList;
import java.util.List;

import uk.co.eelpieconsulting.countdown.model.Stop;

public class FavouriteStopsDAO {
	
	public List<Stop> getFavouriteStops() {
		List<Stop> stops = new ArrayList<Stop>();
		stops.add(new Stop(53550, "York Street / Twickenham", 51.44753801609301, -0.32714600966082513));
		return stops;
	}
	
}
