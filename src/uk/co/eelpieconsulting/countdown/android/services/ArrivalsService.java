package uk.co.eelpieconsulting.countdown.android.services;

import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.buses.client.model.StopBoard;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;

public class ArrivalsService {
		
	private final BusesClientService busesClientService;
	
	public ArrivalsService(BusesClientService busesClientService) {
		this.busesClientService = busesClientService;
	}
	
	public StopBoard getStopBoard(int stopId) throws ContentNotAvailableException {
		try {
			return busesClientService.getStopBoard(stopId);
			
		} catch (NetworkNotAvailableException e) {
			throw new ContentNotAvailableException(e);
		} catch (HttpFetchException e) {
			throw new ContentNotAvailableException(e);		
		} catch (ParsingException e) {
			throw new ContentNotAvailableException(e);
		}
	}

}
