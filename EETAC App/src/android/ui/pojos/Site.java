package android.ui.pojos;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class Site extends ShortSite{
	
	public ArrayList<Comment> comments = new ArrayList<Comment>();
	public  ArrayList<Picture>  pictures;
	public Location location = new Location();
	public Site(){
		comments = new ArrayList<Comment>();
		pictures = new ArrayList<Picture>();
	}
	

	
	public void setComment(Comment comment){
		this.comments.add(comment);
		num_comments=comments.size();
	}

	public void setShortSite(ShortSite s){
		avatar=s.avatar;
		building=s.building;
		description=s.description;
		floor=s.floor;
		id=s.id;
		name=s.name;
		num_checkins=s.num_checkins;
		num_comments=s.num_comments;
		checkin_id=s.checkin_id;
		
	}

}
