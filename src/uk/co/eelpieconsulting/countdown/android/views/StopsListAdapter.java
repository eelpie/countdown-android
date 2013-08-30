package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.R;
import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
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
		View view = convertView;
		if (view == null) {
			view = inflateNewStopRowView();
		}
		
		final Stop stop = getItem(position);
		populateStopView(view, stop);
		return view;		
	}

	private void populateStopView(View view, final Stop stop) {
		final boolean isNearestStop = stop.equals(nearestStop);

		final String stopDescription = StopDescriptionService.makeStopDescription(stop, location, true) + "\n\n";		
		String stopContentDescription = StopDescriptionService.makeStopDescription(stop, location, showRouteNumbers) + "\n\n";
		if (isNearestStop) {
			stopContentDescription = "Nearest stop" + "\n" + stopContentDescription;
		}
		
		final TextView nearestStop = (TextView) view.findViewById(R.id.nearestStop);		
		nearestStop.setText("Nearest stop");
		if (isNearestStop) {
			nearestStop.setVisibility(View.VISIBLE);
		} else {
			nearestStop.setVisibility(View.GONE);
		}
		
		final TextView stopName = (TextView) view.findViewById(R.id.stopName);	
		stopName.setText(StopDescriptionService.makeStopTitle(stop));		
		
		final TextView body = (TextView) view.findViewById(R.id.body);		
		body.setText(stopDescription);
		
		view.setContentDescription(stopContentDescription);		
		view.setOnClickListener(new StopClicker(activity, stop));
	}

	private View inflateNewStopRowView() {
		final LayoutInflater mInflater = LayoutInflater.from(context);
		return mInflater.inflate(R.layout.stoprow, null);
	}
	
}