package android.utils;


import static android.utils.Actions.*;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.ui.LoginActivity;
import android.ui.R;
import android.ui.check.CheckInActivity;
import android.ui.explore.ExploreActivity;
import android.ui.map.MapActivity;
import android.ui.pref.ProfileActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

/**
 * Give a Framework for act on the Menu and the ActionBar. */
public class MenuHelper {
	private Activity parent;
	public static final int ACTIONBAR_MAP = 0x1;
	public static final int ACTIONBAR_EXPLORE = 0x2;
	public static final int ACTIONBAR_CHECKIN = 0x3;
	//	public static final int ACTIONBAR_PROFILE = 0x4;
	public static final int ACTIONBAR_EXPLORE_2 = 0x4;
	public static ActionBar actionBar;
	public MenuHelper(Activity parent) {
		super();
		this.parent = parent;
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = parent.getMenuInflater();
		inflater.inflate(R.menu.test_menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menuLogout) {
			ServiceHelper.startAction(LOGOUT,parent.getApplicationContext());
			parent.startActivity(new Intent(parent.getBaseContext(), LoginActivity.class));
			parent.finish();
			return true;
		}
		else if (item.getItemId() == R.id.menuExit) {
			ServiceHelper.startAction(LOGOUT,parent.getApplicationContext());
			parent.moveTaskToBack(true);
//			System.exit(0);
			return true;
		}else if (item.getItemId() == R.id.menuProf) {
			parent.startActivity(new Intent(parent.getBaseContext(), ProfileActivity.class));
			return true;

		}else {
			return parent.onOptionsItemSelected(item);
		}
	}
	public static void setActionBar(Activity parent){
		setActionBar(parent, -1);
	}
	public static void setActionBar(Activity parent, int choice){
		actionBar = (ActionBar) parent.findViewById(R.id.actionbar);
		actionBar.setTitle(parent.getTitle());   

		final Action mapAction = new IntentAction(parent, new Intent(parent, MapActivity.class), R.drawable.bar_map_btn);  
		int explore_drawable=R.drawable.bar_explore_btn;
		if (choice == ACTIONBAR_EXPLORE_2) explore_drawable=R.drawable.ic_menu_search_disabled;
		final Action exploreAction = new IntentAction(parent, new Intent(parent, ExploreActivity.class),explore_drawable );  
		final Action checkinAction = new IntentAction(parent, new Intent(parent, CheckInActivity.class), R.drawable.bar_check_btn);
		//        final Action profileAction = new IntentAction(parent, new Intent(parent, ProfileActivity.class), R.drawable.ic_menu_manage);
		boolean map_touchable=true;
		boolean explore_touchable=true;
		boolean checkin_touchable=true;
		//        boolean profile_touchable=true;
		if (choice== ACTIONBAR_MAP) map_touchable=false;
		else if (choice == ACTIONBAR_EXPLORE) explore_touchable=false;
		else if (choice == ACTIONBAR_CHECKIN) checkin_touchable=false;
		//        else if (choice == ACTIONBAR_PROFILE) profile_touchable=false;

		actionBar.addAction(mapAction, map_touchable);
		actionBar.addAction(exploreAction, explore_touchable);
		actionBar.addAction(checkinAction, checkin_touchable);
		//        actionBar.addAction(profileAction, profile_touchable);
	}

}
