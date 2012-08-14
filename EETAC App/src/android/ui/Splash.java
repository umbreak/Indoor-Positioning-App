package android.ui;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import static android.utils.Actions.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.ui.explore.ExploreActivity;
import android.ui.restclient.Processor;
import android.ui.twitter.OAuthAccessTokenActivity;
import android.ui.twitter.TwitterUtils;
import android.utils.MyResultReceiver;
import android.utils.ServiceHelper;
import android.utils.ToolKit;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Shows the Splash screen from the layout splash.xml */

public class Splash extends Activity implements MyResultReceiver.Receiver{
	private static MyResultReceiver mReceiver;
	private boolean login=false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		mReceiver = (MyResultReceiver) getLastNonConfigurationInstance();
		final boolean previousState = mReceiver != null;

		if (previousState)
			// Start listening for SyncService updates again
			mReceiver.setReceiver(this);
		else {
			mReceiver=new MyResultReceiver(new Handler());
			mReceiver.setReceiver(this);
		}

		Thread timer=new Thread(){

			@Override
			public void run() {
				try{
					sleep(3500);
				}catch(InterruptedException e){
					e.printStackTrace();
				}finally{
					if(login)
						startActivity(new Intent(getBaseContext(), ExploreActivity.class));
					else
						startActivity(new Intent(getBaseContext(), LoginActivity.class));
					finish();

				}
			}

		};
		timer.start();
		PreferenceManager.setDefaultValues(this, R.xml.user_prefs,true);
		SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
		Processor.setUrl(prefs.getString(PREFS_SERVER, null));
		Processor.appContext=getApplicationContext();
		ToolKit.i.setContext(getApplicationContext());
		Processor.cache_path=ToolKit.i.cacheDir;

		String user=prefs.getString(PREFS_USER, "");
		if (!user.isEmpty()){
			findViewById(R.id.userLayout).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.userName)).setText("Login with: " + user);
			if (prefs.getInt(PREFS_TWITTER_ID, 0) == 0)
				ServiceHelper.startAction(LOGIN, mReceiver,getApplicationContext());
			else
				//If we're using Twitter credentials, let's see if Twitter token already works or expired. If we're still authenticated in twitter, let's authenticate in our system.
				//If we're not authenticated in twitter, let's start the twitter auth activity
				if (TwitterUtils.isAuthenticated(prefs))
					ServiceHelper.startAction(LOGIN, mReceiver,getApplicationContext());
				else
					startActivity(new Intent(this, OAuthAccessTokenActivity.class));
		}		
	}
	@Override
	protected void onPause() {
		mReceiver.clearReceiver();
		super.onPause();
	}
	@Override
	protected void onResume() {
		mReceiver.setReceiver(this);
		super.onResume();
	}
	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mReceiver.clearReceiver();
		return mReceiver;
	}

	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case STATUS_RUNNING: {
			break;
		}
		case STATUS_FINISHED: {
			login=true;
			break;
		}
		case STATUS_ERROR: {
			String result=resultData.getString(Intent.EXTRA_TEXT);
			Toast.makeText(Splash.this, result, Toast.LENGTH_LONG).show();
			((TextView)findViewById(R.id.userName)).setText(result);
			login=false;
			break;
		}
		}		
	}


}
