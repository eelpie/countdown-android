package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.countdown.android.api.CountdownApiFactory;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import uk.co.eelpieconsulting.countdown.exceptions.HttpFetchException;
import uk.co.eelpieconsulting.countdown.exceptions.ParsingException;
import uk.co.eelpieconsulting.countdown.model.Stop;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SearchActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.stops);

	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      try {
			List<Stop> matches = CountdownApiFactory.getApi().findStopById(Integer.parseInt(query));
			
			final LinearLayout stopsList = (LinearLayout) findViewById(R.id.stopsList);
			stopsList.removeAllViews();
						
			for (Stop stop : matches) {
				final TextView stopTextView = new TextView(this.getApplicationContext());

				String stopDescription = StopDescriptionService.makeStopDescription(stop);
				stopDescription = stopDescription + "\n\n";
				
				stopTextView.setText(stopDescription);
				stopTextView.setOnClickListener(new StopClicker(stop));
				stopsList.addView(stopTextView);
			}
			
		} catch (HttpFetchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	      
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, R.string.favourites);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			this.startActivity(new Intent(this, FavouritesActivity.class));
			return true;
		}
		return false;
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
