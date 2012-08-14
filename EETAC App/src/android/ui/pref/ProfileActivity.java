package android.ui.pref;

import static android.utils.Actions.*;

import java.io.File;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.ui.LoginActivity;
import android.ui.R;
import android.utils.MenuHelper;
import android.utils.ServiceHelper;
import android.utils.ToolKit;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ToggleButton;

public class ProfileActivity extends PreferenceActivity implements OnClickListener{
	private Button delUser;
	private Button delCache;
	public ProfileActivity() {
		super();
		this.helper=new MenuHelper(this);
	}

	private MenuHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.user_prefs); 
		//		MenuHelper.setActionBar(this, MenuHelper.ACTIONBAR_PROFILE);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		return helper.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return helper.onOptionsItemSelected(item);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.delUser){
			SharedPreferences prefs=PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editPrefs = prefs.edit();
			editPrefs.putString(PREFS_USER, "");
			editPrefs.putString(PREFS_PASS, "");
			editPrefs.putInt(PREFS_TWITTER_ID, 0);
			editPrefs.putInt(PREFS_USER_ID, 0);
			editPrefs.commit();

			ServiceHelper.startAction(LOGOUT,getApplicationContext());
			startActivity(new Intent(this, LoginActivity.class));
			finish();
			delUser=(Button)v;
			delUser.setEnabled(false);
		}
		else if (v.getId() == R.id.delCache){
			File directory = ToolKit.i.cacheDir;

			File[] files = directory.listFiles();
			for (File file : files)
				file.delete();
			delCache=(Button)v;
			delCache.setEnabled(false);
		}		
	}

	@Override
	protected void onResume() {
		if (delUser != null && delCache != null){
			delUser.setEnabled(true);
			delCache.setEnabled(true);
		}
		super.onResume();
	}

}