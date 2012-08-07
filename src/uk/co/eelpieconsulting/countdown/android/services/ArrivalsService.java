package uk.co.eelpieconsulting.countdown.android.services;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.buses.client.model.StopBoard;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.model.CachedStopBoard;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;

public class ArrivalsService {
	
	private static final String TAG = "ArrivalsService";
	
	private static final long ARRIVALS_CACHE_TTL = 30000;
	
	private final BusesClientService busesClientService;
	
	Map<Integer, CachedStopBoard> cache;
	
	public ArrivalsService(BusesClientService busesClientService) {
		this.busesClientService = busesClientService;
		cache = new HashMap<Integer, CachedStopBoard>();
	}
	
	public StopBoard getStopBoard(int stopId) throws ContentNotAvailableException {
		try {
						
			final CachedStopBoard cachedStopBoard = cache.get(stopId);
			if (cachedStopBoard != null) {
				if (cachedStopBoard.getFetchDate() + ARRIVALS_CACHE_TTL > System.currentTimeMillis()) {
					Log.i(TAG, "Returning arrivals for stop " + stopId + " from cache");
					return cachedStopBoard.getStopBoard();
				}
				Log.i(TAG, "Removing expired arrivals for stop " + stopId + " from cache");
				cache.remove(stopId);
			}
					
			Log.i(TAG, "Making live call for stop " + stopId + " arrivals");
			final StopBoard stopBoard = busesClientService.getStopBoard(stopId);
			if (stopBoard != null) {
				cache.put(stopId, new CachedStopBoard(stopBoard, System.currentTimeMillis()));
			}
			return stopBoard;
			
		} catch (NetworkNotAvailableException e) {
			throw new ContentNotAvailableException(e);
		} catch (HttpFetchException e) {
			throw new ContentNotAvailableException(e);		
		} catch (ParsingException e) {
			throw new ContentNotAvailableException(e);
		}
	}

}
