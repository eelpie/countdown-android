package uk.co.eelpieconsulting.countdown.android.services;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.services.caching.RoutesCache;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import android.content.Context;
import android.util.Log;

public class RoutesService {
	
	private static final String TAG = "RoutesService";
	
	private final BusesClientService busesClientService;
	private final RoutesCache routesCache;
	private final Context context;

	public RoutesService(BusesClientService busesClientService, RoutesCache routesCache, Context context) {
		this.busesClientService = busesClientService;
		this.routesCache = routesCache;
		this.context = context;
	}

	public List<Route> findRoutesWithin(double latitude, double longitude, int radius) throws ContentNotAvailableException {
		try {
			final List<Route> cachedResults = routesCache.getRoutesNear(latitude, longitude, radius);
			final boolean cachedResultsAreAvailable = cachedResults != null;
			if (cachedResultsAreAvailable) {
				Log.i(TAG, "Returning route stops from cache");
				return cachedResults;
			}
			
			final List<Route> routes = busesClientService.findRoutesWithin(latitude, longitude, radius);
			routesCache.cacheRoutes(latitude, longitude, radius, routes);
			return routes;		
			
		} catch (NetworkNotAvailableException e) {
			throw new ContentNotAvailableException(context.getString(R.string.no_network_available));
		} catch (HttpFetchException e) {
			throw new ContentNotAvailableException(e);		
		} catch (ParsingException e) {
			throw new ContentNotAvailableException(e);
		}		
	}

}
