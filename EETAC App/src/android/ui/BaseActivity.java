package android.ui;

import android.app.Activity;
import android.utils.MenuHelper;
import android.view.Menu;
import android.view.MenuItem;
/**
 * BaseActivity used for create the menus.
 * Other activities should extend from this.
 * Uses the MenuHelper to perform the Menu actions. */

public class BaseActivity extends Activity{
	private MenuHelper helper;
	public BaseActivity(){
		super();
		this.helper=new MenuHelper(this);
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
