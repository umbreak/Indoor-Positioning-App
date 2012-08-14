package android.ui.explore;

import static android.utils.Actions.*;
import java.util.ArrayList;
import java.util.Arrays;

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
import android.ui.pojos.Comment;
import android.ui.pojos.ShortSite;
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

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
/**
 * List of ShortSites. Uses the SiteAdapter as a bridge.
 * Performs the call of GET_SITES action on GETService 
 * and handle the response by implementing onReceiveResult.*/

public class ExploreTabSelectionActivity extends ListActivity implements MyResultReceiver.Receiver{
	//	private ProgressDialog m_ProgressDialog = null; 
	//	private List<ShortSite> listSites = null;
	private SiteAdapter m_adapter;
	//	private Runnable viewOrders;
	private Intent longSiteIntent;
	private State mState;
	private ActionBar actionBar;
	private RefreshClick onRefreshClick;
	private SharedPreferences preferences;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		actionBar=MenuHelper.actionBar;

		mState = (State) getLastNonConfigurationInstance();
		onRefreshClick = new RefreshClick();
		MenuHelper.actionBar.setHomeAction(onRefreshClick);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);


		final boolean previousState = mState != null;
		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			ToolKit.updateRefreshStatus(mState.mSyncing);
		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
			if (!preferences.getBoolean(UPDATE_ALWAYS, false))
				onRefreshClick.performAction(null);
		}

		m_adapter = new SiteAdapter(this, R.layout.explore_site, mState.listSites);
		setListAdapter(this.m_adapter);
		getListView().setItemsCanFocus(true);

		
		//		if (previousState) runOnUiThread(returnRes);

		//		viewOrders = new Runnable(){
		//			public void run() {
		//				getSites();
		//			}
		//		};
		//		Thread thread =  new Thread(null, viewOrders, "MagentoBackground");
		//		thread.start();
		//		m_ProgressDialog = ProgressDialog.show(ExploreTabSelectionActivity.this,    
		//				"Please wait...", "Retrieving data ...", true);
	}



	private class RefreshClick extends AbstractAction {

		public RefreshClick() {
			super(R.drawable.ic_title_refresh_default);
		}
		public void performAction(View view) {
			ToolKit.updateRefreshStatus(mState.mSyncing);
			ServiceHelper.startAction(GET_SITES, mState.mReceiver,getApplicationContext());
		}

	}

	//	private Runnable returnRes = new Runnable() {
	//		public void run() {
	//			
	//			if(mState.listSites != null && mState.listSites.size() > 0){
	//				m_adapter.notifyDataSetChanged();
	//				System.out.println("RUNNNN: " + mState.listSites);
	//				System.out.println(m_adapter.items);
	//				for (ShortSite site : mState.listSites){
	//					System.out.println(site);
	//					m_adapter.add(site);
	//				}
	//			}
	//			
	////			m_ProgressDialog.dismiss();
	//			m_adapter.notifyDataSetChanged();
	//		}
	//	};

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mState.mReceiver.clearReceiver();
		return mState;
	}

	//Update the SiteAdapter
	private Runnable returnRes = new Runnable() {
		public void run() {
			m_adapter.items.clear();
			if(mState.listSites != null && mState.listSites.size() > 0){
				m_adapter.notifyDataSetChanged();
				for (ShortSite s : mState.listSites) 
					m_adapter.add(s);
				m_adapter.notifyDataSetChanged();
			}
		}
	};
	protected void onPause() {
		mState.mReceiver.clearReceiver();
		super.onPause();
	}
	@Override
	protected void onResume() {
		mState.mReceiver.setReceiver(this);
		MenuHelper.actionBar=actionBar;
//		ServiceHelper.getInstance(this);
		MenuHelper.actionBar.setHomeAction(onRefreshClick);
		
		if (preferences.getBoolean(UPDATE_ALWAYS, false))
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
			Toast.makeText(ExploreTabSelectionActivity.this, resultData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}
}