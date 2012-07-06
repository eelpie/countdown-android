package uk.co.eelpieconsulting.countdown.android.api;

import uk.co.eelpieconsulting.countdown.api.CountdownApi;

public class CountdownApiFactory {

	private static final String API_BASE_URL = "http://countdown.api.tfl.gov.uk";

	public static CountdownApi getApi() {
		return new CountdownApi(API_BASE_URL);
	}
	
}
