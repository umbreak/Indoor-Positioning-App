package android.utils;
/**
 * Actions to interact between activities (throught Intents).
 * Actions to interact between Services and the ResultService.
 * Actions to interact with the SharedPreferences. */
public class Actions {
	private Actions() {
		super();
	}
	public static final String ACTION = "action";
	public static final String RECEIVER = "receiver";
	public static final String SITE_VAULE = "site_id";
	public static final String USER_VAULE = "user_value";
	public static final String COMMENT_VAULE = "comment_object";
	public static final String COMMENT_POSITION = "comment_pos";

	public static final String PICTURE_URL = "pic_url";
	public static final String PICTURE_VALUE = "pic_val";
	public static final String CHECKIN_ID = "checkin_id";
	

	
	// Type of GET action to do in the PROCESSOR
	public static final String GET_SITES = "GET_sites";
	public static final String GET_SITE = "GET_site";
	public static final String POST_SITES = "POST_sites";

	public static final String SITE_SHORT = "Type_isShort";
	public static final String GET_COMMENTS = "GET_comm";
	public static final String GET_PICTURES = "IMG_GET";
	public static final String GET_PICTURE = "IMG_GET";
	public static final String GET_COMMENTS_PICTURE = "GET_comm_pic";
	
	//Type of PUT&POST action to do in the PROCESSOR
	public static final String PUT_USER = "PUT_user";
	public static final String PUT_COMMENT = "PUT_com";
	public static final String LOGIN = "POST_login";
	public static final String LOGOUT = "DEL_login";
	public static final String LOGOUT_AND_EXIT = "DEL_login_exit";

	public static final String POST_PICTURE = "POST_pic";
	public static final String PUT_CHECKIN = "PUT_checkin";
	public static final String PUT_COMMENT_PICTURE = "PUT_comm_pic";
	
	//Type of DELETE actions to do in the PROCESSOR
	public static final String DEL_CHECKIN = "DEL_checkin";
	public static final String DEL_COMMENT = "DEL_comment";
	

	
	//Result status on the Receiver
	public static final String RESULT = "res";
	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x3;
	public static final int STATUS_FINISHED_COMMENT = 0x4;
	public static final int STATUS_FINISHED_CHECKIN = 0x5;
	public static final int STATUS_FINISHED_CHECKOUT = 0x6;
	public static final int STATUS_FINISHED_PICTURE_POST = 0x7;
	public static final int STATUS_FINISHED_PICTURE_GET = 0x8;
	public static final int STATUS_FINISHED_COMMENTS_PIC_GET = 0x9;
	public static final int STATUS_FINISHED_DEL_COMMENT = 0x10;
	
	
	//SharedPreferences
	public static final String PREFS_USER_ID="userid";
	public static final String UPDATE_ALWAYS="checkbox";
	public static final String TWITTER_NOTIFICATOINS="sendTweet";
	public static final String PREFS_USER="username";
	public static final String PREFS_PASS="password";
	public static final String PREFS_SERVER="server_ip";
	public static final String PREFS_FAVORITES="favorites";
	public static final String PREFS_TWITTER_ID="twitter_id";
	
	//Camera action
	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE=5;
	
	//TWITTER CONSTANTS
	public static final String CONSUMER_KEY = "35JVGrD3oMVe5p29IXlhA";
	public static final String CONSUMER_SECRET= "inPMRBix22TfpNWm3prNi8nKAw6H81L48rICN4MmXY";	
	public static final String REQUEST_URL = "https://api.twitter.com/oauth/request_token";
	public static final String ACCESS_URL = "https://api.twitter.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "https://api.twitter.com/oauth/authorize";	
	public static final String	OAUTH_CALLBACK_URL		= "http://localhost";
	
	//TWITTER ACTIONS
	public static final String SEND_TWEET = "sendTweet";




}
