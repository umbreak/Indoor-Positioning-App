package android.ui.explore;

import static android.utils.Actions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.ui.CustomDialog;
import android.ui.R;
import android.ui.adapters.CommentAdapter;
import android.ui.adapters.ImageAdapter;
import android.ui.adapters.CommentAdapter.ViewHolderComment;
import android.ui.adapters.OnCustomClickListener;
import android.ui.pojos.Comment;
import android.ui.pojos.Picture;
import android.ui.pojos.Site;
import android.ui.restclient.Processor;
import android.ui.twitter.TwitterUtils;
import android.util.Log;
import android.utils.ActualSite;
import android.utils.MenuHelper;
import android.utils.MyResultReceiver;
import android.utils.ServiceHelper;
import android.utils.ToolKit;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;

/**
 * The layout of a concrete Site.  The id of the site has to be obtained from the intent ...getInt(GET_SITE)
 * Uses the CommentAdapter as a bridge.
 * Performs the call of GET_SITE action on GETService,
 * Performs the call of PUT_COMMENT action on PUTService
 * and handle the response by implementing onReceiveResult.*/

public class ExploreLargeActivity extends ListActivity implements OnClickListener, MyResultReceiver.Receiver, OnCustomClickListener{
	@Override
	protected void onPause() {
		mState.mReceiver.clearReceiver();
		super.onPause();
	}
	final private String TAG="ExploreLargeActivity";
	private CommentAdapter m_adapter;
	private EditText textComments;
	private InputMethodManager imm;
	private Intent imageIntent;
	private MenuHelper helper;
	private State mState;
	private RefreshClick onRefreshClick;
	private String currentPhotoPath;
	private ToggleButton checkButton;
	private SharedPreferences preferences;
	private ImageAdapter im_adapter;
	private Gallery gallery;
	private ImageView avatar;

	public ExploreLargeActivity(){
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
		setContentView(R.layout.explore_large_desc);

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;


		onRefreshClick = new RefreshClick();
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		textComments=(EditText)findViewById(R.id.addTextComment);
		avatar = (ImageView)findViewById(R.id.avatar);
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		checkButton = (ToggleButton) findViewById(R.id.checkButton);

		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			im_adapter=new ImageAdapter(this,mState.site.pictures,150,100);
			ToolKit.updateRefreshStatus(mState.mSyncing);
			fillShortSite();
		} else {
			mState = new State();
			im_adapter=new ImageAdapter(this,mState.site.pictures,150,100);
			//Receive the integer of the Site we want to display.
			mState.site.id=getIntent().getExtras().getInt(GET_SITE);
			mState.mReceiver.setReceiver(this);
			if (!preferences.getBoolean(UPDATE_ALWAYS, false))
				onRefreshClick.performAction(null);
			mState.site.checkin_id=-1;
			setTitle("Explore");
		}

		MenuHelper.setActionBar(this, MenuHelper.ACTIONBAR_EXPLORE_2);
		MenuHelper.actionBar.setHomeAction(onRefreshClick);


		//Gallery of images.
		gallery = (Gallery) findViewById(R.id.gallery);
		gallery.setAdapter(im_adapter);

		gallery.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				//				Toast.makeText(ExploreLargeActivity.this, "" + position, Toast.LENGTH_SHORT).show();
				imageIntent = new Intent(getBaseContext(), ExploreImageActivity.class);
				imageIntent.putExtra(SITE_VAULE, mState.site.id);
				//				imageIntent.putExtra(SITE_VAULE, mState.site.name);
				ImageView image=(ImageView)v;
				ActualSite.site=mState.site;
				//PICTURE_ID
				imageIntent.putExtra(PICTURE_URL,position);
				//PICTURE POSITION
				imageIntent.putExtra(PICTURE_VALUE,(Integer)image.getTag() );
				startActivity(imageIntent);

			}
		});

		getListView().setItemsCanFocus(true);
		m_adapter = new CommentAdapter(preferences.getInt(PREFS_USER_ID, 0), this,R.layout.comments ,mState.site.comments, this);
		setListAdapter(this.m_adapter);

		textManagment();
		favoriteButtonManagment();
	}

	private void textManagment(){
		//Listener for the area of a text comment. When ENTER is pressed, call addComment() method
		textComments.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
						(keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					addComment();
					return true;
				}
				return false;
			}
		});
	}

	private void favoriteButtonManagment(){
		final ToggleButton favoriteButton = (ToggleButton) findViewById(R.id.favoriteButton);
		final Gson gson= new Gson();

		//Get the list of favorite sites from Shared Preferences
		final SharedPreferences.Editor editPrefs=preferences.edit();

		String result=preferences.getString(PREFS_FAVORITES, "");
		if (!result.isEmpty()){
			ArrayList<Integer> favorites=new ArrayList<Integer>();		
			favorites=gson.fromJson(result,new TypeToken<ArrayList<Integer>>() {}.getType());
			Log.d(TAG, "Favorites sites: " + favorites.toString());
			if (favorites.contains(mState.site.id))
				favoriteButton.setChecked(true);
		}

		//Listener for the favorite toogleButton
		favoriteButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Gson gson=new Gson();
				ArrayList<Integer> fav=new ArrayList<Integer>();
				String result=preferences.getString(PREFS_FAVORITES, "");
				if (!result.isEmpty())
					fav=gson.fromJson(preferences.getString(PREFS_FAVORITES, ""),new TypeToken<ArrayList<Integer>>() {}.getType());

				if (favoriteButton.isChecked()) {
					Toast.makeText(ExploreLargeActivity.this, "Site added to favorites", Toast.LENGTH_SHORT).show();
					fav.add(mState.site.id);

				} else {
					Toast.makeText(ExploreLargeActivity.this, "Site deleted from favorites.", Toast.LENGTH_SHORT).show();
					fav.remove(new Integer(mState.site.id));
				}

				editPrefs.putString(PREFS_FAVORITES, gson.toJson(fav));
				editPrefs.commit();
			}
		});
	}


	@Override
	protected void onResume() {
		ToolKit.i.recentsQueue.offer(mState.site.id);
		mState.mReceiver.setReceiver(this);
		MenuHelper.actionBar=(ActionBar) findViewById(R.id.actionbar);
		if (preferences.getBoolean(UPDATE_ALWAYS, false))
			onRefreshClick.performAction(null);
		super.onResume();
	}
	//Fill the ShortSite name and description when we have the site info.
	private void fillShortSite(){
		setTitle("Explore (" + mState.site.name + ")");
		((TextView) findViewById(R.id.siteName)).setText(mState.site.name);
		((TextView) findViewById(R.id.siteDesc)).setText(mState.site.description);
		if (!mState.site.route_image.isEmpty())
			if (mState.bitmap != null)
				setAndResizeCorrectly(avatar,mState.bitmap);
			else
				ServiceHelper.startAction(GET_PICTURE, "0", mState.site.route_image, mState.mReceiver, getApplicationContext());

		if (mState.site.checkin_id > 0){
			findViewById(R.id.commentLayout).setVisibility(View.VISIBLE);
			findViewById(R.id.imageButton).setEnabled(true);
			((ToggleButton) findViewById(R.id.checkButton)).setChecked(true);
		}
		else
			((ToggleButton) findViewById(R.id.checkButton)).setChecked(false);
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		// Clear any strong references to this Activity, we'll reattach to
		// handle events on the other side.
		mState.mReceiver.clearReceiver();
		return mState;
	}

	//Static content
	private static class State {
		public MyResultReceiver mReceiver;
		public Site site;
		public Bitmap bitmap;
		public boolean mSyncing = false;
		private State() {
			mReceiver = new MyResultReceiver(new Handler());
			site=new Site();
			bitmap=null;
		}
	}

	private class RefreshClick extends AbstractAction {

		public RefreshClick() {
			super(R.drawable.ic_title_refresh_default);
		}
		public void performAction(View view) {
			ToolKit.updateRefreshStatus(mState.mSyncing);
			ServiceHelper.startAction(GET_SITE, String.valueOf(mState.site.id), mState.mReceiver, getApplicationContext());
		}

	}

	public void onClick(View v) {
		if (v.getId() == R.id.enterComment)
			addComment();	

		else if (v.getId() == R.id.checkButton)
			if (checkButton.isChecked())
				ServiceHelper.startAction(PUT_CHECKIN, String.valueOf(mState.site.id), mState.mReceiver,getApplicationContext());
			else{
				Bundle b= new Bundle();
				b.putInt(CHECKIN_ID, mState.site.checkin_id);
				ServiceHelper.startAction(DEL_CHECKIN, String.valueOf(mState.site.id), b,mState.mReceiver,getApplicationContext());
			}

		else if (v.getId() == R.id.imageButton){
			final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			try{
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(setUpPhotoFile())); 
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
				startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			} catch (IOException e) {
				e.printStackTrace();
				currentPhotoPath = null;
			}

		}		
	}

	private File createImageFile() throws IOException {
		// Create an image file name
		String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
		String imageFileName = "IMG_" + timeStamp + "_";
		return File.createTempFile(imageFileName,".jpg", Processor.cache_path);
	}
	private File setUpPhotoFile() throws IOException {	
		File f = createImageFile();
		currentPhotoPath = f.getAbsolutePath();	
		return f;
	}

	//Camera activity result
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				//					final File file = getTempFile(this);
				if (currentPhotoPath != null){

					ToolKit.updateRefreshStatus(mState.mSyncing);
					runOnUiThread(processPicture);

					//					Bitmap captureBmp = Media.getBitmap(getContentResolver(), Uri.fromFile(file) );
				}
				//					Processor.i.postImage(String.valueOf(mState.site.id), compress_file);



			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Take a photo action cancelled", Toast.LENGTH_SHORT);
			} else {
				Toast.makeText(this, "Picture was not taken", Toast.LENGTH_SHORT);
			}
		}
	}

	private void setAndResizeCorrectly(ImageView view, Bitmap bitmap){

		BitmapFactory.Options bmOptions = new BitmapFactory.Options();

		int scaleFactor=Math.round(Math.max((float)bitmap.getWidth()/100, (float)bitmap.getHeight()/100));
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		bitmap=BitmapFactory.decodeFile(ToolKit.i.cacheDir + "/" + mState.site.route_image.substring(mState.site.route_image.lastIndexOf('/') + 1), bmOptions);
		view.setImageBitmap(bitmap);
		im_adapter.notifyDataSetChanged();


		//		if (view.getLayoutParams().height != Math.round(new_y))
		//			view.setLayoutParams(new LinearLayout.LayoutParams(view.getLayoutParams().width, Math.round(new_y)));
	}

	//Add the comment if the resultCode is positive (the resultCode is the comment_id)
	private void updateAddComment(Comment c){
		if(c.id >0){
			mState.site.setComment(c);
			m_adapter.add(c);
			m_adapter.notifyDataSetChanged();

			Toast.makeText(ExploreLargeActivity.this, "Comment added succesfully.", Toast.LENGTH_SHORT).show();
		}else
			Toast.makeText(ExploreLargeActivity.this, "Comment not added, communication error with the server.", Toast.LENGTH_SHORT).show();

		textComments.setText("");
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
			Comment c= new Comment(preferences.getString(PREFS_USER, ""),textComments.getText().toString(), "", 0);
			ToolKit.updateRefreshStatus(mState.mSyncing);
			Bundle b= new Bundle();
			b.putParcelable(COMMENT_VAULE, c);
			ServiceHelper.startAction(PUT_COMMENT, String.valueOf(mState.site.id), b,mState.mReceiver,getApplicationContext());
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

	//Process the picture obtained from the camera
	private Runnable processPicture = new Runnable() {
		public void run() {
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
			/* Set bitmap options to scale the image decode target */
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize=3;
			bmOptions.inPurgeable = true;

			/* Decode the JPEG file into a Bitmap */
			Bitmap bitmap=BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

			String lasCurrentPath=currentPhotoPath;
			currentPhotoPath=currentPhotoPath.replaceFirst("IMG", "sIMG_");
			File compress_file = new File(currentPhotoPath);
			OutputStream stream;
			try {
				stream = new FileOutputStream(compress_file);
				bitmap.compress(CompressFormat.JPEG, 80, stream);
				stream.close();
				new File(lasCurrentPath).delete();
				ServiceHelper.startAction(POST_PICTURE, String.valueOf(mState.site.id), currentPhotoPath,mState.mReceiver,getApplicationContext());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};


	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case STATUS_RUNNING: {
			mState.mSyncing = true;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}

		case STATUS_FINISHED: {
			mState.site=ActualSite.site;
			runOnUiThread(returnRes);
			mState.mSyncing = false;
			mState.bitmap=ToolKit.i.getBitmap(mState.site.route_image.substring(mState.site.route_image.lastIndexOf('/') + 1));
			fillShortSite();
			im_adapter.notifyDataSetChanged();
			im_adapter.pictures=mState.site.pictures;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			gallery.setSelection(Math.abs(mState.site.pictures.size()/2), true);
			im_adapter.notifyDataSetChanged();

			break;
		}
		case STATUS_FINISHED_PICTURE_GET: { 
			mState.mSyncing = false;
			mState.bitmap=resultData.getParcelable(PICTURE_VALUE);
			if (mState.bitmap != null)
				setAndResizeCorrectly(avatar,mState.bitmap);

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
		case STATUS_FINISHED_CHECKIN: {
			mState.site.checkin_id =resultData.getInt(PUT_CHECKIN);
			if (mState.site.checkin_id > 0){
				checkButton.setChecked(true);
				findViewById(R.id.imageButton).setEnabled(true);
				findViewById(R.id.commentLayout).setVisibility(View.VISIBLE);
				if (preferences.getBoolean(TWITTER_NOTIFICATOINS, false))
					try {
						TwitterUtils.i.sendTweet(preferences, "Checkin in site: " + mState.site.name + " (from EETAC App)");
					} catch (Exception e) {	}

			}else if (mState.site.checkin_id==-2){
				checkButton.setChecked(true);
				Toast.makeText(this, "You are already Check-in.", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "Problem while checkin.", Toast.LENGTH_LONG).show();
				checkButton.setChecked(false);
			}
			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}
		case STATUS_FINISHED_CHECKOUT: {
			if (resultData.getBoolean(PUT_CHECKIN)){
				mState.site.checkin_id=-1;
				checkButton.setChecked(false);
				findViewById(R.id.imageButton).setEnabled(false);
				findViewById(R.id.commentLayout).setVisibility(View.GONE);	
				if (preferences.getBoolean(TWITTER_NOTIFICATOINS, false))
					try {
						TwitterUtils.i.sendTweet(preferences, "Checkout in site: " + mState.site.name + " (from EETAC App)");
					} catch (Exception e) {	}
			}else{
				Toast.makeText(this, "Problem while checkout.", Toast.LENGTH_LONG).show();
				checkButton.setChecked(true);
			}
			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}		case STATUS_FINISHED_PICTURE_POST: {
			Picture p=(Picture)resultData.getParcelable(POST_PICTURE);
			if (p.id > 0){
				Toast.makeText(this, "Picture POSTED to the server.", Toast.LENGTH_LONG).show();
				im_adapter.notifyDataSetChanged();
				mState.site.pictures.add(p);
				im_adapter.notifyDataSetChanged();
			}else
				Toast.makeText(this, "Failed to POST picture on server.", Toast.LENGTH_LONG).show();

			//			updateAddComment(c);
			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}
		case STATUS_FINISHED_DEL_COMMENT: {
			int comment_pos=resultData.getInt(COMMENT_POSITION);
			Comment c= m_adapter.getItem(comment_pos);
			m_adapter.notifyDataSetChanged();
			m_adapter.remove(c);
			m_adapter.notifyDataSetChanged();

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
	public void OnCustomClick(View v, int position) {
		if (v.getId() == R.id.deleteButton){
			int comment_id=(Integer)v.getTag();
			Bundle b= new Bundle();
			b.putInt(COMMENT_VAULE, comment_id);
			b.putInt(COMMENT_POSITION, position);
			ServiceHelper.startAction(DEL_COMMENT, String.valueOf(mState.site.id), b, mState.mReceiver, getApplicationContext());		
		}
		else{
			ViewHolderComment holder=(ViewHolderComment)v.getTag();
			CustomDialog.showDialog(this, holder.author.getText().toString(),  holder.text.getText().toString(), 0);
		}

	}
}
