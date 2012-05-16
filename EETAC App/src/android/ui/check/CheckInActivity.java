package android.ui.check;

import static android.utils.Actions.GET_SITE;

import com.markupartist.android.widget.ActionBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.ui.R;
import android.ui.BaseActivity;
import android.ui.barcode.IntentIntegrator;
import android.ui.barcode.IntentResult;
import android.ui.explore.ExploreLargeActivity;
import android.util.Log;
import android.utils.MenuHelper;

/**
 * Checkin layout. Start the Barcode Scanner and handle for the result.
 * INFO: http://stackoverflow.com/questions/4443891/how-to-read-barcodes-with-the-camera-on-android
 * INFO: http://code.google.com/p/zxing/wiki/ScanningViaIntent */

public class CheckInActivity extends BaseActivity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.checkin);
		MenuHelper.setActionBar(this, MenuHelper.ACTIONBAR_CHECKIN);
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan();

	}
	protected void onResume() {
		MenuHelper.actionBar=(ActionBar) findViewById(R.id.actionbar);
		super.onResume();
	}
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {  
		  switch (requestCode) {
		  case IntentIntegrator.REQUEST_CODE:
		     if (resultCode == Activity.RESULT_OK) {

		        IntentResult intentResult = 
		           IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

		        if (intentResult != null) {

		           String contents = intentResult.getContents();
		           Intent i;
					i= new Intent(this, ExploreLargeActivity.class);
					i.putExtra(GET_SITE, Integer.valueOf(contents.substring(contents.lastIndexOf('=') + 1)));
					
		           String format = intentResult.getFormatName();

		           Log.d("SEARCH_EAN", "OK, EAN: " + contents + ", FORMAT: " + format);
		           startActivity(i);
		        } else {
		           Log.e("SEARCH_EAN", "IntentResult je NULL!");
		        }
		     } else if (resultCode == Activity.RESULT_CANCELED) {
		        Log.e("SEARCH_EAN", "CANCEL");
		     }
		  }
		}
}