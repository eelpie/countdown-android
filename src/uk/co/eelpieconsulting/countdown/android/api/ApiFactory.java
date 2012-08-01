package uk.co.eelpieconsulting.countdown.android.api;

import android.content.Context;
import uk.co.eelpieconsulting.buses.client.BusesClient;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkStatusService;

public class ApiFactory {

	private static final String INHOUSE_API_BASE_URL = "http://buses.eelpieconsulting.co.uk";
	
	public static BusesClientService getApi(Context context) {
		return new BusesClientService(new BusesClient(INHOUSE_API_BASE_URL), new NetworkStatusService(context));
	}
	
}
