package android.ui.pref;


import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
public class AdPreference extends Preference {
	public AdPreference(Context context, AttributeSet attrs, int defStyle) {super    (context, attrs, defStyle);}
	public AdPreference(Context context, AttributeSet attrs) {super(context, attrs);}
	public AdPreference(Context context) {super(context);}

	@Override
	protected View onCreateView(ViewGroup parent) {
		// this will create the linear layout defined in ads_layout.xml
		View view = super.onCreateView(parent);		
		return view;    
	}
}
