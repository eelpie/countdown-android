package uk.co.eelpieconsulting.countdown.android;

import java.util.List;

import uk.co.eelpieconsulting.countdown.android.model.Article;
import uk.co.eelpieconsulting.countdown.android.services.AboutArticlesService;
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

	private FetchArticlesTask fetchMessagesTask;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        
        setTitle(R.string.about);
        TextView status = (TextView) findViewById(R.id.status);
        status.setText(R.string.about_text);
        status.setVisibility(View.VISIBLE);
        
		stopsList = (LinearLayout) findViewById(R.id.stopsList);
		
        aboutArticlesService = new AboutArticlesService();
        
    	fetchMessagesTask = new FetchArticlesTask(aboutArticlesService);
		fetchMessagesTask.execute(ABOUT_ARTICLES_FEED_URL);
    }
	
	@Override
	protected void onPause() {
		super.onPause();
		if (fetchMessagesTask != null && fetchMessagesTask.getStatus().equals(Status.RUNNING)) {
			fetchMessagesTask.cancel(true);
		}	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.alerts_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {		
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
	
	private void renderArticles(final List<Article> articles) {
		if (articles == null) {
			return;
		}
		for (Article article : articles) {
			stopsList.addView(StopDescriptionService.makeArticleView(article, getApplicationContext()));
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