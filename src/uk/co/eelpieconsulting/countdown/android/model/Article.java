package uk.co.eelpieconsulting.countdown.android.model;

public class Article {

	private final String title;
	private final String body;

	public Article(String title, String body) {
		this.title = title;
		this.body = body;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getBody() {
		return body;
	}
	
	@Override
	public String toString() {
		return "Article [title=" + title + ", body=" + body + "]";
	}
	
}
