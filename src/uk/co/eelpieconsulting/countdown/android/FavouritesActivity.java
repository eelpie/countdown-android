package uk.co.eelpieconsulting.countdown.android;

import java.util.Set;

import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import uk.co.eelpieconsulting.countdown.model.Stop;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FavouritesActivity extends Activity {

	private FavouriteStopsDAO favouriteStopsDAO;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);        
        favouriteStopsDAO = FavouriteStopsDAO.get(this.getApplicationContext());
        
        TextView status = (TextView) findViewById(R.id.status);
        status.setVisibility(View.GONE);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		showFavourites();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, "Arrivals");
		menu.add(0, 2, 0, "Find stops");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			this.startActivity(new Intent(this, CountdownActivity.class));
			return true;

		case 2:
			this.startActivity(new Intent(this, StopsActivity.class));
			return true;
		}
		return false;
	}
	
	private void showFavourites() {
		showStops(favouriteStopsDAO.getFavouriteStops());
	}

	private void showStops(Set<Stop> stops) {
		final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
		stopsList.removeAllViews();
		for (Stop stop : stops) {
			stopsList.addView(makeStopView(stop));
		}
	}

	private TextView makeStopView(Stop stop) {
		final TextView stopTextView = new TextView(this.getApplicationContext());
		stopTextView.setText(StopDescriptionService.makeStopDescription(stop) + "\n\n");
		stopTextView.setOnClickListener(new StopClicker(stop));
		return stopTextView;
	}
	
	private class StopClicker implements OnClickListener {
		private Stop stop;

		public StopClicker(Stop stop) {
			this.stop = stop;
		}

		public void onClick(View view) {
			Intent intent = getIntentForContentsType(view.getContext(), stop);
			intent.putExtra("stop", stop);
			startActivity(intent);
		}

		private Intent getIntentForContentsType(Context context, Stop stop) {
			return new Intent(context, CountdownActivity.class);
		}
	}
	
}