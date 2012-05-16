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
import android.ui.R;
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

public class ExploreFavoritesActivity extends ListActivity implements MyResultReceiver.Receiver{

	private SiteAdapter m_adapter;
	private Intent longSiteIntent;
	private State mState;
	private ActionBar actionBar;
	private ArrayList<Integer> favorites;
	private SharedPreferences prefs;
	private SharedPreferences.Editor editPrefs;
	private final Gson gson= new Gson();
	private RefreshClick onRefreshClick;
	private Context context;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_view);
		actionBar=MenuHelper.actionBar;
		context=this;

		favorites=new ArrayList<Integer>();	
		
		mState = (State) getLastNonConfigurationInstance();
		onRefreshClick = new RefreshClick();
		MenuHelper.actionBar.setHomeAction(onRefreshClick);
		
		//Get list of favorites	
		prefs=getSharedPreferences(PREFS_FILE, 0);
		editPrefs=prefs.edit();
		String result=prefs.getString(PREFS_FAVORITES, "");
		if (!result.isEmpty()){
			favorites=gson.fromJson(result,new TypeToken<ArrayList<Integer>>() {}.getType());
			Log.d("ExploreFavoritesActivity", "Favorites sites: " + favorites.toString());
		}
		

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
			super(R.drawable.icon);
			type= new Bundle();
			type.putBoolean(SITE_SHORT, true);

		}
		public void performAction(View view) {
			ToolKit.updateRefreshStatus(mState.mSyncing);
			mState.listSites.clear();
			
			for (Integer site_id: favorites){
				ServiceHelper.startAction(GET_SITE, String.valueOf(site_id), type, mState.mReceiver,context);
			}
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
	protected void onResume() {
		MenuHelper.actionBar=actionBar;
		MenuHelper.actionBar.setHomeAction(onRefreshClick);
		
		//Get list of favorites	
		String result=prefs.getString(PREFS_FAVORITES, "");
		if (!result.isEmpty()){
			favorites=gson.fromJson(result,new TypeToken<ArrayList<Integer>>() {}.getType());
			Log.d("ExploreFavoritesActivity", "Favorites sites: " + favorites.toString());
		}
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
			mState.listSites.add(ActualSite.shortSite);
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
			Toast.makeText(ExploreFavoritesActivity.this, resultData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}

	private class SiteAdapter extends ArrayAdapter<ShortSite>{

		public ArrayList<ShortSite> items;

		public SiteAdapter(Context context, int textViewResourceId, ArrayList<ShortSite> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.explore_site, null);
			}
			v.setClickable(true);
			v.setFocusable(true);
			v.setBackgroundResource(android.R.drawable.menuitem_background);
			ShortSite o = items.get(position);
			if (o != null) {

				((TextView) v.findViewById(R.id.explore_site_name)).setText(o.name);
				((TextView) v.findViewById(R.id.explore_site_description)).setText(o.description);
				((TextView) v.findViewById(R.id.explore_site_comments)).setText(o.num_comments + " comments");
				((TextView) v.findViewById(R.id.explore_site_checkins)).setText(o.num_checkins + " checkins");
				//				((ImageView) v.findViewById(R.id.avatar)).setImageResource(o.avatar);
				((ImageView) v.findViewById(R.id.avatar)).setImageResource(R.drawable.app_notes);
				Button b=(Button) v.findViewById(R.id.goButton);
				OnItemClickListener listener=new OnItemClickListener(position);
				v.setOnClickListener(listener);
				b.setOnClickListener(listener);
			}
			return v;
		}
		private class OnItemClickListener implements OnClickListener{           
			private int mPosition;
			OnItemClickListener(int position){
				mPosition = position;
			}
			public void onClick(View arg0) {
				if (arg0.getId() == R.id.goButton){
					Intent i =new Intent(getBaseContext(), MapActivity.class);
					i.putExtra(GET_SITE, mState.listSites.get(mPosition));
					startActivity(i);
				}else{
					longSiteIntent= new Intent(getBaseContext(), ExploreLargeActivity.class);
					//				Bundle extras= new Bundle();
					//				extras.putParcelable("SITE", listSites.get(mPosition));
					longSiteIntent.putExtra(GET_SITE, mState.listSites.get(mPosition).id);
					startActivity(longSiteIntent);
				}
			}               
		}

	}
}