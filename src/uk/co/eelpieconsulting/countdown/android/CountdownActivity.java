package uk.co.eelpieconsulting.countdown.android;

import uk.co.eelpieconsulting.countdown.api.CountdownApi;
import uk.co.eelpieconsulting.countdown.model.StopBoard;
import uk.co.eelpieconsulting.countdown.parsers.StopBoardParser;
import uk.co.eelpieconsulting.countdown.urls.CountdownApiUrlBuilder;
import uk.co.eelpieconsulting.countdown.util.HttpFetcher;

import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.daos.FavouriteStopsDAO;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CountdownActivity extends Activity {

	private CountdownApi api;
	private FavouriteStopsDAO favouriteStopsDAO;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        CountdownApiUrlBuilder countdownApiUrlBuilder = new CountdownApiUrlBuilder("http://countdown.tfl.gov.uk");
        HttpFetcher httpFetcher = new HttpFetcher();
        StopBoardParser stopBoardParser = new StopBoardParser();
        api = new CountdownApi(countdownApiUrlBuilder, httpFetcher, stopBoardParser);
        favouriteStopsDAO = new FavouriteStopsDAO();
    }
    
	@Override
	protected void onResume() {
		super.onResume();
		final StopBoard stopboard = loadArrivals();
		final TextView arrivalsTextView = (TextView) findViewById(R.id.arrivals);
		arrivalsTextView.setText(stopboard.getArrivals().toString());        
	}
	
	private StopBoard loadArrivals() {
		return api.getStopBoard(favouriteStopsDAO.getFavouriteStop());
	}
	
}