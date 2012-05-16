package android.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Custom Dialog to show a comment (2sec). */

public class CustomDialog {
	private CustomDialog(){}

	public static void showDialog(Context context,String tittle, String message, int type){
		Handler mHandler = new Handler();
		final Dialog dialog = new Dialog(context);

		dialog.setContentView(R.layout.custom_dialog);
		dialog.setTitle(tittle);

		TextView text = (TextView) dialog.findViewById(R.id.text);
		text.setText(message);
		ImageView image = (ImageView) dialog.findViewById(R.id.image);
		if (type == 0)
			image.setImageResource(android.R.drawable.sym_action_chat);
		else
			image.setImageResource(android.R.drawable.ic_dialog_info);

		dialog.show();
		mHandler.postDelayed(new Runnable() {

			public void run() {
				// TODO Auto-generated method stub

				dialog.dismiss();  

			}
		},2000L); // this will dismiss the dialog after 20 Sec. set as per you 

	}
}
