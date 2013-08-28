package uk.co.eelpieconsulting.countdown.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class StartupActivity extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
		callFavouritesActivityWithStartupHint();			
    }

	private void callFavouritesActivityWithStartupHint() {
		final Intent intent = new Intent(this, FavouritesActivity.class);
		intent.putExtra("startup", true);
		this.startActivity(intent);
	}
	
}
