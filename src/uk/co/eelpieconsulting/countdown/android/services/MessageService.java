package uk.co.eelpieconsulting.countdown.android.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import uk.co.eelpieconsulting.buses.client.CountdownApi;
import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.Message;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.model.MultiStopMessage;

public class MessageService {
	
	private final CountdownApi api;

	public MessageService(CountdownApi api) {
		this.api = api;		
	}
	
	public List<MultiStopMessage> getMessages(Set<Stop> stops) throws HttpFetchException, ParsingException {		
		int[] stopIds = new int[stops.size()];
		Iterator<Stop> iterator = stops.iterator();
		for (int i = 0; i < stops.size(); i++) {
			stopIds[i] = iterator.next().getId();
		}				
		
		Map<Stop, List<Message>> stopsMessageMap = new HashMap<Stop, List<Message>>();
		List<Message> messages = api.getMultipleStopMessages(stopIds);
		Map<Integer, Stop> stopsById = new HashMap<Integer, Stop>();
		for (Stop stop : stops) {
			stopsById.put(stop.getId(), stop);
			stopsMessageMap.put(stop, new ArrayList<Message>());
		}
		
		for (Message message : messages) {
			final Stop messageStop = stopsById.get(message.getStopId());
			List<Message> list = stopsMessageMap.get(messageStop);
			list.add(message);
			stopsMessageMap.put(messageStop, list);
		}				
		
		return filterUniqueMessages(stopsMessageMap);
	}

	private List<MultiStopMessage> filterUniqueMessages(Map<Stop, List<Message>> messagesMap) {
		Map<String, Message> uniqueMessages = new HashMap<String, Message>();			
		final Map<String, List<Stop>> messageStops = new HashMap<String, List<Stop>>();
		for (Stop stop : messagesMap.keySet()) {
			for (Message message : messagesMap.get(stop)) {
				final String hash = getMessageHash(message);
				final boolean isCurrent = message.getStartDate() < (System.currentTimeMillis()) && message.getEndDate() > (System.currentTimeMillis());
				if (isCurrent) {
					uniqueMessages.put(hash, message);
				}
				messageStops.put(hash, new ArrayList<Stop>());
			}
		}
		for (Stop stop : messagesMap.keySet()) {
			for (Message message : messagesMap.get(stop)) {
				final String hash = getMessageHash(message);
				List<Stop> list = messageStops.get(hash);
				list.add(stop);
				messageStops.put(hash, list);
			}
		}
	
		final List<MultiStopMessage> messagesToDisplay = new ArrayList<MultiStopMessage>();
		for (Message message : uniqueMessages.values()) {
			messagesToDisplay.add(new MultiStopMessage(message, messageStops.get(getMessageHash(message))));
		}
		
		Collections.sort(messagesToDisplay, new MessageStartDateComparator());
		return messagesToDisplay;
	}
	
	private String getMessageHash(Message message) {
		return message.getId().split("_")[0];
	}	

}
