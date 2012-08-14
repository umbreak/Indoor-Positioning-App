package android.utils;

import static android.utils.Actions.*;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.ui.restclient.service.GETService;
import android.ui.restclient.service.IMGService;
import android.ui.restclient.service.PUTService;
import android.widget.Toast;
/**
 * Class that creates the intents to the IntentServices,
 * both GETService and PUTService */

public class ServiceHelper {

	//	private static final String LOG = ServiceHelper.class.getName();
	private ServiceHelper() {
	}
	
	public synchronized static boolean startAction(String action, Context c) {
		return startAction(action, null, null, null, null, c);
	}
	public synchronized static boolean startAction(String action, MyResultReceiver receiver, Context c) {
		return startAction(action, null, null, null, receiver, c);
	}
	public synchronized static boolean startAction(String action, Bundle extras,MyResultReceiver receiver, Context c) {
		return startAction(action, null, null, extras, receiver, c);
	}
	public synchronized static boolean startAction(String action, String site, MyResultReceiver receiver, Context c) {
		return startAction(action, site, null, null, receiver, c);
	}
	public synchronized static boolean startAction(String action, String site, Bundle extras, MyResultReceiver receiver, Context c) {
		return startAction(action, site, null, extras, receiver, c);
	}
	public synchronized static boolean startAction(String action, String site, String picture, MyResultReceiver receiver, Context c) {
		return startAction(action, site, picture, null, receiver, c);
	}
	public synchronized static boolean startAction(String action, String site, String picture, Bundle extras, MyResultReceiver receiver, Context c) {
		if (ToolKit.i.isInternetAvailable()){
			Intent intent = null;
			if (action.startsWith("GET"))
				intent = new Intent(Intent.ACTION_SYNC, null, c, GETService.class);
			else if (action.startsWith("IMG"))
				intent = new Intent(Intent.ACTION_SYNC, null, c, IMGService.class);
			else
				intent = new Intent(Intent.ACTION_SYNC, null, c, PUTService.class);

			intent.putExtra(ACTION, action);
			if (site!=null) intent.putExtra(SITE_VAULE, site);
			if (picture!=null) intent.putExtra(PICTURE_URL, picture);
			if(extras!=null) intent.putExtras(extras);
			if(receiver!=null) intent.putExtra(RECEIVER, receiver);
			c.startService(intent);

			return true;
		}else{
			Toast.makeText(c, "No internet connection", Toast.LENGTH_SHORT).show();
			return false;
		}

	}

}