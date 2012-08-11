package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.busroutes.model.Route;
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

public class RoutesListAdapter extends ArrayAdapter<Route> {

	private final Context context;
	private final Activity activity;
	private final Location location;
	private final Stop selectedStop;
	
	public RoutesListAdapter(Context context, int viewResourceId, Activity activity, Location location, Stop selectedStop) {
		super(context, viewResourceId);
		this.context = context;
		this.activity = activity;
		this.location = location;
		this.selectedStop = selectedStop;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		View view = convertView;
		if (view == null) {
			view = inflateRouteView();
		}
		
		final Route route = getItem(position);
		final TextView routeTextView = (TextView) view.findViewById(R.id.routeName);
		routeTextView.setText(route.getRoute());			
		
		final TextView bodyTextView = (TextView) view.findViewById(R.id.body);
		bodyTextView.setText(context.getString(R.string.towards) + " " + route.getTowards());
		
		view.setOnClickListener(new RouteClicker(activity, route, selectedStop, location));
		return view;		
	}
	
	private View inflateRouteView() {
		return LayoutInflater.from(context).inflate(R.layout.arrival, null);
	}
	
}
