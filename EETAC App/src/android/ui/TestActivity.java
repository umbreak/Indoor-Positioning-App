package android.ui;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;


import static android.utils.Actions.*;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.ui.pojos.Comment;
import android.ui.restclient.Processor;
import android.utils.ActualSite;
import android.utils.MenuHelper;
import android.utils.MyResultReceiver;
import android.utils.ServiceHelper;
import android.utils.ToolKit;
import android.view.View;
import android.widget.Toast;

public class TestActivity extends BaseActivity  implements MyResultReceiver.Receiver{
	private State mState;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkin);
		MenuHelper.setActionBar(this, MenuHelper.ACTIONBAR_CHECKIN);

		mState = (State) getLastNonConfigurationInstance();
		final boolean previousState = mState != null;
		if (previousState) {
			// Start listening for SyncService updates again
			mState.mReceiver.setReceiver(this);
			//            updateRefreshStatus();
			//            reloadNowPlaying(true);

		} else {
			mState = new State();
			mState.mReceiver.setReceiver(this);
			//            onRefreshClick(null);
		}

		Comment c= new Comment("Didac","Prueba de comentario", DateFormat.getDateInstance().format(new Date()), 0);
		ToolKit.updateRefreshStatus(mState.mSyncing);
		Bundle b= new Bundle();
		b.putParcelable(COMMENT_VAULE, c);
		ServiceHelper.startAction(PUT_COMMENT, "2", b,mState.mReceiver,this);
//		ServiceHelper.startAction(GET_SITES, mState.mReceiver);
//		ServiceHelper.startAction(GET_SITE, "22", mState.mReceiver);
		//        System.out.println(Arrays.asList(Processor.i.getSites()));
		//        System.out.println(Processor.i.getSite("1"));
		//        
		//              
		//        Comment c = new Comment("Paco", "Esto es un comentario de prueba de TestActivity", DateFormat.getDateInstance().format(new Date()), "5");
		//        System.out.println(Processor.i.putComment("2", c));
		//        System.out.println(Processor.i.getSiteComments("2"));
	}
	private static class State {
		public MyResultReceiver mReceiver;
		public boolean mSyncing = false;
		private State() {
			mReceiver = new MyResultReceiver(new Handler());
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}

	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case STATUS_RUNNING: {
			mState.mSyncing = true;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}
		case STATUS_FINISHED: {
			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			System.out.println(Arrays.asList(resultData.getParcelableArray((GET_SITES))));
			break;
		}
//		case STATUS_FINISHED: {
//			mState.mSyncing = false;
//			ToolKit.updateRefreshStatus(mState.mSyncing);
//			System.out.println(ActualSite.site);
//			break;
//		}
		case STATUS_FINISHED_COMMENT: {
			Comment c=(Comment)resultData.getParcelable(COMMENT_VAULE);
			int result_status=resultData.getInt(PUT_COMMENT);
			c.id=result_status;
			System.out.println(c);
			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			break;
		}
		case STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			mState.mSyncing = false;
			ToolKit.updateRefreshStatus(mState.mSyncing);
			Toast.makeText(TestActivity.this, resultData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}

}