package uk.co.eelpieconsulting.countdown.android.services;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.services.caching.StopsCache;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import android.content.Context;
import android.util.Log;

public class StopsService {
	
	private static final String TAG = "StopsService";
	
	private final BusesClientService busesClientService;
	private final StopsCache stopsCache;
	private final Context context;
	
	public StopsService(BusesClientService busesClientService, StopsCache stopsCache, Context context) {
		this.busesClientService = busesClientService;
		this.stopsCache = stopsCache;
		this.context = context;
	}

	public List<Stop> getRouteStops(String route, int run) throws ContentNotAvailableException {		
		try {
			final List<Stop> cachedResults = stopsCache.getRouteStops(route, run);
			final boolean cachedResultsAreAvailable = cachedResults != null;
			if (cachedResultsAreAvailable) {
				Log.i(TAG, "Returning route stops from cache");
				return cachedResults;
			}
			
			final List<Stop> stops = busesClientService.getRouteStops(route, run);
			stopsCache.cacheStops(route, run, stops);
			return stops;
			
		} catch (NetworkNotAvailableException e) {
			throw new ContentNotAvailableException(context.getString(R.string.no_network_available));
		} catch (HttpFetchException e) {
			throw new ContentNotAvailableException(e);		
		} catch (ParsingException e) {
			throw new ContentNotAvailableException(e);
		}		
	}

	public List<Stop> findStopsWithin(double latitude, double longitude, int radius) throws ContentNotAvailableException {
		try {
			final List<Stop> cachedResults = stopsCache.getStopsWithin(latitude, longitude, radius);
			final boolean cachedResultsAreAvailable = cachedResults != null;
			if (cachedResultsAreAvailable) {
				Log.i(TAG, "Returning stops from cache");
				return cachedResults;
			}
			
			final List<Stop> stops = busesClientService.findStopsWithin(latitude, longitude, radius);
			stopsCache.cacheStops(latitude, longitude, radius, stops);
			return stops;
			
		} catch (NetworkNotAvailableException e) {
			throw new ContentNotAvailableException(context.getString(R.string.no_network_available));
		} catch (HttpFetchException e) {
			throw new ContentNotAvailableException(e);		
		} catch (ParsingException e) {
			throw new ContentNotAvailableException(e);
		}		
	}

	public List<Stop> searchStops(String q) throws ContentNotAvailableException {
		try {
			final List<Stop> cachedResults = stopsCache.getSearchResultsFor(q);
			final boolean cachedResultsAreAvailable = cachedResults != null;
			if (cachedResultsAreAvailable) {
				Log.i(TAG, "Returning stop search results from cache");
				return cachedResults;
			}
			
			final List<Stop> stops = busesClientService.searchStops(q);
			stopsCache.cacheStopSearchResults(q, stops);
			
			return stops;
		} catch (NetworkNotAvailableException e) {
			throw new ContentNotAvailableException(context.getString(R.string.no_network_available));
		} catch (HttpFetchException e) {
			throw new ContentNotAvailableException(e);		
		} catch (ParsingException e) {
			throw new ContentNotAvailableException(e);
		}
	}

}
