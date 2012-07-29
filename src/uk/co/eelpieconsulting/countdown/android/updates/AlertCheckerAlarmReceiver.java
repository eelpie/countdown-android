package uk.co.eelpieconsulting.countdown.android.updates;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import uk.co.eelpieconsulting.buses.client.BusesClient;
import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.AlertsActivity;
import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
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
			final BusesClient api = ApiFactory.getApi();
			try {
				List<MultiStopMessage> messages = api.getMultipleStopMessages(stopIds);
				sendNotification(context, messages);				
			} catch (HttpFetchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		AlertCheckerAlarmSetter alarmSetter = new AlertCheckerAlarmSetter();
		alarmSetter.setDailyContentUpdateAlarm(context);
	}

	private void sendNotification(Context context, List<MultiStopMessage> messages) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		final CharSequence tickerText = "New alerts";
		Notification notification = new Notification(R.drawable.notification_icon, tickerText, new Date().getTime());
		
		CharSequence contentTitle = messages.size() + " new alerts";
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
