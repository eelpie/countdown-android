package uk.co.eelpieconsulting.countdown.android.views.balloons;

import uk.co.eelpieconsulting.countdown.android.views.maps.GeoPointFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.location.Location;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class LocationCircleOverlay extends Overlay {
	
	private Location location;
	private GeoPoint point;
	private Paint paint;

	public LocationCircleOverlay() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStrokeWidth(2.0f);
		
		paint.setColor(0xff6666ff);
		paint.setStyle(Style.STROKE);
	}
	
	public void setPoint(Location location) {
		this.location = location;
		this.point = GeoPointFactory.createGeoPointForLatLong(location);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);		
		if (location == null) {
			return;
		}
		
        final Point screenPoint = new Point();
        mapView.getProjection().toPixels(point, screenPoint);
        
        final int radius = metersToRadius(location.getAccuracy(), mapView, location.getLatitude());	// TODO no accuracy use case        
		canvas.drawCircle(screenPoint.x, screenPoint.y, radius, paint);
	}
	
	private int metersToRadius(float meters, MapView map, double latitude) {
	    return (int) (map.getProjection().metersToEquatorPixels(meters) * (1/ Math.cos(Math.toRadians(latitude))));         
	}
	
}
