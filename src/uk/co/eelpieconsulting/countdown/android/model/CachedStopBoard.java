package uk.co.eelpieconsulting.countdown.android.model;

import uk.co.eelpieconsulting.buses.client.model.StopBoard;

public class CachedStopBoard {

	private StopBoard stopBoard;
	private long fetchDate;
	
	public CachedStopBoard(StopBoard stopBoard, long fetchDate) {
		this.stopBoard = stopBoard;
		this.fetchDate = fetchDate;
	}

	public StopBoard getStopBoard() {
		return stopBoard;
	}

	public long getFetchDate() {
		return fetchDate;
	}
	
}
