package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.CountdownActivity;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class StopClicker implements OnClickListener {
	
	private Activity activity;
	private Stop stop;

	public StopClicker(Activity activity, Stop stop) {
		this.activity = activity;
		this.stop = stop;
	}

	public void onClick(View view) {
		Intent intent = getIntentForContentsType(view.getContext(), stop);
		intent.putExtra("stop", stop);
		activity.startActivity(intent);
	}

	private Intent getIntentForContentsType(Context context, Stop stop) {
		return new Intent(activity, CountdownActivity.class);
	}
}