package android.ui.pojos;

import android.os.Parcel;
import android.os.Parcelable;

public class ShortSite implements Parcelable{
	public int id;
	public int avatar; 
	public String name;
	public String description;
	public String floor;
	public String building;
	public int checkin_id;
	public String route_image;
	
	public int num_comments;
	public int num_checkins;
	public ShortSite(int id, int avatar, String name, String description, int comments, int checkins, String floor, String building, int checkin_id, String route_image) {
		super();
		this.id=id;
		this.avatar = avatar;
		this.name = name;
		this.description = description;
		this.num_comments=comments;
		this.num_checkins=checkins;
		this.floor=floor;
		this.building=building;
		this.checkin_id=checkin_id;
		this.route_image=route_image;
	}
	public static final Parcelable.Creator<ShortSite> CREATOR = new
			Parcelable.Creator<ShortSite>() {
		public ShortSite createFromParcel(Parcel in) {
			return new ShortSite(in);
		}

		public ShortSite[] newArray(int size) {
			return new ShortSite[size];
		}
	};

	public ShortSite() {
	}
	private ShortSite(Parcel in) {
		avatar = in.readInt();
		num_comments = in.readInt();
		num_checkins = in.readInt();
		id=in.readInt();
		name= in.readString();
		description= in.readString();
		floor=in.readString();
		building=in.readString();
		route_image=in.readString();
		checkin_id=in.readInt();
		
	}
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(avatar);
		dest.writeInt(num_comments);
		dest.writeInt(num_checkins);
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeString(floor);
		dest.writeString(building);
		dest.writeString(route_image);
		dest.writeInt(checkin_id);
	}
	@Override
	public String toString() {
		return "ShortSite [id=" + id + ", avatar=" + avatar + ", name=" + name
				+ ", description=" + description + ", floor=" + floor
				+ ", building=" + building + ", isChecking=" + checkin_id
				+ ", num_comments=" + num_comments + ", num_checkins="
				+ num_checkins + "]";
	}
	

}
