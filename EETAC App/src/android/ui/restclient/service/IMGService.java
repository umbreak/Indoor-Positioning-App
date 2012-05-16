package android.ui.restclient.service;

import static android.utils.Actions.*;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.ui.pojos.ShortSite;
import android.ui.restclient.Processor;
import android.util.Log;
import android.utils.ActualSite;
/**
 * Intent Service that send the convinient requests to the Processor.
 * Performs actions related with retrieving PICTURES
 * Once has obtained the result from the Processor, change the status on the receiver,
 * to the correct one (STATUS_FINISHED, STATUS_ERROR,...) */

public class IMGService extends IntentService{
	private static final String TAG = "IMGService";


	public IMGService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		final ResultReceiver receiver = intent.getParcelableExtra(RECEIVER);
		if (receiver != null) receiver.send(STATUS_RUNNING, Bundle.EMPTY);
		String action = intent.getStringExtra(ACTION);
		Bundle b = new Bundle();

		if(action.equals(GET_PICTURE)) {
			try{
				String url=intent.getExtras().getString(PICTURE_URL);

				Bitmap picture=Processor.i.getImage(url);

				b.putParcelable(PICTURE_VALUE, picture);
				b.putString(PICTURE_URL, url);
				
				Log.d(TAG, "sync finished GET_PICTURE");
				if (receiver != null) receiver.send(STATUS_FINISHED_PICTURE_GET, b);

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
