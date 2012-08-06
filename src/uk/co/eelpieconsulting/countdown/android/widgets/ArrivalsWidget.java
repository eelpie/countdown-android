package uk.co.eelpieconsulting.countdown.android.widgets;

import uk.co.eelpieconsulting.buses.client.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.buses.client.model.Arrival;
import uk.co.eelpieconsulting.buses.client.model.StopBoard;
import uk.co.eelpieconsulting.busroutes.model.Stop;
import uk.co.eelpieconsulting.countdown.android.CountdownActivity;
import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.services.location.LocationService;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.RemoteViews;

public class ArrivalsWidget extends AppWidgetProvider {
	
	private static final String TAG = "ArrivalsWidget";
	
	public static String WIDGET_CLICK = "ArrivalsWidgetClick";
		
	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
		super.onEnabled(context);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {		
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		refresh(context, appWidgetIds);
	}
	
	private void refresh(Context context, int[] appWidgetIds) {		
		final RemoteViews remoteWidgetView = new RemoteViews(context.getPackageName(), R.layout.widget);		
		Intent intent = new Intent(context, ArrivalsWidget.class);
		intent.setAction(WIDGET_CLICK);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		remoteWidgetView.setOnClickPendingIntent(R.id.WidgetItemLayout, pendingIntent);
		
		setClosestStopTitle(context, remoteWidgetView, getClosestFavouriteStop(context));
		remoteWidgetView.setTextViewText(R.id.arrivals, "");
		
		AppWidgetManager manager = AppWidgetManager.getInstance(context);	
		if (manager != null) {
			manager.updateAppWidget(appWidgetIds, remoteWidgetView);
		}
	}

	private void setClosestStopTitle(Context context, final RemoteViews remoteWidgetView, Stop closestFavouriteStop) {
		if (closestFavouriteStop != null) {
			remoteWidgetView.setTextViewText(R.id.title, StopDescriptionService.makeStopTitle(closestFavouriteStop));
		} else {
			remoteWidgetView.setTextViewText(R.id.title, context.getString(R.string.no_favourites_warning));			
		}
	}

	private Stop getClosestFavouriteStop(Context context) {
		FavouriteStopsDAO favouriteStopsDAO = new FavouriteStopsDAO(context);
		if (favouriteStopsDAO.hasFavourites()) {
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			return favouriteStopsDAO.getClosestFavouriteStopTo(LocationService.getBestLastKnownLocation(locationManager));
		}
		return null;
	}
		
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);		
		Log.d(TAG, "Received broadcast: " + intent.getAction());
		if (intent.getAction().equals(WIDGET_CLICK)) {
			final RemoteViews remoteWidgetView = new RemoteViews(context.getPackageName(), R.layout.widget);
			
			final Stop closestFavouriteStop = getClosestFavouriteStop(context);
			setClosestStopTitle(context, remoteWidgetView, closestFavouriteStop);
			
			if (closestFavouriteStop != null) {
				Log.d(TAG, "Loading arrivals for: " + closestFavouriteStop.getName());
				FetchArrivalsTask fetchArrivalsTask = new FetchArrivalsTask(ApiFactory.getApi(context), context);
				fetchArrivalsTask.execute(closestFavouriteStop.getId());
					
			}
			
			final AppWidgetManager manager = AppWidgetManager.getInstance(context);
			if (manager != null) {
				final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, ArrivalsWidget.class));
				manager.updateAppWidget(appWidgetIds, remoteWidgetView);
			}
		}
	}
	
	private void renderStopboard(StopBoard stopBoard, Context context) {
		final RemoteViews remoteWidgetView = new RemoteViews(context.getPackageName(), R.layout.widget);		
		final StringBuilder arrivalsOutput = new StringBuilder();
		if (stopBoard != null) {
			for (Arrival arrival : stopBoard.getArrivals()) {
				arrivalsOutput.append(arrival.getRoute().getRoute() + " - " + StopDescriptionService.secondsToMinutes(arrival.getEstimatedWait(), context));
				arrivalsOutput.append("\n");
			}
		}
		remoteWidgetView.setTextViewText(R.id.arrivals, arrivalsOutput);
		
		final AppWidgetManager manager = AppWidgetManager.getInstance(context);
		if (manager != null) {
			final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, ArrivalsWidget.class));
			manager.updateAppWidget(appWidgetIds, remoteWidgetView);
		}
	}
	
	protected PendingIntent createStopIntent(Context context, Stop stop) {
		Intent intent = new Intent(context, CountdownActivity.class);		
		intent.setData(Uri.withAppendedPath(Uri.parse("content://stop/id"), String.valueOf(stop.getId())));
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		return pendingIntent;
	}
	
	private class FetchArrivalsTask extends AsyncTask<Integer, Integer, StopBoard> {

		private BusesClientService api;
		private Context context;

		public FetchArrivalsTask(BusesClientService api, Context context) {
			super();
			this.api = api;
			this.context = context;
		}

		@Override
		protected StopBoard doInBackground(Integer... params) {
			final int stopId = params[0];
			try {
				return api.getStopBoard(stopId);
			} catch (HttpFetchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParsingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NetworkNotAvailableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(StopBoard stopboard) {
			renderStopboard(stopboard, context);
		}
		
	}
	
}
