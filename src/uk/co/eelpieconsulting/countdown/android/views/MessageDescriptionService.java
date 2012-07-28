package uk.co.eelpieconsulting.countdown.android.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Message;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.model.MultiStopMessage;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class MessageDescriptionService {


	public static TextView makeStopDescription(Message message, Context context) {
		return makeMessageView(message, context, new ArrayList<Stop>());
	}

	public static TextView makeMessageView(MultiStopMessage displayMessage, Context applicationContext) {
		return makeMessageView(displayMessage, applicationContext, displayMessage.getStops());			
	}
	
	private static TextView makeMessageView(Message message, Context context, List<Stop> stops) {
		final StringBuilder output = new StringBuilder(message.getMessage() + "\n");
		output.append(new Date(message.getStartDate()));
		for (Stop stop : stops) {
			output.append(stop.getName() + ", ");
		}
		TextView messageView = new TextView(context);
		messageView.setText(output.toString() + "\n\n\n");
		messageView.setVisibility(View.VISIBLE);
		return messageView;		
	}
	
}
