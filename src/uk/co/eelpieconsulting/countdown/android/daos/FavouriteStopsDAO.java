package uk.co.eelpieconsulting.countdown.android.daos;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.services.DistanceMeasuringService;
import android.content.Context;
import android.location.Location;
import android.util.Log;

public class FavouriteStopsDAO {
	
	private static final String TAG = "FavouriteStopsDAO";
	
	private static final String FAVOURITE_STOPS_FILENAME = "favourite-stops.ser";
	
	private static FavouriteStopsDAO dao;

	private Context context;
	
	public static FavouriteStopsDAO get(Context context) {
		if (dao == null) {
			dao = new FavouriteStopsDAO(context);
		}
		return dao;		
	}
	
	public FavouriteStopsDAO(Context context) {
		this.context = context;
	}

	public Set<Stop> getFavouriteStops() {
		try {
			if (FileService.existsLocally(context, FAVOURITE_STOPS_FILENAME)) {
				FileInputStream fis = FileService.getFileInputStream(context, FAVOURITE_STOPS_FILENAME);
				ObjectInputStream in = new ObjectInputStream(fis);
				@SuppressWarnings("unchecked")
				Set<Stop> favouriteStops = (Set<Stop>) in.readObject();
				in.close();
				fis.close();			
				return favouriteStops;
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}		
		return new HashSet<Stop>();
	}
	
	public void addFavourite(Stop stop) {
		Set<Stop> favouriteStops = getFavouriteStops();
		favouriteStops.add(stop);
		saveFavouriteStops(favouriteStops);
	}
	
	public void removeFavourite(Stop stop) {
		Set<Stop> favouriteStops = getFavouriteStops();
		favouriteStops.remove(stop);
		saveFavouriteStops(favouriteStops);
	}
	
	public Stop getFirstFavouriteStop() {
		Set<Stop> favouriteStops = getFavouriteStops();
		if (favouriteStops.iterator().hasNext()) {
			return favouriteStops.iterator().next();
		}
		return null;
	}
	
	public Stop getClosestFavouriteStopTo(Location location) {
		Stop closestStop = null;
		Iterator<Stop> iterator = getFavouriteStops().iterator();
		while(iterator.hasNext()) {
			final Stop stop = iterator.next();					
			if (closestStop == null) {
				closestStop = stop;
			}
			final double distanceToStop = DistanceMeasuringService.distanceTo(location, stop);
			if (distanceToStop < DistanceMeasuringService.distanceTo(location, closestStop)) {
				closestStop = stop;
			}					
		}
		return closestStop;
	}
	
	public boolean isFavourite(Stop stop) {
		return getFavouriteStops().contains(stop);
	}
	
	private void saveFavouriteStops(Set<Stop> favouriteStops) {
		try {
            FileOutputStream fos = FileService.getFileOutputStream(context, FAVOURITE_STOPS_FILENAME);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(favouriteStops);
            out.close();            
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}		
	}

	public boolean hasFavourites() {
		return !getFavouriteStops().isEmpty();
	}
	
}
