package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.RouteActivity;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.view.View;
import android.view.View.OnClickListener;

public class RouteClicker implements OnClickListener {

	private final Activity activity;
	private final Route route;
	private final Stop stop;
	private final Location location;

	public RouteClicker(Activity activity, Route route, Stop stop, Location location) {
		this.activity = activity;
		this.route = route;
		this.stop = stop;
		this.location = location;
	}

	public void onClick(View view) {
		Intent intent = new Intent(view.getContext(), RouteActivity.class);
		intent.putExtra("route", route);		
		intent.putExtra("stop", stop);
		intent.putExtra("location", location);
		activity.startActivity(intent);
	}

}
