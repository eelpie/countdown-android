package uk.co.eelpieconsulting.countdown.android.views.maps;

import android.location.Location;

import com.google.android.maps.GeoPoint;

public class GeoPointFactory {

	public static GeoPoint createGeoPointForLatLong(double latitude, double longitude) {
		return new GeoPoint((int) Math.round(latitude * 1e6), (int) Math.round(longitude * 1e6));
	}

	public static GeoPoint createGeoPointForLatLong(Location location) {
		return createGeoPointForLatLong(location.getLatitude(), location.getLongitude());
	}

}
