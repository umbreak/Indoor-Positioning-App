package android.ui.adapters;

import static android.utils.Actions.GET_SITE;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.ui.R;
import android.ui.explore.ExploreLargeActivity;
import android.ui.map.MapActivity;
import android.ui.pojos.ShortSite;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SiteAdapter extends ArrayAdapter<ShortSite>{

	public ArrayList<ShortSite> items;
	private Context context;
	private View tmpView=null;

	public SiteAdapter(Context context, int textViewResourceId, ArrayList<ShortSite> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context=context;
	}
	
	private static class ViewHolderSite{
		public TextView name;
		public TextView description;
		public TextView num_comments;
		public TextView num_checkins;
		public ImageView image;
		public Button button;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolderSite holder;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.explore_site, null);
			holder = new ViewHolderSite();
			holder.name=(TextView) v.findViewById(R.id.explore_site_name);
			holder.description=(TextView) v.findViewById(R.id.explore_site_description);
			holder.num_comments=(TextView) v.findViewById(R.id.explore_site_comments);
			holder.num_checkins=(TextView) v.findViewById(R.id.explore_site_checkins);
			holder.image=(ImageView) v.findViewById(R.id.avatar);
			holder.button=(Button) v.findViewById(R.id.goButton);
			v.setClickable(true);
			v.setFocusable(true);
			v.setBackgroundResource(android.R.drawable.menuitem_background);
			v.setTag(holder);
			
		}else
			holder=(ViewHolderSite)v.getTag();
		
		ShortSite o = items.get(position);
		if (o != null) {
			if (o.avatar == 0) holder.image.setBackgroundResource(R.drawable.unknown);
			else if (o.avatar == 1) holder.image.setBackgroundResource(R.drawable.site);
			else if (o.avatar == 2) holder.image.setBackgroundResource(R.drawable.lab);
			else if (o.avatar == 3) holder.image.setBackgroundResource(R.drawable.lib);
			else holder.image.setBackgroundResource(R.drawable.unknown);
			holder.name.setText(o.name);
			holder.description.setText(o.description);
			holder.num_comments.setText(o.num_comments + " comments");
			holder.num_checkins.setText(o.num_checkins + " checkins");
			OnItemClickListener listener=new OnItemClickListener(position);
			holder.button.setOnClickListener(listener);
			v.setOnClickListener(listener);
		}
		return v;
	}
	private class OnItemClickListener implements OnClickListener{           
		private int mPosition;
		OnItemClickListener(int position){
			mPosition = position;
		}
		public void onClick(View arg0) {
			Intent i;
//			arg0.setBackgroundColor(Color.LTGRAY);
			if (arg0.getId() == R.id.goButton){
				i =new Intent(context, MapActivity.class);
				i.putExtra(GET_SITE, items.get(mPosition));
				context.startActivity(i);
			}else{
				i= new Intent(context, ExploreLargeActivity.class);
				i.putExtra(GET_SITE, items.get(mPosition).id);
				context.startActivity(i);
			}
		}               
	}
}

