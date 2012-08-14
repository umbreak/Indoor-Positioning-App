package android.ui;

import static android.utils.Actions.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.ui.explore.ExploreActivity;
import android.ui.explore.ExploreFavoritesActivity;
import android.ui.twitter.OAuthAccessTokenActivity;
import android.ui.twitter.TwitterUtils;
import android.utils.MyResultReceiver;
import android.utils.ServiceHelper;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The Login Layout. 
 * Perform the basic authentication and introduce user and pass in the SharedPreferences.
 * user saved in PREFS_USER. 
 * pass saved in PREFS_PASS. 
 * SharedPreferences file in PREFS_NAME */

public class LoginActivity extends Activity implements OnClickListener, MyResultReceiver.Receiver{
	private SharedPreferences preferences;
	private EditText user,pass;
	private static MyResultReceiver mReceiver;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		user=(EditText)findViewById(R.id.userText);
		pass=(EditText)findViewById(R.id.passText);
		mReceiver = (MyResultReceiver) getLastNonConfigurationInstance();

		TextView t2 = (TextView) findViewById(R.id.registerText);
		t2.setMovementMethod(LinkMovementMethod.getInstance());
		final boolean previousState = mReceiver != null;

		if (previousState)
			// Start listening for SyncService updates again
			mReceiver.setReceiver(this);
		else {
			mReceiver=new MyResultReceiver(new Handler());
			mReceiver.setReceiver(this);
		}
		//If user and pass already exist in the preferences, put it in the layout
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		if (preferences.getInt(PREFS_TWITTER_ID, 0) == 0){
			user.setText(preferences.getString(PREFS_USER, ""));
			pass.setText(preferences.getString(PREFS_PASS, ""));
		}

		pass.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					if (!(user.getText().toString().isEmpty() && pass.getText().toString().isEmpty())){
						loginAction();	
						return true;
					}
				}
				return false;
			}
		});
	}

	public void onClick(View v) {
		if (v.getId() == R.id.loginButton){
			//			startActivity(new Intent(getBaseContext(), TestActivity.class));
			if (!(user.getText().toString().isEmpty() && pass.getText().toString().isEmpty()))
				loginAction();
			//			startActivity(new Intent(getBaseContext(), MapActivity.class));

			else
				Toast.makeText(this, "Fill the user and password fields.", Toast.LENGTH_SHORT).show();
		}else if (v.getId() == R.id.registerText)
			startActivity(new Intent(this, RegisterActivity.class));
		else if (v.getId() == R.id.twitter_button)
			if (TwitterUtils.isAuthenticated(preferences))
				ServiceHelper.startAction(LOGIN, mReceiver,getApplicationContext());
			else
				startActivity(new Intent(this, OAuthAccessTokenActivity.class));

	}
	private void loginAction(){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(pass.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);	

		SharedPreferences.Editor editPrefs = preferences.edit();
		editPrefs.putString(PREFS_USER, user.getText().toString());
		editPrefs.putString(PREFS_PASS, pass.getText().toString());
		editPrefs.putInt(PREFS_TWITTER_ID, 0);
		editPrefs.commit();

		ServiceHelper.startAction(LOGIN, mReceiver,getApplicationContext());
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mReceiver.clearReceiver();
		return mReceiver;
	}

	@Override
	protected void onPause() {
		mReceiver.clearReceiver();
		super.onPause();
	}
	@Override
	protected void onResume() {
		mReceiver.setReceiver(this);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (getIntent().getBooleanExtra(TWITTER_NOTIFICATOINS, false))
			if (prefs.getInt(PREFS_TWITTER_ID, 0) > 0)
				ServiceHelper.startAction(LOGIN, mReceiver,getApplicationContext());
		super.onResume();
	}

	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case STATUS_RUNNING: {
			break;
		}
		case STATUS_FINISHED: {
			startActivity(new Intent(this, ExploreActivity.class));
			finish();
			break;
		}
		case STATUS_ERROR: {
			Toast.makeText(LoginActivity.this, resultData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}

}