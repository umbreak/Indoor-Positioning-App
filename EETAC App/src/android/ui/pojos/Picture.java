package android.ui.pojos;

import android.os.Parcel;
import android.os.Parcelable;

public class Picture implements Parcelable{

	public int id;
	public String author;
	public String route_image;
	public boolean main;
	
	public Picture(){}
	
	public static final Parcelable.Creator<Picture> CREATOR = new
			Parcelable.Creator<Picture>() {
		public Picture createFromParcel(Parcel in) {
			return new Picture(in);
		}

		public Picture[] newArray(int size) {
			return new Picture[size];
		}
	};
	
	private Picture(Parcel in) {
		id = in.readInt();
		author=in.readString();
		route_image=in.readString();
	}
	
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeString(author);
		dest.writeString(route_image);
	}

}
