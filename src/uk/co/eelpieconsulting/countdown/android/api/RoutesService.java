package uk.co.eelpieconsulting.countdown.android.api;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import uk.co.eelpieconsulting.countdown.android.model.Route;
import uk.co.eelpieconsulting.countdown.model.Stop;
import android.util.Log;

public class RoutesService {
	
	private static final String TAG = "RoutesService";

	public Route getRoute(InputStream routes, String selectedRoute) {	    
	    final ArrayList<Stop> stops = new ArrayList<Stop>();
	    try {    	
	    	BufferedInputStream bis = new BufferedInputStream(routes);
	    	DataInputStream dis = new DataInputStream(bis);	    

	    	while(dis.available() != 0) {
	    		String line = dis.readLine();
	    		if (line.startsWith(selectedRoute + ",")) {
	    			String[] split = line.split(",");
	    			stops.add(new Stop(Integer.parseInt(split[4]), split[6], null, null, null, 0, 0));
	    			Log.i(TAG, line);
	    		}
	    	}
	    	
	    	dis.close();
	    	bis.close();	    	
	    	
	    } catch (Exception e) {
	    	Log.e(TAG, e.getMessage());
	    }
	    
	    return new Route(selectedRoute, 0, stops);	    
	}

}
