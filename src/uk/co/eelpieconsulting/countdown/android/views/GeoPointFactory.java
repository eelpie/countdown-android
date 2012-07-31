package uk.co.eelpieconsulting.countdown.android.views;

import com.google.android.maps.GeoPoint;

public class GeoPointFactory {

	public static GeoPoint createGeoPointForLatLong(double latitude, double longitude) {
		return new GeoPoint((int) Math.round(latitude * 1e6), (int) Math.round(longitude * 1e6));
	}

}
