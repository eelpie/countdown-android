package uk.co.eelpieconsulting.countdown.android.views;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.Message;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.services.StopNameComparator;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class MessageDescriptionService {

	private static final String MESSAGE_START_DATE_FORMAT = "dd MMMM, kk:mm a";
	
	private static DateFormat dateFormatter = new SimpleDateFormat(MESSAGE_START_DATE_FORMAT);

	private static StopNameComparator stopNameComparator = new StopNameComparator();
	
	public static TextView makeStopDescription(Message message, Context context) {
		return makeMessageView(message, context, new ArrayList<Stop>());
	}
	
	@Deprecated
	public static TextView makeMessageView(Message message, Context context, List<Stop> stops) {		
		TextView messageView = new TextView(context);
		messageView.setText(makeMessageDescription(message, stops) + "\n\n\n");
		messageView.setVisibility(View.VISIBLE);
		return messageView;		
	}
	
	public static String makeMessageDescription(Message message, List<Stop> stops) {
		final StringBuilder output = new StringBuilder();
		output.append(dateFormatter.format(new Date(message.getStartDate())));
		output.append(": ");
		output.append(message.getMessage());
		output.append("\n");
		appendCommaSeperatedStopNames(stops, output);
		return output.toString();
	}
	
	private static void appendCommaSeperatedStopNames(List<Stop> stops, final StringBuilder output) {
		Collections.sort(stops, stopNameComparator);
		
		int i = 1;		
		for (Stop stop : stops) {
			output.append(StopDescriptionService.makeStopTitle(stop));
			if (i < stops.size()) {
				output.append(", ");
			}
			i++;
		}
	}
	
}
