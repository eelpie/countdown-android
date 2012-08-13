package uk.co.eelpieconsulting.countdown.android.activities.maps;

import uk.co.eelpieconsulting.countdown.android.R;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class BaseMapActivity  extends MapActivity implements LocationListener {
	
	protected MapView mapView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.mapstops);
        
        mapView = (MapView) findViewById(R.id.map);		
        mapView.setBuiltInZoomControls(false);
		mapView.setClickable(true);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mapView.setVisibility(View.VISIBLE);		
	}
	
	@Override
	protected void onPause() {		
		super.onPause();
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
	
}
