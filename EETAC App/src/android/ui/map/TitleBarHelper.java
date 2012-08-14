package android.ui.map;
import android.app.Activity;
import android.ui.R;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TitleBarHelper {


	
	public static void enableProgress(Activity v) {
		
		ProgressBar view = (ProgressBar)v.findViewById(R.id.map_loading_progress);
		TextView textView = (TextView)v.findViewById(R.id.title_loading);
		if(view!=null) 
			view.setVisibility(View.VISIBLE);
		if(textView!=null) 
			textView.setVisibility(View.VISIBLE);
		
	}
	public static void disableProgress(Activity v) {

		ProgressBar view = (ProgressBar)v.findViewById(R.id.map_loading_progress);
		TextView textView = (TextView)v.findViewById(R.id.title_loading);

		if(view!=null) 
			view.setVisibility(View.GONE);
		if(textView!=null) 
			textView.setVisibility(View.GONE);
		
	}
	/*
	public static void setTitle(Activity v, String title) {
		TextView view = (TextView)v.findViewById(R.id.title_text);
		if(view!=null) {
			view.setText(title);
		}
	}
	*/
}