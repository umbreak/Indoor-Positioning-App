package android.ui.image;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import static android.utils.Actions.*;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.ui.R;
import android.ui.explore.ExploreLargeActivity;
import android.util.Log;
import android.utils.MyResultReceiver;
import android.utils.ServiceHelper;
import android.utils.ToolKit;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageManager implements MyResultReceiver.Receiver{

	private static final String TAG = "ImageManager";
	public MemoryCache memoryCache=new MemoryCache();
	private Map<String, ImageView> imageViews=Collections.synchronizedMap(new WeakHashMap<String, ImageView>());


	//	private HashMap<String, SoftReference<Bitmap>> imageMap = new HashMap<String, SoftReference<Bitmap>>();
	private MyResultReceiver receiver;
	private String short_url;
	private Context context;

	public ImageManager(Context c) {
		super();
		context=c;
		//Creating Directory for saving images
		receiver=new MyResultReceiver(new Handler());
		receiver.setReceiver(this);
	}
	public void displayImage(String url,ImageView imageView) {
		short_url=url.substring(url.lastIndexOf('/') + 1);
		Bitmap bitmap=null;
		//If the Bitmap is in memory, set it actually
		imageViews.put(url, imageView);
		bitmap=memoryCache.get(url);
		if(bitmap!=null){
			Log.d(TAG, "Get Bitmap from cache " + short_url);
			setAndResizeCorrectly(imageView,bitmap);
		}

		else{
			//If the bitmap is in the cache, retrieve it and set it actually.
			bitmap=ToolKit.i.getBitmap(short_url);
			if (bitmap != null){
				Log.d(TAG, "Get Bitmap from SD card " + short_url);
				memoryCache.put(url, bitmap);

				setAndResizeCorrectly(imageView,bitmap);
			}
			//if not, request it to the server.
			else{
				Log.d(TAG, "Get Bitmap from Internet " + short_url);
				ServiceHelper.startAction(GET_PICTURE, "0", url, receiver, context);
				imageView.setImageResource(R.drawable.downloading);
			}
		}
	}

	public void onReceiveResult(int resultCode, Bundle resultData) {
		switch (resultCode) {
		case STATUS_FINISHED_PICTURE_GET: { 
			Bitmap picture=resultData.getParcelable(PICTURE_VALUE);
			String url=resultData.getString(PICTURE_URL);
			if (picture != null)
				memoryCache.put(url, picture);		
			ImageView view=imageViews.get(url);
			BitmapDisplayer bd=new BitmapDisplayer(picture, view);
			Activity a=(Activity)view.getContext();
			a.runOnUiThread(bd);
			break;
		}
		case STATUS_ERROR: {
			// Error happened down in SyncService, show as toast.
			Toast.makeText(context, resultData.getString(Intent.EXTRA_TEXT), Toast.LENGTH_LONG).show();
			break;
		}
		}		
	}
	private void setAndResizeCorrectly(ImageView view, Bitmap bitmap){
		view.setImageBitmap(bitmap);
		float new_x=view.getLayoutParams().height*bitmap.getWidth()/bitmap.getHeight();
		if (view.getLayoutParams().width != Math.round(new_x))
			view.setLayoutParams(new Gallery.LayoutParams(Math.round(new_x), view.getLayoutParams().height));
	}
	private class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;

		public BitmapDisplayer(Bitmap b, ImageView i) {
			bitmap=b;
			imageView=i;
		}
		public void run() {
			if(bitmap != null){
				setAndResizeCorrectly(imageView,bitmap);
			}else
				imageView.setImageResource(R.drawable.downloading);
		}
	}
}
