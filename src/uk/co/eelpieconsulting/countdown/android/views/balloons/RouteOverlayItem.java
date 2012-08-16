package uk.co.eelpieconsulting.countdown.android.views.balloons;

import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.views.maps.GeoPointFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class RouteOverlayItem extends Overlay {

	private final List<Stop> routeStops;
	private final Paint paint;
		
	public RouteOverlayItem(List<Stop> routeStops) {
		this.routeStops = routeStops;
		paint = new Paint();
		paint.setARGB(20, 0, 0, 240);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);		
		paint.setStrokeWidth(5);
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		
		Stop from = routeStops.get(0);
		for (int i = 1; i < routeStops.size(); i++) {
			final Stop to = routeStops.get(i);
			
			final Point fromPoint = new Point();
			mapView.getProjection().toPixels(GeoPointFactory.createGeoPointForLatLong(from.getLatitude(), from.getLongitude()), fromPoint);
			
			final Point toPoint = new Point();
			mapView.getProjection().toPixels(GeoPointFactory.createGeoPointForLatLong(to.getLatitude(), to.getLongitude()), toPoint);

			final Path path = new Path();
			path.moveTo(toPoint.x, toPoint.y);
			path.lineTo(fromPoint.x, fromPoint.y);			

			if (mapView.getZoomLevel() <= 14) {
				paint.setARGB(255, 0, 0, 240);
			} else {
				paint.setARGB(20, 0, 0, 240);
			}
						
			canvas.drawPath(path, paint);
			
			from = to;
		}
		
	}

}
