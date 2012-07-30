package uk.co.eelpieconsulting.countdown.android.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.util.Log;

import uk.co.eelpieconsulting.buses.client.BusesClient;
import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.countdown.android.daos.SeenMessagesDAO;

public class MessageService {
	
	private static final String TAG = "MessageService";
	
	private final BusesClient api;
	private final SeenMessagesDAO seenMessagesDAO;
	
	public MessageService(BusesClient api, SeenMessagesDAO seenMessagesDAO) {
		this.api = api;
		this.seenMessagesDAO = seenMessagesDAO;
	}
	
	public List<MultiStopMessage> getStopMessages(int[] stopIds) {
		try {
			return api.getMultipleStopMessages(stopIds);
		} catch (HttpFetchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<MultiStopMessage>();
	}

	public List<MultiStopMessage> getNewMessagesFor(int[] stopIds) {
		List<MultiStopMessage> stopMessages = getStopMessages(stopIds);
		Set<String> seenMessages = seenMessagesDAO.getSeenMessages();
		List<MultiStopMessage> unseenMessages = new ArrayList<MultiStopMessage>();
		for (MultiStopMessage multiStopMessage : stopMessages) {
			if (!seenMessages.contains(multiStopMessage.getId())) {
				unseenMessages.add(multiStopMessage);
			} else {
				Log.i(TAG, "Disgarding previously seen message: " + multiStopMessage.getMessage());
			}
		}
		return unseenMessages;
	}
	
	public void markAsSeen(List<MultiStopMessage> messages) {
		Set<String> seenMessages = seenMessagesDAO.getSeenMessages();		
		for (MultiStopMessage message : messages) {
			seenMessages.add(message.getId());
		}
		seenMessagesDAO.setSeenMessages(seenMessages);
	}
	
}
