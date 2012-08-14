package android.ui.pojos;

import android.os.Parcel;
import android.os.Parcelable;



public class User implements Parcelable {

	public String name;
	public String username;
	public int id;
	public String email;
	public String password;
	public int twitter_id;

	public User(){}
	public User(String username, String password){
		this.username=username;
		this.password=password;
	}
	public static final Parcelable.Creator<User> CREATOR = new
			Parcelable.Creator<User>() {
		public User createFromParcel(Parcel in) {
			return new User(in);
		}

		public User[] newArray(int size) {
			return new User[size];
		}
	};
	private User(Parcel in) {
		id = in.readInt();
		twitter_id = in.readInt();
		name= in.readString();
		username= in.readString();
		password=in.readString();
		email=in.readString();
		
	}
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(id);
		dest.writeInt(twitter_id);
		dest.writeString(name);
		dest.writeString(username);
		dest.writeString(password);
		dest.writeString(email);
	}
}
