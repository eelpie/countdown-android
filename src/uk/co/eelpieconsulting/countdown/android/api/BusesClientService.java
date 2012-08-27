package uk.co.eelpieconsulting.countdown.android.api;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.BusesClient;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.buses.client.model.StopBoard;
import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkStatusService;

public class BusesClientService {

	private final BusesClient busesClient;
	private final NetworkStatusService networkStatusService;

	public BusesClientService(BusesClient busesClient, NetworkStatusService networkStatusService) {
		this.busesClient = busesClient;
		this.networkStatusService = networkStatusService;
	}

	public StopBoard getStopBoard(int stopId) throws HttpFetchException, ParsingException, NetworkNotAvailableException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.getStopBoard(stopId);
		}
		throw new NetworkNotAvailableException();
	}

	public List<MultiStopMessage> getMultipleStopMessages(int[] stopIds) throws HttpFetchException, ParsingException, NetworkNotAvailableException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.getMultipleStopMessages(stopIds);
		}
		throw new NetworkNotAvailableException();
	}

	public List<Stop> findStopsWithin(double latitude, double longitude, int radius) throws HttpFetchException, ParsingException, NetworkNotAvailableException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.findStopsWithin(latitude, longitude, radius);
		}
		throw new NetworkNotAvailableException();
	}

	public List<Route> findRoutesWithin(double latitude, double longitude, int radius) throws HttpFetchException, ParsingException, NetworkNotAvailableException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.findRoutesWithin(latitude, longitude, radius);
		}
		throw new NetworkNotAvailableException();

	}

	public List<Stop> searchStops(String q) throws HttpFetchException, ParsingException, NetworkNotAvailableException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.searchStops(q);
		}
		throw new NetworkNotAvailableException();

	}

	public List<Stop> getRouteStops(String route, int run) throws HttpFetchException, ParsingException, NetworkNotAvailableException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.getRouteStops(route, run);
		}
		throw new NetworkNotAvailableException();
	}
	
}
