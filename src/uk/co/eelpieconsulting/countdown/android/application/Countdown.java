package uk.co.eelpieconsulting.countdown.android.application;

import uk.co.eelpieconsulting.countdown.android.updates.AlertCheckerAlarmSetter;
import android.app.Application;
import android.util.Log;

public class Countdown extends Application {
	
	private static final String TAG = "Countdown";

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Setting sync alarm");
		AlertCheckerAlarmSetter alarmSetter = new AlertCheckerAlarmSetter();
		alarmSetter.setDailyContentUpdateAlarm(getApplicationContext());
	}

}
