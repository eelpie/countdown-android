package uk.co.eelpieconsulting.countdown.android.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import uk.co.eelpieconsulting.countdown.android.R;
import uk.co.eelpieconsulting.countdown.android.model.Article;
import android.content.Context;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

public class AboutArticlesService {

	public List<Article> getArticles(Context context) {		
		final List<Article> articles = new ArrayList<Article>();		
		try {
			final InputStream raw = context.getResources().openRawResource(R.raw.articles);
			final Reader articleReader = new BufferedReader(new InputStreamReader(raw, "UTF8"));		
			final SyndFeedInput input = new SyndFeedInput();
			final SyndFeed feed = input.build(articleReader);
			
			List<SyndEntry> entries = (List<SyndEntry>) feed.getEntries();
			for (SyndEntry entry : entries) {
				articles.add(new Article(entry.getTitle(), entry.getDescription().getValue()));				
			}
			
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FeedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return articles;
	}
	
}
