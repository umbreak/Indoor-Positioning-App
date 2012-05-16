package android.ui.pojos;

import android.os.Parcel;
import android.os.Parcelable;


public class Comment implements Parcelable{
	public int id;
	public String author;
	public String text;
	public String date;

	public Comment(String author, String text, String date, int id) {
		super();
		this.author = author;
		this.text = text;
		this.date = date;
		this.id=id;
	}
	public Comment(String text) {
		super();
		this.text = text;
	}
	
	public Comment(){}
	public static final Parcelable.Creator<Comment> CREATOR = new
			Parcelable.Creator<Comment>() {
		public Comment createFromParcel(Parcel in) {
			return new Comment(in);
		}

		public Comment[] newArray(int size) {
			return new Comment[size];
		}
	};
	
	private Comment(Parcel in) {
		id = in.readInt();
		date= in.readString();
		text= in.readString();
		author=in.readString();
	}
	
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);

		dest.writeString(date);
		dest.writeString(text);
		dest.writeString(author);
	}

	@Override
	public String toString() {
		return "\nComment [author=" + author + ", text=" + text + ", date="
				+ date + ", id=" + id + "]\n";
	}	

}
