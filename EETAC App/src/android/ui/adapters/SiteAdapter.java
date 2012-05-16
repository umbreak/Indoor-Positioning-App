package android.ui.adapters;

import static android.utils.Actions.GET_SITE;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.ui.R;
import android.ui.explore.ExploreLargeActivity;
import android.ui.map.MapActivity;
import android.ui.pojos.ShortSite;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SiteAdapter extends ArrayAdapter<ShortSite>{

	public ArrayList<ShortSite> items;
	private Context context;

	public SiteAdapter(Context context, int textViewResourceId, ArrayList<ShortSite> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context=context;
	}
	
	private static class ViewHolder{
		public TextView name;
		public TextView description;
		public TextView num_comments;
		public TextView num_checkins;
		public ImageView image;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		ViewHolder holder;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.explore_site, null);
			holder = new ViewHolder();
			holder.name=(TextView) v.findViewById(R.id.explore_site_name);
			holder.description=(TextView) v.findViewById(R.id.explore_site_description);
			holder.num_comments=(TextView) v.findViewById(R.id.explore_site_comments);
			holder.num_checkins=(TextView) v.findViewById(R.id.explore_site_checkins);
			holder.image=(ImageView) v.findViewById(R.id.avatar);

			v.setClickable(true);
			v.setFocusable(true);
			v.setBackgroundResource(android.R.drawable.menuitem_background);
			v.setTag(holder);
			
		}else
			holder=(ViewHolder)v.getTag();

		
		ShortSite o = items.get(position);
		if (o != null) {
			holder.name.setText(o.name);
			holder.description.setText(o.description);
			holder.num_comments.setText(o.num_comments + " comments");
			holder.num_checkins.setText(o.num_checkins + " checkins");
			holder.image.setImageResource(R.drawable.app_notes);
			OnItemClickListener listener=new OnItemClickListener(position);
			v.findViewById(R.id.goButton).setOnClickListener(listener);
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
			if (arg0.getId() == R.id.goButton){
				i =new Intent(context, MapActivity.class);
				i.putExtra(GET_SITE, items.get(mPosition));
				context.startActivity(i);
			}else{
				i= new Intent(context, ExploreLargeActivity.class);
				//				Bundle extras= new Bundle();
				//				extras.putParcelable("SITE", listSites.get(mPosition));
				i.putExtra(GET_SITE, items.get(mPosition).id);
				context.startActivity(i);
			}
		}               
	}
}

