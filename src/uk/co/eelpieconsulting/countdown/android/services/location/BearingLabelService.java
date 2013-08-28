package uk.co.eelpieconsulting.countdown.android.services.location;

import java.util.HashMap;
import java.util.Map;

public class BearingLabelService {

	private static final int SECTORS = 16;
	private static final double SECTOR_WIDTH = 360 / SECTORS;
	private static final double CENTRE_OF_SECTOR_OFFSET = SECTOR_WIDTH / 2;

	private final Map<Long, String> labels;
	
	public BearingLabelService() {
		// http://en.wikipedia.org/wiki/Boxing_the_compass
		labels = new HashMap<Long, String>();
		labels.put(0L, "North");
		labels.put(1L, "North-northeast");
		labels.put(2L, "Northeast");
		labels.put(3L, "East-northeast");
		labels.put(4L, "East");
		labels.put(5L, "East-southeast");
		labels.put(6L, "Southeast");
		labels.put(7L, "South-southeast");
		labels.put(8L, "South");
		labels.put(9L, "South-southwest");
		labels.put(10L, "Southwest");
		labels.put(11L, "West-southwest");
		labels.put(12L, "West");
		labels.put(13L, "West-northwest");
		labels.put(14L, "Northwest");
		labels.put(15L, "North-northwest");
	}

	public String bearingLabelFor(double degrees) {	
		long sector = Math.round(Math.ceil((degrees - CENTRE_OF_SECTOR_OFFSET) / (SECTOR_WIDTH)));
		if (sector >= SECTORS) {
			sector = sector - SECTORS;
		}
		return labels.get(sector);
	}
	
}
