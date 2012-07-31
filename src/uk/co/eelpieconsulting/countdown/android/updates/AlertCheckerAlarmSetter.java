package uk.co.eelpieconsulting.countdown.android.updates;

import java.util.Calendar;
import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlertCheckerAlarmSetter {

	private static final long TEN_MINUTES = 10 * 60 * 1000;
	
	private static final String TAG = "AlertCheckerAlarmSetter";

	public void setSyncAlarm(Context context, long interval) {
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		final PendingIntent pi = makeIntent(context);

		final long timeInMillis = getNextSyncTime(interval);
		Log.i(TAG, "Setting next sync alarm for: " + new Date(timeInMillis).toLocaleString());
		alarmManager.setRepeating(AlarmManager.RTC, timeInMillis, TEN_MINUTES, pi);
	}
	
	public void setSyncAlarm(Context context) {
		setSyncAlarm(context, TEN_MINUTES);
	}
	
	private PendingIntent makeIntent(Context context) {
		Intent i=new Intent(context, AlertCheckerAlarmReceiver.class);
		PendingIntent pi= PendingIntent.getBroadcast(context, 0, i, 0);
		return pi;
	}
	
	private long getNextSyncTime(long interval) {
		Calendar time = Calendar.getInstance();
		long timeInMillis = time.getTimeInMillis() + interval;
		return timeInMillis;
	}
	
}
