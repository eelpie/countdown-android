package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class StopsListAdapter extends ArrayAdapter<Stop> {

	private final Context context;
	private Activity activity;
	
	public StopsListAdapter(Context context, int viewResourceId, Activity activity) {
		super(context, viewResourceId);
		this.context = context;
		this.activity = activity;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		return StopDescriptionService.makeStopView(getItem(position), context, activity);
	}
	
}