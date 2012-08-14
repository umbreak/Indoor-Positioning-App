package android.ui.restclient.service;

import static android.utils.Actions.*;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.ui.pojos.Checkin;
import android.ui.pojos.Comment;
import android.ui.pojos.Picture;
import android.ui.pojos.ShortSite;
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
		if(action.equals(POST_SITES)) {
			try {
				ShortSite[] result=Processor.i.getSites(intent.getIntegerArrayListExtra(SITE_VAULE));  
				Log.d(TAG, result.toString());  
				b.putParcelableArray(GET_SITES, result);
				//				b.putParcelableArrayList("result", results);
				Log.d(TAG, "sync finished GET_SITES");
				if (receiver != null) receiver.send(STATUS_FINISHED, b);

			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					// Pass back error to surface listener
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}
			} 


		}
		else if(action.equals(PUT_USER)) {

			try {
				User user=(User)intent.getParcelableExtra(USER_VAULE);
				int created=Processor.i.putUser(user);
				if (receiver != null && created > 0){
					Log.d(TAG, "sync finished PUT_USER");
					receiver.send(STATUS_FINISHED, Bundle.EMPTY);
				}else{
					Log.e(TAG, "Problem while syncing: user" +user.username + "  already exist");
					if (receiver != null) {
						String result="";
						// Pass back error to surface listener
						if (created == 0)  result="User " +user.username + "  already exist";
						b.putString(Intent.EXTRA_TEXT, result);
						receiver.send(STATUS_ERROR, b);
					}
				}
			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					// Pass back error to surface listener
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}
			} 
		}else if (action.equals(LOGIN)){
			try{
				int result=Processor.i.postUser();
				if (receiver != null && result > 0){
					Log.d(TAG, "sync finished LOGIN");

					receiver.send(STATUS_FINISHED, Bundle.EMPTY);
				}else{
					Log.e(TAG, "Problem while syncing");
					String msg="No response from the server ";
					if (result == -1) msg="Password incorrect";
					else if (result == -3) msg="User doesn't exist";
					if (receiver != null){
						b.putString(Intent.EXTRA_TEXT, msg);
						receiver.send(STATUS_ERROR, b);
					}
				}
					
			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					// Pass back error to surface listener
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}	
			}
		}
		else if (action.equals(LOGOUT)){
			try{
				Processor.i.logoutUser();
			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
			}
		}
		else if(action.equals(DEL_COMMENT)) {
			try{
				int comment_id=intent.getIntExtra(COMMENT_VAULE,0);
				boolean deleted=Processor.i.deleteComment(intent.getStringExtra(SITE_VAULE),comment_id);
				if (receiver != null && deleted){
					Log.d(TAG, "sync finished DEL_COMMENT");
					b.putInt(COMMENT_VAULE, comment_id);
					b.putInt(COMMENT_POSITION, intent.getIntExtra(COMMENT_POSITION,0));
					if (receiver != null) receiver.send(STATUS_FINISHED_DEL_COMMENT, b);
				}else{
					Log.e(TAG, "Problem while deleting comment");
					if (receiver != null) {
						// Pass back error to surface listener
						b.putString(Intent.EXTRA_TEXT, "Problem deleting the comment");
						receiver.send(STATUS_ERROR, b);
					}
				}
			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					// Pass back error to surface listener
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}
			}
		}else if(action.equals(PUT_COMMENT)) {

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
