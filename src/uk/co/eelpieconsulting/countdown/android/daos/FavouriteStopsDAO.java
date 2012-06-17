package uk.co.eelpieconsulting.countdown.android.daos;

import java.util.HashSet;
import java.util.Set;

import uk.co.eelpieconsulting.countdown.model.Stop;

public class FavouriteStopsDAO {
	
	private static FavouriteStopsDAO dao;
	
	public static FavouriteStopsDAO get() {
		if (dao == null) {
			dao = new FavouriteStopsDAO();
		}
		return dao;		
	}
	
	private Set<Stop> stops;
	
	private FavouriteStopsDAO() {
		stops = new HashSet<Stop>();		
		stops.add(new Stop(53550, "York Street / Twickenham", "towards Richmond", "H", 51.44753801609301, -0.32714600966082513));
		stops.add(new Stop(98001, "Slough Bus Station", null, null, 51.51176134396214, -0.5928687139796684));		
	}

	public Set<Stop> getFavouriteStops() {
		return stops;
	}
	
	public void addFavourite(Stop stop) {
		stops.add(stop);
	}

	public void removeFavourite(Stop stop) {		
		stops.remove(stop);
	}
	
	public Stop getFirstFavouriteStop() {
		return stops.iterator().next();
	}
	
}
