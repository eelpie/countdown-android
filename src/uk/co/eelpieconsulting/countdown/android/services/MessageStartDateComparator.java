package uk.co.eelpieconsulting.countdown.android.services;

import java.util.Comparator;

import uk.co.eelpieconsulting.busroutes.model.Message;

public class MessageStartDateComparator implements Comparator<Message> {
		
	public MessageStartDateComparator() {
	}
	
	public int compare(Message lhs, Message rhs) {
		if (lhs.getStartDate() == rhs.getStartDate()) {
			return 0;
		}
		if (lhs.getStartDate() < rhs.getStartDate() ) {
			return 1;
		}
		return -1;
	}
	
}
