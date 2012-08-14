package android.ui;

import static android.utils.Actions.*;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.ui.explore.ExploreActivity;
import android.ui.explore.ExploreFavoritesActivity;
import android.ui.pojos.User;
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

public class RegisterActivity extends Activity implements OnClickListener, MyResultReceiver.Receiver{

	private EditText user, pass, mail;
	private static MyResultReceiver mReceiver;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		user=(EditText)findViewById(R.id.usernameReg);
		pass=(EditText)findViewById(R.id.passReg);
		mail=(EditText)findViewById(R.id.emailReg);
		mReceiver = (MyResultReceiver) getLastNonConfigurationInstance();
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
					register();	
					return true;
				}
				return false;
			}
		});
	}

	public void onClick(View v) {
		if (v.getId() == R.id.loginButton){
			register();
		}else if (v.getId() == R.id.loadImageReg){
			Intent intent = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, 0);
		}

	}
	private void register(){
		if (user.getText().toString().isEmpty())
			Toast.makeText(this, "Fill the Username field.", Toast.LENGTH_SHORT).show();
		else if (pass.getText().toString().isEmpty())
			Toast.makeText(this, "Fill the Password field.", Toast.LENGTH_SHORT).show();
		else{
			User newUser= new User(user.getText().toString(), pass.getText().toString());
			newUser.email=mail.getText().toString();
			Bundle b = new Bundle();
			b.putParcelable(USER_VAULE, newUser);
			SharedPreferences.Editor localPrefs=PreferenceManager.getDefaultSharedPreferences(this).edit();
			localPrefs.putInt(PREFS_TWITTER_ID, 0);
			localPrefs.commit();
			ServiceHelper.startAction(PUT_USER, b, mReceiver,getApplicationContext());

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
			SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
			editPrefs.putString(PREFS_USER, user.getText().toString());
			editPrefs.putString(PREFS_PASS, pass.getText().toString());
			editPrefs.commit();
			startActivity(new Intent(this, ExploreActivity.class));
			break;

		}
		case STATUS_ERROR: {
			Toast.makeText(this, resultData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}

}