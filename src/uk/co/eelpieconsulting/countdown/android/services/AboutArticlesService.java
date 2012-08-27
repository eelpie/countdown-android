package uk.co.eelpieconsulting.countdown.android.services;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.List;

import uk.co.eelpieconsulting.common.http.HttpFetcher;
import uk.co.eelpieconsulting.countdown.android.ArticlesNotAvailableException;
import uk.co.eelpieconsulting.countdown.android.model.Article;
import android.content.Context;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;

public class AboutArticlesService {
	
	private HttpFetcher httpFetcher;
	
	public AboutArticlesService() {
		this.httpFetcher = new HttpFetcher();	// TODO Shouldn't be stealing this dependency from the bus client jar
	}
	
	public List<Article> getArticles(Context context, String feedUrl) throws ArticlesNotAvailableException {		
		final List<Article> articles = new ArrayList<Article>();		
		try {			
			final String feedContent = httpFetcher.fetchContent(feedUrl);
			
			final InputStream inputStream = new StringBufferInputStream(feedContent);
			final Reader articleReader = new BufferedReader(new InputStreamReader(inputStream, "UTF8"));		
			
			final SyndFeedInput input = new SyndFeedInput();
			final SyndFeed feed = input.build(articleReader);
			
			List<SyndEntry> entries = (List<SyndEntry>) feed.getEntries();
			for (SyndEntry entry : entries) {
				articles.add(new Article(entry.getTitle(), entry.getDescription().getValue()));				
			}
			
		} catch (Exception e) {
			throw new ArticlesNotAvailableException();
		}
		
		return articles;
	}
	
}
