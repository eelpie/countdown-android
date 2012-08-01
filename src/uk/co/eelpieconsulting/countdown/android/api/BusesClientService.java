package uk.co.eelpieconsulting.countdown.android.api;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.BusesClient;
import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.buses.client.model.StopBoard;
import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkStatusService;

public class BusesClientService {

	private final BusesClient busesClient;
	private final NetworkStatusService networkStatusService;

	public BusesClientService(BusesClient busesClient, NetworkStatusService networkStatusService) {
		this.busesClient = busesClient;
		this.networkStatusService = networkStatusService;
	}

	public List<MultiStopMessage> getStopMessages(int stopId) throws HttpFetchException, ParsingException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.getStopMessages(stopId);
		}
		return null;	// TODO
	}

	public StopBoard getStopBoard(int stopId) throws HttpFetchException, ParsingException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.getStopBoard(stopId);
		}
		return null;	// TODO
	}

	public List<MultiStopMessage> getMultipleStopMessages(int[] stopIds) throws HttpFetchException, ParsingException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.getMultipleStopMessages(stopIds);
		}
		return null;	// TODO
	}

	public List<Stop> findStopsWithin(double latitude, double longitude, int radius) throws HttpFetchException, ParsingException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.findStopsWithin(latitude, longitude, radius);
		}
		return null;	// TODO
	}

	public List<Route> findRoutesWithin(double latitude, double longitude, int radius) throws HttpFetchException, ParsingException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.findRoutesWithin(latitude, longitude, radius);
		}
		return null;	// TODO

	}

	public List<Stop> searchStops(String q) throws HttpFetchException, ParsingException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.searchStops(q);
		}
		return null;	// TODO

	}

	public List<Stop> getRouteStops(String route, int run) throws HttpFetchException, ParsingException {
		if (networkStatusService.isConnectionAvailable()) {
			return busesClient.getRouteStops(route, run);
		}
		return null;	// TODO
	}
	
}
