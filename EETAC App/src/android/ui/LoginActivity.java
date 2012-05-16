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


	}
	private void loginAction(){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(pass.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);	

		SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editPrefs.putString(PREFS_USER, user.getText().toString());
		editPrefs.putString(PREFS_PASS, pass.getText().toString());
		editPrefs.commit();
		ServiceHelper.startAction(POST_LOGIN, mReceiver,getApplicationContext());
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
			if (result>0){
				SharedPreferences.Editor editor=getSharedPreferences(PREFS_FILE, 0).edit();
				editor.putInt(PREFS_USER_ID, result);
				editor.commit();
				startActivity(new Intent(this, ExploreActivity.class));
				finish();
			} else if (result == -1)
				Toast.makeText(LoginActivity.this, "Password incorrect", Toast.LENGTH_LONG).show();
			else if (result == -3)
				Toast.makeText(LoginActivity.this, "User doesn't exist", Toast.LENGTH_LONG).show();
			else
				Toast.makeText(LoginActivity.this, "No response from the server " + PreferenceManager.getDefaultSharedPreferences(this).getString(PREFS_SERVER, null),  Toast.LENGTH_LONG).show();
			break;
		}
		case STATUS_ERROR: {
			Toast.makeText(LoginActivity.this, "Error in the authentication process", Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}

}