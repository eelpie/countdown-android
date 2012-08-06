package uk.co.eelpieconsulting.countdown.android.widgets;

import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.CountdownActivity;
import uk.co.eelpieconsulting.countdown.android.R;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class ArrivalsWidget extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		refresh(context, appWidgetIds);
	}
	
	private void refresh(Context context, int[] appWidgetIds) {		
		RemoteViews widgetView = new RemoteViews(context.getPackageName(), R.layout.stoprow);
		
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		if (manager != null) {
			manager.updateAppWidget(appWidgetIds, widgetView);
		}
	}
	
	protected PendingIntent createStopIntent(Context context, Stop stop) {
		Intent intent = new Intent(context, CountdownActivity.class);		
		intent.setData(Uri.withAppendedPath(Uri.parse("content://stop/id"), String.valueOf(stop.getId())));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		return pendingIntent;
	}
	
}
