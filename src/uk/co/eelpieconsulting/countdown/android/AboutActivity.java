package uk.co.eelpieconsulting.countdown.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends Activity {
	
	private static final String TAG = "AlertsActivity";
		
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stops);
        
        setTitle(R.string.about);
        TextView status = (TextView) findViewById(R.id.status);
        status.setText(R.string.about_text);
        status.setVisibility(View.VISIBLE);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.alerts_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
		Log.d(TAG, "Clicked: " + item.getItemId());		
		switch (item.getItemId()) {
			case R.id.favourites:
				this.startActivity(new Intent(this, FavouritesActivity.class));
				return true;
				
			case R.id.nearby:
				this.startActivity(new Intent(this, NearbyTabActivity.class));
				return true;
				
			case R.id.search:
				this.startActivity(new Intent(this, SearchActivity.class));
				return true;
			}
			return false;
	}
	
}