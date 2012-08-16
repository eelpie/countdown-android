package uk.co.eelpieconsulting.countdown.android.views.balloons;

import java.util.ArrayList;

import uk.co.eelpieconsulting.countdown.android.StopActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class StopsItemizedOverlay extends BalloonItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> m_overlays = new ArrayList<OverlayItem>();
	private Context context;
	private Drawable drawable;
	
	public StopsItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		context = mapView.getContext();
		this.drawable = defaultMarker;
	}

	public void addOverlay(OverlayItem overlay) {
	    m_overlays.add(overlay);
	    populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return m_overlays.get(i);
	}

	@Override
	public int size() {
		return m_overlays.size();
	}

	@Override
	protected boolean onBalloonTap(int index, OverlayItem item) {	
		Intent intent = new Intent(context, StopActivity.class);
		intent.putExtra("stop", ((StopOverlayItem) item).getStop());
		context.startActivity(intent);		
		return true;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {		
		if (mapView.getZoomLevel() > 14) {
			drawable.setAlpha(255);		
		} else {
			drawable.setAlpha(0);			
		}
		super.draw(canvas, mapView, shadow);
	}
	
}
