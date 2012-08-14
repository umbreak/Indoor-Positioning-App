package android.ui.explore;

import static android.utils.Actions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.ui.R;
import android.ui.adapters.SiteAdapter;
import android.ui.map.MapActivity;
import android.ui.pojos.ShortSite;
import android.util.Log;
import android.utils.ActualSite;
import android.utils.MenuHelper;
import android.utils.MyResultReceiver;
import android.utils.ServiceHelper;
import android.utils.ToolKit;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
/**
 * List of ShortSites. Uses the SiteAdapter as a bridge.
 * Performs the call of GET_SITE (concurrently) action on GETService 
 * and handle the response by implementing onReceiveResult.
 * Sites obtained from the Shared Preferences, PREFS_FAVORITES ArrayList of Integers*/

public class ExploreRecentsActivity extends ListActivity implements MyResultReceiver.Receiver{

	private SiteAdapter m_adapter;
	private State mState;
	private ActionBar actionBar;

	private RefreshClick onRefreshClick;
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		actionBar=MenuHelper.actionBar;
		context=this;


		mState = (State) getLastNonConfigurationInstance();
		onRefreshClick = new RefreshClick();
		MenuHelper.actionBar.setHomeAction(onRefreshClick);




		final boolean previousState = mState != null;
		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			ToolKit.updateRefreshStatus(mState.mSyncing);
		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
			//			onRefreshClick.performAction(null);
		}

		getListView().setItemsCanFocus(true);
		m_adapter = new SiteAdapter(this, R.layout.explore_site, mState.listSites);
		setListAdapter(this.m_adapter);


	}


	private class RefreshClick extends AbstractAction {
		private Bundle type;

		public RefreshClick() {
			super(R.drawable.ic_title_refresh_default);
			type= new Bundle();
			type.putBoolean(SITE_SHORT, true);

		}
		public void performAction(View view) {
			ToolKit.updateRefreshStatus(mState.mSyncing);
			mState.listSites.clear();
			if (ToolKit.i.recentsQueue.size() > 0){
				Bundle b = new Bundle();
				ArrayList<Integer> list = new ArrayList<Integer>();
				Integer array[]=(Integer[])ToolKit.i.recentsQueue.toArray(new Integer[0]);
				Collections.addAll(list, array);
				b.putIntegerArrayList(SITE_VAULE, list);
				ServiceHelper.startAction(POST_SITES,b,mState.mReceiver,getApplicationContext());
			}
			//			for (integer site_id: toolkit.i.recentsqueue){
			//				servicehelper.startaction(get_site, string.valueof(site_id), type, mstate.mreceiver,context);
			//			}
		}
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mState.mReceiver.clearReceiver();
		return mState;
	}
	@Override
	protected void onPause() {
		mState.mReceiver.clearReceiver();
		super.onPause();
	}
	@Override
	protected void onResume() {
		mState.mReceiver.setReceiver(this);
		MenuHelper.actionBar=actionBar;
		MenuHelper.actionBar.setHomeAction(onRefreshClick);

		onRefreshClick.performAction(null);
		super.onResume();
	}

	private static class State {
		public MyResultReceiver mReceiver;
		public ArrayList<ShortSite> listSites;
		public boolean mSyncing = false;
		private State() {
			listSites=new ArrayList<ShortSite>();
			mReceiver = new MyResultReceiver(new Handler());
		}
	}

	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case STATUS_RUNNING: {
			mState.mSyncing = true;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}
		case STATUS_FINISHED: {
			ShortSite[] s= (ShortSite[])resultData.getParcelableArray(GET_SITES);
			m_adapter.notifyDataSetChanged();
			mState.listSites.clear();
			mState.listSites.addAll(Arrays.asList(s));	
			m_adapter.notifyDataSetChanged();
			//			runOnUiThread(returnRes);

			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}
		case STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			Toast.makeText(ExploreRecentsActivity.this, resultData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}
}