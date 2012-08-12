package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessagesListAdapter extends ArrayAdapter<MultiStopMessage> {

	private final Context context;
	
	public MessagesListAdapter(Context context, int viewResourceId) {
		super(context, viewResourceId);
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {		
		View view = convertView;
		if (view == null) {
			view = inflateRouteView();
		}
		
		final MultiStopMessage message = getItem(position);		
		((TextView) view).setText(MessageDescriptionService.makeMessageDescription(message, message.getStops()));
		return view;
	}
	
	private View inflateRouteView() {
		return new TextView(context);
	}
	
}