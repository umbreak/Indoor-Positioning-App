package android.ui.pref;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.ui.R;
import android.utils.MenuHelper;
import android.view.Menu;
import android.view.MenuItem;

public class ProfileActivity extends PreferenceActivity{
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
	
}