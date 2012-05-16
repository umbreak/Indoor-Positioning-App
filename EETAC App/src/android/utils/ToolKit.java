package android.utils;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.view.View;
import android.widget.Gallery;
import android.widget.ImageView;
//Class with static methods that could be reached from any Acitivity, if it's needed.
public enum ToolKit {
	i;
	public Context context;
	private MessageDigest digest=null;
	public File cacheDir;
	private ToolKit(){
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
	}

	//	private static String LOG = ToolKit.class.getName();

	public boolean isInternetAvailable() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null)
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		else
			return false;
	}
	public static void updateRefreshStatus(boolean updating){
		MenuHelper.actionBar.setProgressBarVisibility(updating ? View.VISIBLE : View.GONE);
	}
	public File getDir(){
		File path;
		String sdState = android.os.Environment.getExternalStorageState();
		if (sdState.equals(android.os.Environment.MEDIA_MOUNTED)) {
			path = new File( Environment.getExternalStorageDirectory(), context.getPackageName() );
		}else
			path = context.getCacheDir();
		
		if(!path.exists()){
			path.mkdir();
		}
		return path;
	}
	public Bitmap getBitmap(String filename) {
		File f = new File(cacheDir, filename);
		Bitmap bitmap = BitmapFactory.decodeFile(f.getPath());
		return bitmap;

	}
	
	public String getHash(String password) {
		digest.reset();
		byte[] data=digest.digest(password.getBytes());
		return bin2hex(data);
	}
	static String bin2hex(byte[] data) {
		return String.format("%0" + (data.length*2) + "X", new BigInteger(1, data));
	}

	public void setContext(Context contexto) {
		context = contexto;
		cacheDir=new File(getDir(), "cache");
		if(!cacheDir.exists())
			cacheDir.mkdirs();
	}
}
