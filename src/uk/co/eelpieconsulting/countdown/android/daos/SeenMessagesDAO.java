package uk.co.eelpieconsulting.countdown.android.daos;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.util.Log;

public class SeenMessagesDAO {
	
	private static final String TAG = "SeenMessagesDAO";
	
	private static final String READ_MESSAGES_FILENAME = "seenmessages.ser";
	
	private Context context;

	public SeenMessagesDAO(Context context) {
		this.context = context;				
	}
	
	public Set<String> getSeenMessages() {
		try {
			if (FileService.existsLocally(context, READ_MESSAGES_FILENAME)) {
				FileInputStream fis = FileService.getFileInputStream(context, READ_MESSAGES_FILENAME);
				ObjectInputStream in = new ObjectInputStream(fis);
				@SuppressWarnings("unchecked")
				Set<String> seenMessages = (Set<String>) in.readObject();
				in.close();
				fis.close();			
				return seenMessages;
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (ClassNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}		
		return new HashSet<String>();
	}

	public void setSeenMessages(Set<String> seenMessages) {
		Log.i(TAG, "Marking messages as seen: " + seenMessages);
		try {
			FileOutputStream fos = FileService.getFileOutputStream(context, READ_MESSAGES_FILENAME);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(seenMessages);
			out.close();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}
	
}
