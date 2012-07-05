package uk.co.eelpieconsulting.countdown.android.daos;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import uk.co.eelpieconsulting.countdown.model.Stop;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	public Stop getClosestFavouriteStopTo(Location lastKnownLocation) {
		Stop closestStop = null;
		Iterator<Stop> iterator = getFavouriteStops().iterator();
		while(iterator.hasNext()) {
			final Stop stop = iterator.next();					
			if (closestStop == null) {
				closestStop = stop;
				Log.i(TAG, "Closed stop set to: " + stop.toString());
			}
			
			final double distanceToStop = distanceApart(lastKnownLocation, stop);
			Log.i(TAG, "Distance to " + stop.toString() + ": " + distanceToStop);
			if (distanceToStop < distanceApart(lastKnownLocation, closestStop)) {
				Log.i(TAG, "Closed stop set to: " + stop.toString());
				closestStop = stop;
			}					
		}
		return closestStop;
	}
	
	private double distanceApart(Location lastKnownLocation, Stop stop) {	// TODO correct implementation
		double latitudeDelta = lastKnownLocation.getLatitude() - stop.getLatitude();
		double longitudeDelta = lastKnownLocation.getLongitude() - stop.getLongitude();
		double delta = latitudeDelta + longitudeDelta;
		if (delta < 0) {
			delta = delta * -1;
		}		
		return delta;
	}
	
	private void saveFavouriteStops(Set<Stop> favouriteStops) {
		try {
            FileOutputStream fos = FileService.getFileOutputStream(context, FAVOURITE_STOPS_FILENAME);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(favouriteStops);
            out.close();            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
}
