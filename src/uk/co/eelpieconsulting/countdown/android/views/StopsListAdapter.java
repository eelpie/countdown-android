package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StopsListAdapter extends ArrayAdapter<Stop> {

	private final Context context;
	private final Activity activity;
	private final Location location;
	private final Stop nearestStop;
	private final boolean showRouteNumbers;
	
	public StopsListAdapter(Context context, int viewResourceId, Activity activity, Location location, Stop nearestStop, boolean showRouteNumbers) {
		super(context, viewResourceId);
		this.context = context;
		this.activity = activity;
		this.location = location;
		this.nearestStop = nearestStop;
		this.showRouteNumbers = showRouteNumbers;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		TextView view = (TextView) convertView;
		if (view == null) {
			view = new TextView(context);
		}
		
		final Stop stop = getItem(position);
		String stopDescription = StopDescriptionService.makeStopDescription(stop, location, true) + "\n\n";
		if (stop.equals(nearestStop)) {
			stopDescription = "Nearest stop" + "\n" + stopDescription;
		}
		
		String stopContentDescription = StopDescriptionService.makeStopDescription(stop, location, showRouteNumbers) + "\n\n";
		if (stop.equals(nearestStop)) {
			stopContentDescription = "Nearest stop" + "\n" + stopContentDescription;
		}
		
		view.setText(stopDescription);
		view.setContentDescription(stopContentDescription);
		view.setOnClickListener(new StopClicker(activity, stop));
		return view;		
	}
	
}