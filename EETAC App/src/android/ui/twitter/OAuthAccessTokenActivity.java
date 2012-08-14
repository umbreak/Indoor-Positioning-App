package android.ui.twitter;

import java.io.IOException;

import twitter4j.User;
import static android.utils.Actions.*;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.ui.LoginActivity;
import android.ui.twitter.store.CredentialStore;
import android.ui.twitter.store.SharedPreferencesCredentialStore;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import com.google.api.client.auth.oauth.OAuthHmacSigner;
import com.google.api.client.http.apache.ApacheHttpTransport;

public class OAuthAccessTokenActivity extends Activity {

	final String TAG = getClass().getName();

	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "Starting task to retrieve request token.");
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		WebView webview = new WebView(this);
		webview.getSettings().setJavaScriptEnabled(true);  
		webview.setVisibility(View.VISIBLE);
		setContentView(webview);

		Log.i(TAG, "Retrieving request token from Google servers");

		try {

			new SharedPreferencesCredentialStore(prefs).clearCredentials();

			final OAuthHmacSigner signer = new OAuthHmacSigner();
			signer.clientSharedSecret = CONSUMER_SECRET;

			OAuthGetTemporaryToken temporaryToken = new OAuthGetTemporaryToken(REQUEST_URL);
			temporaryToken.transport = new ApacheHttpTransport();
			temporaryToken.signer = signer;
			temporaryToken.consumerKey = CONSUMER_KEY;
			temporaryToken.callback = OAUTH_CALLBACK_URL;

			OAuthCredentialsResponse tempCredentials = temporaryToken.execute();
			signer.tokenSharedSecret = tempCredentials.tokenSecret;

			OAuthAuthorizeTemporaryTokenUrl authorizeUrl = new OAuthAuthorizeTemporaryTokenUrl(AUTHORIZE_URL);
			authorizeUrl.temporaryToken = tempCredentials.token;
			String authorizationUrl = authorizeUrl.build();


			/* WebViewClient must be set BEFORE calling loadUrl! */  
			webview.setWebViewClient(new WebViewClient() {  

				@Override  
				public void onPageStarted(WebView view, String url,Bitmap bitmap)  {  
					System.out.println("onPageStarted : " + url);
				}
				@Override  
				public void onPageFinished(WebView view, String url)  {  

					if (url.startsWith(OAUTH_CALLBACK_URL)) {
						try {

							if (url.indexOf("oauth_token=")!=-1) {
								view.setVisibility(View.INVISIBLE);

								String requestToken  = extractParamFromUrl(url,"oauth_token");
								String verifier= extractParamFromUrl(url,"oauth_verifier");

								signer.clientSharedSecret = CONSUMER_SECRET;

								OAuthGetAccessToken accessToken = new OAuthGetAccessToken(ACCESS_URL);
								accessToken.transport = new ApacheHttpTransport();
								accessToken.temporaryToken = requestToken;
								accessToken.signer = signer;
								accessToken.consumerKey = CONSUMER_KEY;
								accessToken.verifier = verifier;

								OAuthCredentialsResponse credentials = accessToken.execute();
								signer.tokenSharedSecret = credentials.tokenSecret;


								CredentialStore credentialStore = new SharedPreferencesCredentialStore(prefs);
								credentialStore.write(new String[] {credentials.token,credentials.tokenSecret});
//								view.setVisibility(View.INVISIBLE);
								try {
									setUser();
								} catch (Exception e) {
									e.printStackTrace();
								}
								Intent intent= new Intent(OAuthAccessTokenActivity.this,LoginActivity.class);
									intent.putExtra(TWITTER_NOTIFICATOINS, true);
								startActivity(intent);
							} else if (url.indexOf("error=")!=-1) {
								view.setVisibility(View.INVISIBLE);
								//								new SharedPreferencesCredentialStore(prefs).clearCredentials();
								startActivity(new Intent(OAuthAccessTokenActivity.this,LoginActivity.class));
								finish();
							}

						} catch (IOException e) {
							e.printStackTrace();
						}

					}
					System.out.println("onPageFinished : " + url);

				}
				private String extractParamFromUrl(String url,String paramName) {
					String queryString = url.substring(url.indexOf("?", 0)+1,url.length());
					QueryStringParser queryStringParser = new QueryStringParser(queryString);
					return queryStringParser.getQueryParamValue(paramName);
				}  

			});  

			webview.loadUrl(authorizationUrl);	
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	private void setUser() throws Exception{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editPrefs = prefs.edit();
		User user=TwitterUtils.getUser(prefs);
		editPrefs.putString(PREFS_USER, user.getScreenName());
		editPrefs.putString(PREFS_PASS, user.getId() + user.getScreenName() + user.getId());
		editPrefs.putInt(PREFS_TWITTER_ID, user.getId());
		editPrefs.commit();
	}

}
