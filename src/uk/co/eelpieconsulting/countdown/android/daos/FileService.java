package uk.co.eelpieconsulting.countdown.android.daos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.util.Log;

public class FileService {
	
	private static final String TAG = "FileService";
	
	static FileOutputStream getFileOutputStream(Context context, String filename) throws FileNotFoundException {
		Log.i(TAG, "Opening output stream to: " + filename);
		File file = new File(getCacheDir(context) + "/" + filename);
		return new FileOutputStream(file);
	}
		
	static FileInputStream getFileInputStream(Context context, String filename) throws FileNotFoundException {
		File file = new File(getCacheDir(context) + "/" + filename);
		Log.i(TAG, "Opening input stream to: " + file.getAbsolutePath());
		return new FileInputStream(file);
	}

	static boolean existsLocally(Context context, String filename) {
		File localFile = new File(getCacheDir(context), filename);
		boolean result = localFile.exists() && localFile.canRead();
		Log.i(TAG, "Checking for local cache file at '" + localFile.getAbsolutePath() + "': " + result);
		return result;
	}
			
	private static File getCacheDir(Context context) {		
		return context.getCacheDir();
	}
	
}
