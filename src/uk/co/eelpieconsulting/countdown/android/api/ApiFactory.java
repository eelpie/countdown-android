package uk.co.eelpieconsulting.countdown.android.api;

import android.content.Context;
import uk.co.eelpieconsulting.buses.client.BusesClient;
import uk.co.eelpieconsulting.countdown.android.services.ArrivalsService;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkStatusService;

public class ApiFactory {

	private static final String INHOUSE_API_BASE_URL = "http://buses.eelpieconsulting.co.uk";

	private static ArrivalsService arrivalsService;

	private static BusesClientService busesClientService;
	
	public static BusesClientService getApi(Context context) {
		if (busesClientService == null) {
			busesClientService = new BusesClientService(new BusesClient(INHOUSE_API_BASE_URL), new NetworkStatusService(context));
		}
		return busesClientService;
	}
	
	public static ArrivalsService getArrivalsService(Context context) {
		if (arrivalsService == null) {
			arrivalsService = new ArrivalsService(getApi(context));
		}
		return arrivalsService;
	}
	
}
