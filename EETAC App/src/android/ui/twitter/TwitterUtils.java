package android.ui.twitter;


import static android.utils.Actions.CONSUMER_KEY;
import static android.utils.Actions.CONSUMER_SECRET;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.http.AccessToken;
import android.content.SharedPreferences;
import android.ui.twitter.store.SharedPreferencesCredentialStore;

public enum TwitterUtils {
	i;
	private TwitterUtils(){
	}

	public static boolean isAuthenticated(SharedPreferences prefs) {
		Twitter twitter=getTwitter(prefs);
		try {
			twitter.getAccountSettings();
			return true;
		} catch (TwitterException e) {
			return false;
		}
	}

	public  void sendTweet(SharedPreferences prefs,String msg) {
		new TwitterUtils.SendTweet(prefs, msg);
	}	
	public static String getScreenName(SharedPreferences prefs) throws Exception {
		Twitter twitter=getTwitter(prefs);
		User user = twitter.showUser(twitter.getId());
		return user.getScreenName();
	}
	public static User getUser(SharedPreferences prefs) throws Exception {
		Twitter twitter=getTwitter(prefs);
		User user = twitter.showUser(twitter.getId());
		return user;
	}

	private static Twitter getTwitter(SharedPreferences prefs){
		String[] tokens = new SharedPreferencesCredentialStore(prefs).read();
		AccessToken a = new AccessToken(tokens[0],tokens[1]);
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		twitter.setOAuthAccessToken(a);
		return twitter;
	}
	private class SendTweet implements Runnable{
		private SharedPreferences prefs;
		private String msg;
		public SendTweet(SharedPreferences prefs, String msg) {
			super();
			this.prefs = prefs;
			this.msg = msg;
			Thread t=new Thread(this);
			t.start();
		}
		public void run() {
			Twitter twitter=getTwitter(prefs);
			try {
				twitter.updateStatus(msg);
			} catch (TwitterException e) {
			}	
		}
	}
}
