package android.ui.adapters;

import static android.utils.Actions.PREFS_USER;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.ui.R;
import android.ui.pojos.Comment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
/**
 * Adaptar for the ListView of Comments. 
 * It uses the layout comments.xml */

public class CommentAdapter extends ArrayAdapter<Comment>{

	public ArrayList<Comment> items;
	private Context context;
	private String userName;
	private OnCustomClickListener callback;
	private int user_id;

	public CommentAdapter(int user_id,Context context, int textViewResourceId, ArrayList<Comment> items, OnCustomClickListener callback) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.callback=callback;
		this.context=context;
		this.user_id=user_id;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		userName=prefs.getString(PREFS_USER, "");
	}

	public static class ViewHolderComment{
		public TextView author;
		public TextView text;
		public TextView date;
		public Button delete;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolderComment holder;
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(R.layout.comments, null);
			holder = new ViewHolderComment();
			holder.author=(TextView) convertView.findViewById(R.id.author);
			holder.text=(TextView) convertView.findViewById(R.id.comment_text);
			holder.date=(TextView) convertView.findViewById(R.id.comment_date);
			holder.delete=(Button) convertView.findViewById(R.id.deleteButton);
			convertView.setTag(holder);
			convertView.setBackgroundResource(android.R.drawable.menuitem_background);
			convertView.setClickable(true);
			convertView.setFocusable(true);
		}
		else
			holder=(ViewHolderComment)convertView.getTag();
		Comment o = items.get(position);
		if (o != null) {
			holder.author.setText(o.author + " says: ");
			holder.text.setText(o.text);
			holder.date.setText(o.date);
			holder.delete.setTag(o.id);
			if (o.user_id == user_id) holder.delete.setVisibility(View.VISIBLE);
			else holder.delete.setVisibility(View.GONE);
			CustomOnClickListener listener= new CustomOnClickListener(callback, position);

			holder.delete.setOnClickListener(listener);
			convertView.setOnClickListener(listener);
		}
		return convertView;
	}
}