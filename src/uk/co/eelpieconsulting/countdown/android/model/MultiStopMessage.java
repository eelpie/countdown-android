package uk.co.eelpieconsulting.countdown.android.model;

import uk.co.eelpieconsulting.busroutes.model.Message;
import uk.co.eelpieconsulting.busroutes.model.Stop;

import java.util.List;

public class MultiStopMessage extends Message {

	private static final long serialVersionUID = 1L;
	
	private final List<Stop> stops;

	public MultiStopMessage(Message message, List<Stop> stops) {
		super(message.getId(), message.getStopId(), message.getMessage(), message.getPriority(), message.getStartDate(), message.getEndDate());
		this.stops = stops;
	}

	public List<Stop> getStops() {
		return stops;
	}
	
}
