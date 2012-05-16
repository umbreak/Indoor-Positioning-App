package android.ui;

import java.io.File;

import android.app.Activity;
import static android.utils.Actions.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.ui.explore.ExploreActivity;
import android.ui.restclient.Processor;
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
					sleep(2500);
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
		ToolKit.i.setContext(getApplicationContext());
		System.out.println("lereleeeeeee");
		Processor.cache_path=ToolKit.i.cacheDir;

		String user=prefs.getString(PREFS_USER, "");
		if (!user.isEmpty()){
			findViewById(R.id.userLayout).setVisibility(View.VISIBLE);
			((TextView)findViewById(R.id.userName)).setText("Login with: " + user);
			ServiceHelper.startAction(POST_LOGIN, mReceiver,getApplicationContext());
		}		
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
			int result=Integer.valueOf(resultData.getString(POST_LOGIN));
			if (result>=0){
				login=true;
				SharedPreferences.Editor editor=getSharedPreferences(PREFS_FILE, 0).edit();
				editor.putInt(PREFS_USER_ID, result);
				editor.commit();

			} else if (result == -1){
				Toast.makeText(Splash.this, "Password incorrect", Toast.LENGTH_LONG).show();
				((TextView)findViewById(R.id.userName)).setText("Password incorrect");
			}else if (result==-3){
				Toast.makeText(Splash.this, "User not found", Toast.LENGTH_LONG).show();
				((TextView)findViewById(R.id.userName)).setText("User not found");
			}

			else{
				Toast.makeText(Splash.this, "No response from the server " + PreferenceManager.getDefaultSharedPreferences(this).getString(PREFS_SERVER, null),  Toast.LENGTH_LONG).show();
				((TextView)findViewById(R.id.userName)).setText("No response from the server");

			}
			break;
		}
		case STATUS_ERROR: {
			Toast.makeText(Splash.this, "Error in the authentication process", Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}


}
