package uk.co.eelpieconsulting.countdown.android.api;

import uk.co.eelpieconsulting.buses.client.CountdownApi;

public class CountdownApiFactory {

	private static final String INHOUSE_API_BASE_URL = "http://buses.eelpieconsulting.co.uk";
	private static final String COUNTDOWN_API_BASE_URL = "http://countdown.api.tfl.gov.uk";

	public static CountdownApi getApi() {
		return new CountdownApi(INHOUSE_API_BASE_URL);
	}
	
	public static uk.co.eelpieconsulting.countdown.api.CountdownApi getCountdownApi() {
		return new uk.co.eelpieconsulting.countdown.api.CountdownApi(COUNTDOWN_API_BASE_URL);
	}
	
}
