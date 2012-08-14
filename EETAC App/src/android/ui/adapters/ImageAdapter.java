package android.ui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.ui.R;
import android.ui.image.ImageManager;
import android.ui.pojos.Picture;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
/**
 * Adaptar for the Gallery of images (ids on mImageIds).*/

public class ImageAdapter extends BaseAdapter {
	int mGalleryItemBackground;
	private Context mContext;
	public ArrayList<Picture> pictures;
	public ImageManager imageManager; 
	private int x,y;

	public ImageAdapter(Context context, ArrayList<Picture> pictures, int x, int y) {
		super();
		this.x=x;
		this.y=y;
		this.pictures=pictures;
		mContext = context;
		TypedArray attr = mContext.obtainStyledAttributes(R.styleable.ExploreLargeActivity);
		mGalleryItemBackground = attr.getResourceId(
				R.styleable.ExploreLargeActivity_android_galleryItemBackground, 0);
		attr.recycle();
		imageManager= new ImageManager(context.getApplicationContext());
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		final Picture picture = pictures.get(position);
//		if  (picture!= null)
//			if (picture.main) return null;
		
        if (convertView == null) {  // If it's not recycled, initialize some attributes
        	imageView=new ImageView(mContext);
        	imageView.setLayoutParams(new Gallery.LayoutParams(x, y));
        	imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        	imageView.setBackgroundResource(mGalleryItemBackground);
//        	notifyDataSetChanged();
        	
        }else
        	imageView = (ImageView) convertView;
        

		
		if (picture != null) {
			imageView.setTag(picture.id);
			imageManager.displayImage(picture.route_image, imageView);

		}
		return imageView;
	}

	public int getCount() {
		return pictures.size();
	}

	public Object getItem(int arg0) {
		return pictures.get(arg0);
	}

	public long getItemId(int arg0) {
		return arg0;
	}

}
