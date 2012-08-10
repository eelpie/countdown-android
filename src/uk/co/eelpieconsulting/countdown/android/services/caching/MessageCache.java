package uk.co.eelpieconsulting.countdown.android.services.caching;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import uk.co.eelpieconsulting.busroutes.model.MultiStopMessage;
import uk.co.eelpieconsulting.countdown.android.daos.FileService;
import android.content.Context;
import android.util.Log;

public class MessageCache {

	private static final String TAG = "MessageCache";

	private static final long TEN_MINUTES = 10 * 60 * 1000;
	
	private final Context context;

	public MessageCache(Context context) {
		this.context = context;
	}

	public List<MultiStopMessage> getStopMessages(int[] stopIds) {
		final String cacheFilename = getCacheFilenameFor(stopIds);
		if (FileService.existsLocallyAndIsNotStale(context, cacheFilename, TEN_MINUTES)) {
			try {
				FileInputStream fileInputStream = FileService.getFileInputStream(context, cacheFilename);
				ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
				List<MultiStopMessage> readObject = (List<MultiStopMessage>) objectInputStream.readObject();
				objectInputStream.close();
				fileInputStream.close();
				return readObject;

			} catch (Exception e) {
				Log.w(TAG, "Failed to read from cache file: " + e.getMessage());
				return null;
			}
		}
		return null;
	}

	public void cache(int[] stopIds, List<MultiStopMessage> messages) {
		final String cacheFilename = getCacheFilenameFor(stopIds);
		Log.d(TAG, "Writing to disk: " + cacheFilename);
		try {
			FileOutputStream fileOutputStream = FileService.getFileOutputStream(context, cacheFilename);
			ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
			out.writeObject(messages);
			out.close();

		} catch (Exception e) {
			Log.e(TAG, "Failed to write to cache file: " + e.getMessage());
			Log.e(TAG, e.getMessage());
		}
		Log.d(TAG, "Finished writing to disk: " + cacheFilename);
	}

	private String getCacheFilenameFor(int[] stopIds) {
		StringBuilder filename = new StringBuilder("messages-");
		for (int stopId : stopIds) {
			filename.append(stopId);
		}
		filename.append(".ser");
		return  SafeFilenameService.makeSafeFilenameFor(filename.toString());
	}
	
}
