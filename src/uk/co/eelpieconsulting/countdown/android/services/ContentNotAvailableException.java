package uk.co.eelpieconsulting.countdown.android.services;


public class ContentNotAvailableException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final Exception cause;

	public ContentNotAvailableException(Exception cause) {
		this.cause = cause;
	}

	public Exception getCause() {
		return cause;
	}
	
}
