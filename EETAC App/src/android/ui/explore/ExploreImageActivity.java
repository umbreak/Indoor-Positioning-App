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
import android.ui.adapters.CommentAdapter;
import android.ui.adapters.ImageAdapter;
import android.ui.pojos.Comment;
import android.ui.pojos.Site;
import android.utils.ActualSite;
import android.utils.MenuHelper;
import android.utils.MyResultReceiver;
import android.utils.ServiceHelper;
import android.utils.ToolKit;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
/**
 * The layout of a concrete Picture in a concrete Site. 
 * It is expecting: site_id=...getInt(GET_SITE); site_name=...getString(SITE_VALUE); picture_id=...getInt(GET_PICTURE)
 * Uses the CommentAdapter as a bridge.
 * Performs the call of GET_COMMENTS_PICTURE action on GETService,
 * Performs the call of PUT_COMMENT_PICTURE action on PUTService
 * and handle the response by implementing onReceiveResult.*/


public class ExploreImageActivity extends ListActivity implements MyResultReceiver.Receiver{
	private CommentAdapter m_adapter;
	private ArrayList<Comment> listComments;
	private TextView textComments;
	private InputMethodManager imm;
	private MenuHelper helper;
	private ImageAdapter im_adapter;
	private Gallery gallery;
	private State mState;
	private SharedPreferences preferences;
	private RefreshClick onRefreshClick;
	private int picture_id;

	public ExploreImageActivity(){
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
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.explore_image);

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;


		onRefreshClick = new RefreshClick();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			ToolKit.updateRefreshStatus(mState.mSyncing);
		} else {
			mState = new State();
			//Receive the integer of the Site we want to display.
			mState.site.id=getIntent().getExtras().getInt(SITE_VAULE);
			System.out.println("ID del site: " + mState.site.id);
			mState.mReceiver.setReceiver(this);
			if (!preferences.getBoolean(UPDATE_ALWAYS, false))
				onRefreshClick.performAction(null);
		}

		textComments=(EditText)findViewById(R.id.addTextComment);
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

		setTitle("Explore Images " + ActualSite.site.name);
		if (mState.site.checkin_id >= 0){
			findViewById(R.id.linearLayoutAddComment).setVisibility(View.VISIBLE);
		}
		else
			findViewById(R.id.linearLayoutAddComment).setVisibility(View.GONE);

		MenuHelper.setActionBar(this, MenuHelper.ACTIONBAR_EXPLORE_2);

		//Gallery of images.
		gallery = (Gallery) findViewById(R.id.galleryImage);
		im_adapter=new ImageAdapter(this,mState.site.pictures,375,250);
		gallery.setAdapter(im_adapter);

		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				ImageView image=(ImageView)view;				
				picture_id=(Integer)image.getTag();
				ServiceHelper.startAction(GET_COMMENTS_PICTURE, String.valueOf(mState.site.id), String.valueOf(picture_id), mState.mReceiver, getApplicationContext());
				//**** GET LIST OF COMMENTS
			}

			public void onNothingSelected(AdapterView<?> parent) {

			}
		});


		listComments = new ArrayList<Comment>();
		getListView().setItemsCanFocus(true);
		this.m_adapter = new CommentAdapter(this, R.layout.comments, listComments);
		setListAdapter(this.m_adapter);

		textComments.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
					addComment();
					return true;
				}
				return false;
			}
		});

		gallery.setSelection(getIntent().getExtras().getInt(PICTURE_URL), true);
		System.out.println("PICTURE POSITION: " + getIntent().getExtras().getInt(PICTURE_URL) + " PICTURE ID: " + getIntent().getExtras().getInt(PICTURE_VALUE));
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
		MenuHelper.actionBar=(ActionBar) findViewById(R.id.actionbar);
		if (preferences.getBoolean(UPDATE_ALWAYS, false))
			onRefreshClick.performAction(null);
		super.onResume();
	}
	public void onClick(View v) {
		if (v.getId() == R.id.enterComment)
			addComment();
	}
	/*Perform the actions to add a comment
	1) Hide the keyboard;
	2) Check if the text area is not null; 
	3) get the Username from the Shared Preferences;
	4) Create a Bundle with the comment object;
	5) Start the Service for adding a comment.
	 */
	private void addComment(){
		imm.hideSoftInputFromWindow(textComments.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);	
		if (!textComments.getText().toString().equals("")){
			Comment c= new Comment(PreferenceManager.getDefaultSharedPreferences(this).getString(PREFS_USER, ""),textComments.getText().toString(), "", 0);
			ToolKit.updateRefreshStatus(mState.mSyncing);
			Bundle b= new Bundle();
			b.putParcelable(COMMENT_VAULE, c);
			ServiceHelper.startAction(PUT_COMMENT_PICTURE, String.valueOf(mState.site.id), String.valueOf(String.valueOf(picture_id)), b,mState.mReceiver,getApplicationContext());
		}
	}
	//Update the CommentAdapter
	private Runnable returnRes = new Runnable() {
		public void run() {
			m_adapter.items.clear();
			if(mState.site.comments != null && mState.site.comments.size() > 0){
				m_adapter.notifyDataSetChanged();
				for (Comment c : mState.site.comments) 
					m_adapter.add(c);
				m_adapter.notifyDataSetChanged();
			}
		}
	};

	private class RefreshClick extends AbstractAction {

		public RefreshClick() {
			super(R.drawable.ic_menu_mapmode);
		}
		public void performAction(View view) {
			ToolKit.updateRefreshStatus(mState.mSyncing);
			ServiceHelper.startAction(GET_SITE, String.valueOf(mState.site.id), mState.mReceiver, getApplicationContext());
		}

	}
	//Add the comment if the resultCode is positive (the resultCode is the comment_id)
	private void updateAddComment(Comment c){
		if(c.id >=0 -1){
			mState.site.setComment(c);
			m_adapter.add(c);
			m_adapter.notifyDataSetChanged();

			Toast.makeText(ExploreImageActivity.this, "Comment added succesfully.", Toast.LENGTH_SHORT).show();
		}else
			Toast.makeText(ExploreImageActivity.this, "Comment not added, communication error with the server.", Toast.LENGTH_SHORT).show();

		textComments.setText("");
	}

	//Static content
	private static class State {
		public MyResultReceiver mReceiver;
		public Site site;
		public boolean mSyncing = false;
		private State() {
			mReceiver = new MyResultReceiver(new Handler());
			site=ActualSite.site;
		}
	}

	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case STATUS_RUNNING: {
			mState.mSyncing = true;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}

		case STATUS_FINISHED_COMMENTS_PIC_GET: {
			Comment[] c= (Comment[])resultData.getParcelableArray(GET_COMMENTS_PICTURE);

			m_adapter.notifyDataSetChanged();
			mState.site.comments.clear();
			mState.site.comments.addAll(Arrays.asList(c));	
			runOnUiThread(returnRes);
			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}
		case STATUS_FINISHED_COMMENT: {
			Comment c=(Comment)resultData.getParcelable(COMMENT_VAULE);
			updateAddComment(c);
			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}
		case STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			Toast.makeText(this, resultData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}

}