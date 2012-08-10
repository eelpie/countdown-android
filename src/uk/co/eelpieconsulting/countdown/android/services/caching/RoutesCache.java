package uk.co.eelpieconsulting.countdown.android.services.caching;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.countdown.android.daos.FileService;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import android.content.Context;
import android.util.Log;

public class RoutesCache {
	
	private static final String TAG = "RoutesCache";
	
	private static final long ONE_DAY = 1 * 24 * 60 * 60 * 1000;

	private Context context;
	
	public RoutesCache(Context context) {
		this.context = context;
	}
	
	public List<Route> getRoutesNear(double latitude, double longitude,  int radius) {
		return getFromCache(getCacheFilenameFor(latitude, longitude, radius));
	}

	public void cacheRoutes(double latitude, double longitude, int radius, List<Route> routes) {
		putIntoCache(routes, getCacheFilenameFor(latitude, longitude, radius));
	}
	
	private String getCacheFilenameFor(double latitude, double longitude, int radius) {
		return SafeFilenameService.makeSafeFilenameFor("routesnear-" + DistanceMeasuringService.roundLatLong(latitude) + "-" + DistanceMeasuringService.roundLatLong(longitude) + "-" + radius);
	}
	
	private List<Route> getFromCache(final String cacheFilename) {
		Log.i(TAG, "Looking for cache file: " + cacheFilename);
		if (FileService.existsLocallyAndIsNotStale(context, cacheFilename, ONE_DAY)) {
			try {
				FileInputStream fileInputStream = FileService.getFileInputStream(context, cacheFilename);
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				List<Route> readObject = (List<Route>) objectInputStream.readObject();
				objectInputStream.close();
				fileInputStream.close();
				return readObject;

			} catch (Exception e) {
				Log.w(TAG, "Failed to read from cache file: " + e.getMessage());
				return null;
			}
		}
		return null;
	}
	
	private void putIntoCache(List<Route> routes, final String cacheFilename) {
		Log.d(TAG, "Writing to disk: " + cacheFilename);
		try {
			FileOutputStream fileOutputStream = FileService.getFileOutputStream(context, cacheFilename);
			ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
			out.writeObject(routes);
			out.close();

		} catch (Exception e) {
			Log.e(TAG, "Failed to write to cache file: " + e.getMessage());
			Log.e(TAG, e.getMessage());
		}
		Log.d(TAG, "Finished writing to disk: " + cacheFilename);
	}

}
