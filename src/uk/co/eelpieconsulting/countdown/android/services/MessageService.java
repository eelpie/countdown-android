package uk.co.eelpieconsulting.countdown.android.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.daos.SeenMessagesDAO;
import uk.co.eelpieconsulting.countdown.android.services.caching.MessageCache;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import android.content.Context;
import android.util.Log;

public class MessageService {
	
	private static final String TAG = "MessageService";
	
	private final BusesClientService api;
	private final MessageCache messageCache;
	private final SeenMessagesDAO seenMessagesDAO;
	private final Context context;

	public MessageService(BusesClientService api, MessageCache messageCache, SeenMessagesDAO seenMessagesDAO, Context context) {
		this.api = api;
		this.messageCache = messageCache;
		this.seenMessagesDAO = seenMessagesDAO;
		this.context = context;
	}
	
	public List<MultiStopMessage> getStopMessages(int[] stopIds) throws ContentNotAvailableException {
		try {			
			final List<MultiStopMessage> cachedMessages = messageCache.getStopMessages(stopIds);
			final boolean cachedMessagesAreAvailable = cachedMessages != null;
			if (cachedMessagesAreAvailable) {
				Log.i(TAG, "Returning messages from cache");
				return cachedMessages;
			}
			
			final List<MultiStopMessage> messages = api.getMultipleStopMessages(stopIds);
			messageCache.cache(stopIds, messages);			
			return messages;
			
		} catch (NetworkNotAvailableException e) {
			throw new ContentNotAvailableException(context.getString(R.string.no_network_available));
		} catch (HttpFetchException e) {
			throw new ContentNotAvailableException(e);		
		} catch (ParsingException e) {
			throw new ContentNotAvailableException(e);
		}
	}
	
	public List<MultiStopMessage> getStopMessages(int stopId) throws ContentNotAvailableException {
		return getStopMessages(new int[] {stopId});
	}

	public List<MultiStopMessage> getNewMessagesFor(int[] stopIds) throws ContentNotAvailableException {
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
