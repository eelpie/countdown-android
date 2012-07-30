package uk.co.eelpieconsulting.countdown.android.updates;

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
import uk.co.eelpieconsulting.countdown.android.services.MessageService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlertCheckerAlarmReceiver extends BroadcastReceiver {

	private static final String TAG = "AlertCheckerAlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Received alarm");
		
		final FavouriteStopsDAO favouriteStopsDAO = FavouriteStopsDAO.get(context);
				
		final Set<Stop> favouriteStops = favouriteStopsDAO.getFavouriteStops();
		if (!favouriteStops.isEmpty()) {
			final int[] stopIds = getIdsFrom(favouriteStops);			
			final MessageService messageService = new MessageService(ApiFactory.getApi(), new SeenMessagesDAO(context));
			
			final List<MultiStopMessage> newMessages = messageService.getNewMessagesFor(stopIds);
			if (!newMessages.isEmpty()) {
				sendNotification(context, newMessages);
			}
		}
	
		AlertCheckerAlarmSetter alarmSetter = new AlertCheckerAlarmSetter();
		alarmSetter.setDailyContentUpdateAlarm(context);
	}

	private void sendNotification(Context context, List<MultiStopMessage> messages) {
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
	
}
