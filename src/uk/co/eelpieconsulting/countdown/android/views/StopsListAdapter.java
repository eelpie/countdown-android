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
	
	public StopsListAdapter(Context context, int viewResourceId, Activity activity, Location location) {
		super(context, viewResourceId);
		this.context = context;
		this.activity = activity;
		this.location = location;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {	
		TextView view = (TextView) convertView;
		if (view == null) {
			view = new TextView(context);
		}
		
		final Stop stop = getItem(position);
		view.setText(StopDescriptionService.makeStopDescription(stop, location) + "\n\n");
		view.setOnClickListener(new StopClicker(activity, stop));
		return view;		
	}
	
}