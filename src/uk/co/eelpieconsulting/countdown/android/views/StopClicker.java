package uk.co.eelpieconsulting.countdown.android.views;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.CountdownActivity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class StopClicker implements OnClickListener {
	
	private Context context;
	private Stop stop;

	public StopClicker(Context context, Stop stop) {
		this.context = context;
		this.stop = stop;
	}

	public void onClick(View view) {
		Intent intent = getIntentForContentsType(view.getContext(), stop);
		intent.putExtra("stop", stop);
		context.startActivity(intent);
	}

	private Intent getIntentForContentsType(Context context, Stop stop) {
		return new Intent(context, CountdownActivity.class);
	}
}