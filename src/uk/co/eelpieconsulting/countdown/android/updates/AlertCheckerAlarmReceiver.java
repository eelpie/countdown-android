package uk.co.eelpieconsulting.countdown.android.updates;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.AlertsActivity;
import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.daos.SeenMessagesDAO;
import uk.co.eelpieconsulting.countdown.android.services.ContentNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.services.MessageService;
import uk.co.eelpieconsulting.countdown.android.services.caching.MessageCache;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

public class AlertCheckerAlarmReceiver extends BroadcastReceiver {

	private static final String TAG = "AlertCheckerAlarmReceiver";
	
	private FetchUnreadMessagesTask fetchUnreadMessagesTask;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Received alarm; checking for new alert messages");
		
		final MessageService messageService = new MessageService(ApiFactory.getApi(context), new MessageCache(context), new SeenMessagesDAO(context));
		final FavouriteStopsDAO favouriteStopsDAO = FavouriteStopsDAO.get(context);
		final Set<Stop> favouriteStops = favouriteStopsDAO.getFavouriteStops();
		
		fetchUnreadMessagesTask = new FetchUnreadMessagesTask(messageService, context);
		fetchUnreadMessagesTask.execute(favouriteStops);
		
		AlertCheckerAlarmSetter alarmSetter = new AlertCheckerAlarmSetter();
		alarmSetter.setSyncAlarm(context);
	}

	private void sendNotification(Context context, List<MultiStopMessage> messages) {
		if (messages == null) {
			Log.w(TAG, "Messages could not be fetched; skipping check");
			return;
		}
		
		if (messages.isEmpty()) {
			Log.i(TAG, "No new messages seen; not notifying");
			return;
		}
		
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		final CharSequence tickerText = messages.size() > 1 ? "New alerts" : "New alert";	// TODO can be moved to strings?
		Notification notification = new Notification(R.drawable.notification_icon, tickerText, new Date().getTime());
		
		CharSequence contentTitle = messages.size() + " " + (messages.size() > 1 ? "new alerts" : "new alert");
		CharSequence contentText =  messages.get(0).getMessage();
		
		Intent notificationIntent = new Intent(context, AlertsActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);		
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		notificationManager.notify(AlertsActivity.NOTIFICATION_ID, notification);
	}
	
	private int[] getIdsFrom(final Set<Stop> favouriteStops) {
		int[] stopIds = new int[favouriteStops.size()];
		
		Iterator<Stop> iterator = favouriteStops.iterator();
		for (int i = 0; i < favouriteStops.size(); i++) {
			stopIds[i] = iterator.next().getId();				
		}
		return stopIds;
	}
	
	private class FetchUnreadMessagesTask extends AsyncTask<Set<Stop>, Integer, List<MultiStopMessage>> {

		private final MessageService messageService;
		private final Context context;

		public FetchUnreadMessagesTask(MessageService messageService, Context context) {
			super();
			this.messageService = messageService;
			this.context = context;
		}

		@Override
		protected List<MultiStopMessage> doInBackground(Set<Stop>... params) {
			fetchUnreadMessagesTask = this;		
			final Set<Stop> favouriteStops = params[0];
			if (!favouriteStops.isEmpty()) {
				final int[] stopIds = getIdsFrom(favouriteStops);				
				List<MultiStopMessage> newMessages;
				try {
					return messageService.getNewMessagesFor(stopIds);
				} catch (ContentNotAvailableException e) {
					Log.w(TAG, "Could not get unread messages. Cause: " + e.getMessage());
					return null;
				}
			}						
			return new ArrayList<MultiStopMessage>();
		}
		
		@Override
		protected void onPostExecute(List<MultiStopMessage> messages) {
			sendNotification(context, messages);			
		}
		
	}
	
}
