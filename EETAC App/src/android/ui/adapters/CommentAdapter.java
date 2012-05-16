package android.ui.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.ui.CustomDialog;
import android.ui.R;
import android.ui.pojos.Comment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
/**
 * Adaptar for the ListView of Comments. 
 * It uses the layout comments.xml */

public class CommentAdapter extends ArrayAdapter<Comment>{

	public ArrayList<Comment> items;
	private Context context;

	public CommentAdapter(Context context, int textViewResourceId, ArrayList<Comment> items) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context=context;
	}

	private static class ViewHolder{
		public TextView author;
		public TextView text;
		public TextView date;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.comments, null);
			holder = new ViewHolder();
			holder.author=(TextView) convertView.findViewById(R.id.author);
			holder.text=(TextView) convertView.findViewById(R.id.comment_text);
			holder.date=(TextView) convertView.findViewById(R.id.comment_date);
			convertView.setTag(holder);
			convertView.setBackgroundResource(android.R.drawable.menuitem_background);
		}
		else
			holder=(ViewHolder)convertView.getTag();
		//			v.setClickable(true);
		//			v.setFocusable(true);
		Comment o = items.get(position);
		if (o != null) {
			holder.author.setText(o.author + " says: ");
			holder.text.setText(o.text);
			holder.date.setText(o.date);
			convertView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					ViewHolder holder=(ViewHolder)v.getTag();
					CustomDialog.showDialog(context, holder.author.getText().toString(),  holder.text.getText().toString(), 0);
				}
			});
		}
		return convertView;
	}
}
