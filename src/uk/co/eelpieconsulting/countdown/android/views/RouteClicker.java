package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.busroutes.model.Route;
import uk.co.eelpieconsulting.countdown.android.RouteTabActivity;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class RouteClicker implements OnClickListener {

	private final Activity activity;
	private final Route route;

	public RouteClicker(Activity activity, Route route) {
		this.activity = activity;
		this.route = route;
	}

	public void onClick(View view) {
		Intent intent = new Intent(view.getContext(), RouteTabActivity.class);
		intent.putExtra("route", route);
		activity.startActivity(intent);
	}

}
