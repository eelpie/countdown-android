package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class FavouritesActivity extends Activity {

	private FavouriteStopsDAO favouriteStopsDAO;
	private TextView arrivalsTextView;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);        
        favouriteStopsDAO = new FavouriteStopsDAO();        
        arrivalsTextView = (TextView) findViewById(R.id.arrivals);
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		arrivalsTextView.setText("Loading favourite stops");
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
		int favouriteStop = favouriteStopsDAO.getFavouriteStop();
		arrivalsTextView.setText(Integer.toString(favouriteStop));
	}
	
}
