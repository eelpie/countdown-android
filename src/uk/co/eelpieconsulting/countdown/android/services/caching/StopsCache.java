package uk.co.eelpieconsulting.countdown.android.services.caching;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import uk.co.eelpieconsulting.buses.client.model.StopsNear;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.daos.FileService;
import uk.co.eelpieconsulting.countdown.android.services.location.DistanceMeasuringService;
import android.content.Context;
import android.util.Log;

public class StopsCache {
	
	private static final String TAG = "StopsCache";
	
	private static final long ONE_DAY = 1 * 24 * 60 * 60 * 1000;

	private Context context;
	
	public StopsCache(Context context) {
		this.context = context;
	}
	
	public List<Stop> getRouteStops(String route, int run) {
		return getFromCache(getCacheFilenameFor(route, run));
	}
	
	public void cacheStops(String route, int run, List<Stop> stops) {
		putIntoCache(stops, getCacheFilenameFor(route, run));
	}

	public List<Stop> getStopsWithin(double latitude, double longitude,  int radius) {
		return getFromCache(getCacheFilenameFor(latitude, longitude, radius));
	}
	
	public StopsNear getStopsNear(double latitude, double longitude, int radius) {
		return getStopsNearybyFromCache(getCacheFilenameForStopsNear(latitude, longitude, radius));
	}

	public void cacheStops(double latitude, double longitude, int radius, List<Stop> stops) {
		putIntoCache(stops, getCacheFilenameFor(latitude, longitude, radius));
	}
	
	public List<Stop> getSearchResultsFor(String q) {
		return getFromCache(getCacheFilenameFor(q));
	}
	
	public void cacheStopSearchResults(String q, List<Stop> stops) {
		putIntoCache(stops, getCacheFilenameFor(q));		
	}
	
	public void cacheNearbyStops(double latitude, double longitude, int radius, StopsNear stopsNear) {
		putIntoCache(stopsNear, getCacheFilenameForStopsNear(latitude, longitude, radius));
	}
	
	private String getCacheFilenameFor(String route, int run) {
		return SafeFilenameService.makeSafeFilenameFor("routestops-" + route + "-" + run);
	}
	
	private String getCacheFilenameFor(double latitude, double longitude, int radius) {
		return SafeFilenameService.makeSafeFilenameFor("stopsnear-" + DistanceMeasuringService.roundLatLong(latitude) + "-" + DistanceMeasuringService.roundLatLong(longitude) + "-" + radius);
	}
	
	private String getCacheFilenameForStopsNear(double latitude, double longitude, int radius) {
		return SafeFilenameService.makeSafeFilenameFor("stopsnearresolved-" + DistanceMeasuringService.roundLatLong(latitude) + "-" + DistanceMeasuringService.roundLatLong(longitude) + "-" + radius);
	}
	
	private String getCacheFilenameFor(String q) {
		return  SafeFilenameService.makeSafeFilenameFor("stopsearch-" + q);
	}
	
	private List<Stop> getFromCache(final String cacheFilename) {
		Log.i(TAG, "Looking for cache file: " + cacheFilename);
		if (FileService.existsLocallyAndIsNotStale(context, cacheFilename, ONE_DAY)) {
			try {
				FileInputStream fileInputStream = FileService.getFileInputStream(context, cacheFilename);
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				List<Stop> readObject = (List<Stop>) objectInputStream.readObject();
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
	
	private StopsNear getStopsNearybyFromCache(final String cacheFilename) {	// TODO generics
		Log.i(TAG, "Looking for cache file: " + cacheFilename);
		if (FileService.existsLocallyAndIsNotStale(context, cacheFilename, ONE_DAY)) {
			try {
				FileInputStream fileInputStream = FileService.getFileInputStream(context, cacheFilename);
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				StopsNear readObject = (StopsNear) objectInputStream.readObject();
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
	
	private void putIntoCache(List<Stop> stops, final String cacheFilename) {
		Log.d(TAG, "Writing to disk: " + cacheFilename);
		try {
			FileOutputStream fileOutputStream = FileService.getFileOutputStream(context, cacheFilename);
			ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
			out.writeObject(stops);
			out.close();

		} catch (Exception e) {
			Log.e(TAG, "Failed to write to cache file: " + e.getMessage());
			Log.e(TAG, e.getMessage());
		}
		Log.d(TAG, "Finished writing to disk: " + cacheFilename);
	}
	
	private void putIntoCache(StopsNear stopsNear, final String cacheFilename) {
		Log.d(TAG, "Writing to disk: " + cacheFilename);
		try {
			FileOutputStream fileOutputStream = FileService.getFileOutputStream(context, cacheFilename);
			ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
			out.writeObject(stopsNear);
			out.close();

		} catch (Exception e) {
			Log.e(TAG, "Failed to write to cache file: " + e.getMessage());
			Log.e(TAG, e.getMessage());
		}
		Log.d(TAG, "Finished writing to disk: " + cacheFilename);
	}
	
	
}
