package android.ui.restclient.service;

import static android.utils.Actions.*;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.ui.pojos.Comment;
import android.ui.pojos.ShortSite;
import android.ui.restclient.Processor;
import android.util.Log;
import android.utils.ActualSite;
/**
 * Intent Service that send the convinient requests to the Processor.
 * Performs the next actions: GET_SITES, GET_SITE, ...
 * Once has obtained the result from the Processor, change the status on the receiver,
 * to the correct one (STATUS_FINISHED, STATUS_ERROR,...) */

public class GETService extends IntentService{
	private static final String TAG = "getService";


	public GETService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		final ResultReceiver receiver = intent.getParcelableExtra("receiver");
		if (receiver != null) receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		String action = intent.getStringExtra(ACTION);
		Bundle b = new Bundle();
		if(action.equals(GET_SITES)) {

			try {
				ShortSite[] result=Processor.i.getSites();  
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


		}else if(action.equals(GET_SITE)) {
			try {
				boolean isShort=false;
				isShort=intent.getExtras().getBoolean(SITE_SHORT);
				if (isShort)
					ActualSite.shortSite=Processor.i.getShortSite(intent.getStringExtra(SITE_VAULE));
				else
					ActualSite.site=Processor.i.getSite(intent.getStringExtra(SITE_VAULE));

				Log.d(TAG, "sync finished GET_SITE");
				if (receiver != null) receiver.send(STATUS_FINISHED, Bundle.EMPTY);

			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}
			}    
		}
		else if(action.equals(GET_COMMENTS_PICTURE)) {
			try {
				Comment[] comments=Processor.i.getPictureComments(intent.getStringExtra(SITE_VAULE), intent.getStringExtra(PICTURE_URL));
				b.putParcelableArray(GET_COMMENTS_PICTURE, comments);
				Log.d(TAG, "sync finished GET_COMMENTS_PICTURE");
				if (receiver != null) receiver.send(STATUS_FINISHED_COMMENTS_PIC_GET, b);


			} catch(Exception e) {
				Log.e(TAG, "Problem while syncing", e);
				if (receiver != null) {
					b.putString(Intent.EXTRA_TEXT, e.toString());
					receiver.send(STATUS_ERROR, b);
				}
			}    
		}

		//		this.stopSelf();
	}
}
