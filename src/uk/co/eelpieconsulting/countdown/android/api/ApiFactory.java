package uk.co.eelpieconsulting.countdown.android.api;

import uk.co.eelpieconsulting.buses.client.CountdownApi;

public class ApiFactory {

	private static final String INHOUSE_API_BASE_URL = "http://buses.eelpieconsulting.co.uk";
	
	public static CountdownApi getApi() {
		return new CountdownApi(INHOUSE_API_BASE_URL);
	}
	
}
