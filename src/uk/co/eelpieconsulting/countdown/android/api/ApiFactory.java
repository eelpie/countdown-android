package uk.co.eelpieconsulting.countdown.android.api;

import uk.co.eelpieconsulting.buses.client.BusesClient;

public class ApiFactory {

	private static final String INHOUSE_API_BASE_URL = "http://buses.eelpieconsulting.co.uk";
	
	public static BusesClient getApi() {
		return new BusesClient(INHOUSE_API_BASE_URL);
	}
	
}
