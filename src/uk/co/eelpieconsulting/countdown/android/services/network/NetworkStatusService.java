package uk.co.eelpieconsulting.countdown.android.services.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStatusService {
		
	private ConnectivityManager connectivityManager;
	
	public NetworkStatusService(Context context) {
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	public boolean isConnectionAvailable() {
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo != null) {
			final boolean available = activeNetworkInfo.isAvailable();
			return available;
		}
		return false;
	}
	
	public boolean isBackgroundDataAvailable() {
		return connectivityManager.getBackgroundDataSetting() && isConnectionAvailable();
	}
	
}
