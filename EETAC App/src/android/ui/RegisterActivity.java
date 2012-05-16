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

	private EditText name, user, pass, mail;
	private String currentPhotoPath;
	private static MyResultReceiver mReceiver;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		name=(EditText)findViewById(R.id.nameReg);
		user=(EditText)findViewById(R.id.usernameReg);
		mail=(EditText)findViewById(R.id.emailReg);
		pass=(EditText)findViewById(R.id.passReg);
		currentPhotoPath=null;
		mReceiver = (MyResultReceiver) getLastNonConfigurationInstance();
		final boolean previousState = mReceiver != null;

		if (previousState)
			// Start listening for SyncService updates again
			mReceiver.setReceiver(this);
		else {
			mReceiver=new MyResultReceiver(new Handler());
			mReceiver.setReceiver(this);
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.loginButton){
			if (name.getText().toString().isEmpty())
				Toast.makeText(this, "Fill the Name field.", Toast.LENGTH_SHORT).show();
			else if (user.getText().toString().isEmpty())
				Toast.makeText(this, "Fill the Username field.", Toast.LENGTH_SHORT).show();
			else if (mail.getText().toString().isEmpty())
				Toast.makeText(this, "Fill the Email field.", Toast.LENGTH_SHORT).show();
			else if (pass.getText().toString().isEmpty())
				Toast.makeText(this, "Fill the Password field.", Toast.LENGTH_SHORT).show();
			else if (mail.getText().toString().isEmpty())
				Toast.makeText(this, "Fill the Name field.", Toast.LENGTH_SHORT).show();
			else
				register();
		}else if (v.getId() == R.id.loadImageReg){
			Intent intent = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, 0);
		}

	}
	private void register(){
		startActivity(new Intent(this, LoginActivity.class));
//		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.hideSoftInputFromWindow(pass.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);	
//
//		SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
//		editPrefs.putString(PREFS_USER, user.getText().toString());
//		editPrefs.putString(PREFS_PASS, pass.getText().toString());
//		editPrefs.commit();
		//		ServiceHelper.startAction(POST_LOGIN, mReceiver,getApplicationContext());
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mReceiver.clearReceiver();
		return mReceiver;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK){
			currentPhotoPath=data.getData().toString();
			System.out.println("Image to Load: " + currentPhotoPath);
		}
	}


	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case STATUS_RUNNING: {
			break;
		}
		case STATUS_FINISHED: {
			int result=Integer.valueOf(resultData.getString(POST_LOGIN));
			if (result>=0){
				SharedPreferences.Editor editor=getSharedPreferences(PREFS_FILE, 0).edit();
				editor.putInt(PREFS_USER_ID, result);
				editor.commit();
				startActivity(new Intent(getBaseContext(), ExploreActivity.class));
				finish();
			} else if (result == -1)
				Toast.makeText(RegisterActivity.this, "User not found or password incorrect", Toast.LENGTH_LONG).show();

			else{
				Toast.makeText(RegisterActivity.this, "No response from the server " + PreferenceManager.getDefaultSharedPreferences(this).getString(PREFS_SERVER, null),  Toast.LENGTH_LONG).show();

			}
			break;
		}
		case STATUS_ERROR: {
			Toast.makeText(RegisterActivity.this, "Error in the authentication process", Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}

}