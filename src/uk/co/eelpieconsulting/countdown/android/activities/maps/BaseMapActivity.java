package uk.co.eelpieconsulting.countdown.android.activities.maps;

import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.services.location.LocationService;
import uk.co.eelpieconsulting.countdown.android.views.balloons.LocationCircleOverlay;
import uk.co.eelpieconsulting.countdown.android.views.maps.GeoPointFactory;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class BaseMapActivity  extends MapActivity implements LocationListener {
		
	protected MapView mapView;
	
	protected LocationCircleOverlay locationCircleOverlay;

	protected Location currentLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.mapstops);
        
        mapView = (MapView) findViewById(R.id.map);		
        mapView.setBuiltInZoomControls(false);
		mapView.setClickable(true);
		
		locationCircleOverlay = new LocationCircleOverlay();
		mapView.getOverlays().add(locationCircleOverlay);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mapView.setVisibility(View.VISIBLE);		
	}
	
	@Override
	protected void onPause() {		
		super.onPause();
		LocationService.turnOffLocationUpdates(this.getApplicationContext(), this);
		mapView.setVisibility(View.GONE);
	}
	
	@Override
	protected boolean isRouteDisplayed() {		
		return false;
	}

	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub		
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub		
	}
	
	protected void zoomMapToLocation(Location location) {
		mapView.getController().animateTo(GeoPointFactory.createGeoPointForLatLong(location.getLatitude(), location.getLongitude()));
		mapView.getController().setZoom(17);		
		locationCircleOverlay.setPoint(location);
		mapView.postInvalidate();
	}
	
}
