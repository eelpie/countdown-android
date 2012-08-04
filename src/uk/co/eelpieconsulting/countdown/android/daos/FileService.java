package uk.co.eelpieconsulting.countdown.android.daos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.util.Log;

public class FileService {
	
	private static final String TAG = "FileService";
	
	public static FileOutputStream getFileOutputStream(Context context, String filename) throws FileNotFoundException {
		Log.i(TAG, "Opening output stream to: " + filename);
		File file = new File(getCacheDir(context) + "/" + filename);
		return new FileOutputStream(file);
	}
		
	public static FileInputStream getFileInputStream(Context context, String filename) throws FileNotFoundException {
		File file = new File(getCacheDir(context) + "/" + filename);
		Log.i(TAG, "Opening input stream to: " + file.getAbsolutePath());
		return new FileInputStream(file);
	}
	
	public static boolean existsLocally(Context context, String filename) {
		File localFile = new File(getCacheDir(context), filename);
		return (localFile.exists() && localFile.canRead());
	}
	
	public static boolean existsLocallyAndIsNotStale(Context context, String filename, long ttl) {
		File localFile = new File(getCacheDir(context), filename);
		if (localFile.exists() && localFile.canRead()) {
			Log.d(TAG, "Cache file exists and is readable: " + filename);		
			final long age = System.currentTimeMillis() - localFile.lastModified();
			if (age <= ttl) {
				Log.d(TAG, "Cache file is more recent than the ttl: " + age + "/" + ttl);
				return true;
			} else {
				Log.d(TAG, "File is older than TTL: " + age + "/" + ttl);
			}
		}
		
		Log.d(TAG, "Cache file is not valid: " + filename);
		return false;
	}
	
	private static File getCacheDir(Context context) {		
		return context.getCacheDir();
	}
	
}
