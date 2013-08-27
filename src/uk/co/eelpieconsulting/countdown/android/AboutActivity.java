package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.buses.client.exceptions.ParsingException;
import uk.co.eelpieconsulting.buses.client.model.FileInformation;
import uk.co.eelpieconsulting.common.http.HttpFetchException;
import uk.co.eelpieconsulting.countdown.android.api.ApiFactory;
import uk.co.eelpieconsulting.countdown.android.api.BusesClientService;
import uk.co.eelpieconsulting.countdown.android.model.Article;
import uk.co.eelpieconsulting.countdown.android.services.AboutArticlesService;
import uk.co.eelpieconsulting.countdown.android.services.network.NetworkNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.views.StopDescriptionService;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AboutActivity extends Activity {
		
	private static final String TAG = "AboutActivity";

	private static final String ABOUT_ARTICLES_FEED_URL = "http://eelpieconsulting.co.uk/category/development/buses/feed/rss";
	
	private AboutArticlesService aboutArticlesService;
	
	private LinearLayout stopsList;

	private FetchSourceFileInformationTask fetchSourceFileInformationTask;
	private FetchArticlesTask fetchMessagesTask;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
	    getActionBar().setDisplayHomeAsUpEnabled(true);
	    
        setTitle(R.string.about);
        TextView status = (TextView) findViewById(R.id.status);
        status.setText(R.string.about_text);
        status.setVisibility(View.VISIBLE);
        
		stopsList = (LinearLayout) findViewById(R.id.stopsList);
		
        aboutArticlesService = new AboutArticlesService();
        
        fetchSourceFileInformationTask = new FetchSourceFileInformationTask(ApiFactory.getApi(getApplicationContext()));
        fetchSourceFileInformationTask.execute();
        
    	//fetchMessagesTask = new FetchArticlesTask(aboutArticlesService);
		//fetchMessagesTask.execute(ABOUT_ARTICLES_FEED_URL);
    }
	
	@Override
	protected void onPause() {
		super.onPause();
		if (fetchSourceFileInformationTask != null && fetchSourceFileInformationTask.getStatus().equals(Status.RUNNING)) {
			fetchSourceFileInformationTask.cancel(true);
		}
		if (fetchMessagesTask != null && fetchMessagesTask.getStatus().equals(Status.RUNNING)) {
			fetchMessagesTask.cancel(true);
		}	
	}
	
	private void renderSourceFileInformation(List<FileInformation> sourceFileInformation) {
		if (sourceFileInformation == null || sourceFileInformation.isEmpty()) {
			return;
		}
		
		final FileInformation sourceInformation = sourceFileInformation.get(0);
		final TextView sourcesTextView = (TextView) findViewById(R.id.sources);
		sourcesTextView.setText("Stops data file information: " + sourceInformation.getName() + 
				" imported on " + sourceInformation.getDate() + 
				" (md5 checksum: " + sourceInformation.getMd5() + ")");
		sourcesTextView.setVisibility(View.VISIBLE);
	}
	
	private void renderArticles(final List<Article> articles) {
		if (articles == null) {
			return;
		}
		for (Article article : articles) {
			stopsList.addView(StopDescriptionService.makeArticleView(article, getApplicationContext()));
		}
	}
	
	private class FetchSourceFileInformationTask extends AsyncTask<String, Integer, List<FileInformation>> {

		private BusesClientService busClientService;

		public FetchSourceFileInformationTask(BusesClientService busClientService) {
			super();
			this.busClientService = busClientService;
		}

		@Override
		protected List<FileInformation> doInBackground(String... params) {
			try {
				Log.i(TAG, "Fetching source file information");
				return busClientService.getSourceFileInformation();
				
			} catch (HttpFetchException e) {
				Log.w(TAG, "Could not load source file information: " + e.getMessage());
				e.printStackTrace();
				e.getCause().printStackTrace();
				
			} catch (ParsingException e) {
				Log.w(TAG, "Could not load source file information: " + e.getMessage());
				e.printStackTrace();

			} catch (NetworkNotAvailableException e) {
				Log.w(TAG, "Could not load source file information: " + e.getMessage());
				e.printStackTrace();

			}
			return null;
		}

		@Override
		protected void onPostExecute(List<FileInformation> fileInformation) {
			renderSourceFileInformation(fileInformation);
		}
	}
		
	private class FetchArticlesTask extends AsyncTask<String, Integer, List<Article>> {

		private AboutArticlesService aboutArticlesService;

		public FetchArticlesTask(AboutArticlesService aboutArticlesService) {
			super();
			this.aboutArticlesService = aboutArticlesService;
		}

		@Override
		protected List<Article> doInBackground(String... params) {
			final String feedUrl = params[0];
			try {
				return aboutArticlesService.getArticles(getApplicationContext(), feedUrl);
			} catch (ArticlesNotAvailableException e) {
				Log.w(TAG, "Articles could not be fetched from feed url: " + feedUrl);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Article> articles) {
			renderArticles(articles);
		}		
	}
	
}