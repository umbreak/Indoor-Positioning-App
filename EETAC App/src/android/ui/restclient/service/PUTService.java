package android.ui.restclient.service;

import static android.utils.Actions.*;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.ui.pojos.Checkin;
import android.ui.pojos.Comment;
import android.ui.pojos.Picture;
import android.ui.pojos.User;
import android.ui.restclient.Processor;
import android.util.Log;
/**
 * Intent Service that send the convinient requests to the Processor.
 * Performs the next actions: PUT_COMMENT, PUT_PICTURE, POST_LOGIN, PUT_CHECKIN
 * Once has obtained the result from the Processor, change the status on the receiver,
 * to the correct one (STATUS_FINISHED, STATUS_ERROR,...) */

public class PUTService extends IntentService{
	private static final String TAG = "putService";


	public PUTService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
		if (receiver != null) receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		String action = intent.getStringExtra(ACTION);
		Bundle b = new Bundle();

		if(action.equals(PUT_COMMENT)) {

			try {
				Comment c=(Comment)intent.getParcelableExtra(COMMENT_VAULE);
				Comment comment=Processor.i.putComment(intent.getStringExtra(SITE_VAULE), c);

				Log.d(TAG, comment.toString());  
				b.putParcelable(COMMENT_VAULE, comment);
				Log.d(TAG, "sync finished PUT_COMMENT");
				if (receiver != null) receiver.send(STATUS_FINISHED_COMMENT, b);

			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					// Pass back error to surface listener
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}
			} 
		}
		else if (action.equals(PUT_COMMENT_PICTURE)) {

			try {
				Comment c=(Comment)intent.getParcelableExtra(COMMENT_VAULE);
				Comment comment=Processor.i.putComment(intent.getStringExtra(SITE_VAULE),intent.getStringExtra(PICTURE_URL),c);

				Log.d(TAG, comment.toString());  
				b.putParcelable(COMMENT_VAULE, comment);
				Log.d(TAG, "sync finished PUT_COMMENT");
				if (receiver != null) receiver.send(STATUS_FINISHED_COMMENT, b);

			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					// Pass back error to surface listener
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}
			} 
		}else if (action.equals(PUT_CHECKIN)){
			try{
				Checkin c = new Checkin();
				int result=Processor.i.putCheckin(intent.getStringExtra(SITE_VAULE), c);
				//the other option is to return back from the server the checkin_id and use it on the DEL_CHECKIN Action.
				b.putInt(PUT_CHECKIN, result);
				Log.d(TAG, "sync finished PUT_CHECKIN");
				if (receiver != null) receiver.send(STATUS_FINISHED_CHECKIN, b);

			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					// Pass back error to surface listener
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}
			}

		}else if (action.equals(DEL_CHECKIN)){
			try{
				boolean result=Processor.i.deleteCheckin(intent.getStringExtra(SITE_VAULE), intent.getIntExtra(CHECKIN_ID,-1));
				b.putBoolean(PUT_CHECKIN, result);
				Log.d(TAG, "sync finished PUT_CHECKIN");
				if (receiver != null) receiver.send(STATUS_FINISHED_CHECKOUT, b);

			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					// Pass back error to surface listener
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}
			}

		}
		else if (action.equals(POST_LOGIN)){
			try{
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
				User user= new User();
				String result="";
				user.username=settings.getString(PREFS_USER, "");
				user.password=settings.getString(PREFS_PASS, "");
				System.out.println("Name: " + user.name+ " Password: " + user.password);
				if (!(user.username.isEmpty() && user.password.isEmpty()))
					result=Processor.i.postUser(user);
				Log.d(TAG, result); 
				b.putString(POST_LOGIN, result);
				Log.d(TAG, "sync finished POST_LOGIN");
				if (receiver != null) receiver.send(STATUS_FINISHED, b);

			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					// Pass back error to surface listener
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}	
			}
		}else if (action.equals(POST_PICTURE)){
			try{
				Picture result=Processor.i.postImage(intent.getStringExtra(SITE_VAULE), intent.getStringExtra(PICTURE_URL));
				
				b.putParcelable(POST_PICTURE, result);
				Log.d(TAG, "sync finished POST_PICTURE");
				if (receiver != null) receiver.send(STATUS_FINISHED_PICTURE_POST, b);

			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					// Pass back error to surface listener
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}	
			}
		}
		
//		this.stopSelf();
	}
}
