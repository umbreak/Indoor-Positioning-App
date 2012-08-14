package android.ui.explore;

import java.util.ArrayList;

import com.markupartist.android.widget.ActionBar;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.ui.R;
import android.ui.restclient.Processor;
import android.utils.MenuHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
/**
 * Main Activity of the explore menu. It creates 3 tabs,
 * each one has an intent of ExploreTabSelectionActivity. */

public class ExploreActivity extends TabActivity{
	private MenuHelper helper;
	public ExploreActivity(){
		super();
		this.helper=new MenuHelper(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.explore);
		MenuHelper.setActionBar(this, MenuHelper.ACTIONBAR_EXPLORE);
		initTabs();
		
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		return helper.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return helper.onOptionsItemSelected(item);
	}
	
	private View buildIndicator(String textRes) {
		final TextView indicator = (TextView) getLayoutInflater().inflate(R.layout.tab_indicator,
				getTabWidget(), false);
		indicator.setText(textRes);
		return indicator;
	}
	private void initTabs(){
		TabHost th = getTabHost();
		Intent i= new Intent(getBaseContext(), ExploreTabSelectionActivity.class);
		TabSpec specs = th.newTabSpec("tag1").setContent(i).setIndicator(buildIndicator("Nearby"));
		th.addTab(specs);

		i= new Intent(getBaseContext(), ExploreRecentsActivity.class);
		specs = th.newTabSpec("tag2").setContent(i).setIndicator(buildIndicator("Recents"));
		th.addTab(specs);

		i= new Intent(getBaseContext(), ExploreFavoritesActivity.class);
		specs = th.newTabSpec("tag3").setContent(i).setIndicator(buildIndicator("Favorites"));
		th.addTab(specs);
//		ArrayList<Integer> integers= new ArrayList<Integer>();
//		integers.add(1);
////		integers.add(2);
//		Processor.i.getSites(integers);
	}
}
