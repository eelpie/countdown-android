package uk.co.eelpieconsulting.countdown.android.services;


public class ContentNotAvailableException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final String message;

	public ContentNotAvailableException(Exception cause) {
		super();
		this.message = cause.getMessage();
	}
	
	public ContentNotAvailableException(String message) {
		super();
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
	
}
