package android.ui.restclient;

import static android.utils.Actions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentCodingType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.ui.pojos.Checkin;
import android.ui.pojos.Comment;
import android.ui.pojos.Picture;
import android.ui.pojos.ShortSite;
import android.ui.pojos.Site;
import android.ui.pojos.TokenAndId;
import android.ui.pojos.User;
import android.util.Log;
import android.utils.ServiceHelper;
import android.utils.ToolKit;
/**
 * Singleton that perform the HTTP connection with the Server and creates the correct JSON structure (using REST).
 * Uses the Spring Framework for Android.
 * Obtain the response and Serialize (with gson internally) automatically to the desired Object.
 * Used by the IntentServer (PUTServer and GETServer)*/

public enum Processor {
	i;
	//	private static final String url="http://147.83.7.85:8080/DXAT_Server/rest";
	private static String url;
	//Date received from the server to make the hash

	//Header: Eetac_token sent to the server
	private static String token;
	public static Context appContext;
	public static File cache_path;
	private static String url_image;
	public static final String TAG="RestClient";
	private RestTemplate restTemplate;
	private HttpEntity<?> requestEntity;
	private HttpHeaders requestHeaders;
	private Processor(){
		restTemplate = new RestTemplate();
		requestHeaders = new HttpHeaders();
		requestHeaders.setAccept(Collections.singletonList(new MediaType("application","json")));
		requestHeaders.setAcceptEncoding(Collections.singletonList(ContentCodingType.GZIP));
		requestEntity = new HttpEntity<Object>(requestHeaders);
	}
	//Reading input stream, saving to file and returning the bitmap. From: http://evgeny-goldin.com/blog/category/spring/
	private static final RequestCallback ACCEPT_CALLBACK =
			new RequestCallback()
	{
		public void doWithRequest ( ClientHttpRequest request ) throws IOException
		{
			request.getHeaders().setAccept(Collections.singletonList(new MediaType("application","json")));
			request.getHeaders().setAcceptEncoding(Collections.singletonList(ContentCodingType.GZIP));
		}
	};

	private static class BitmapResponseExtractor implements ResponseExtractor<Bitmap>
	{
		private final File file;

		private BitmapResponseExtractor ( File file )
		{
			this.file = file;
		}
		public Bitmap extractData ( ClientHttpResponse response ) throws IOException
		{
			InputStream  is=null;
			is = response.getBody();

			OutputStream os = new FileOutputStream(file);
			IOUtils.copy( is, os);
			IOUtils.closeQuietly( is );
			IOUtils.closeQuietly( os );
			final Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());

			return bitmap;
		}
	}

	public int postUser(){
		try{	
			requestHeaders.set("Eetac_challenge", UUID.randomUUID().toString());
			requestEntity = new HttpEntity<Object>(requestHeaders);
			ResponseEntity<String> responseEntity = restTemplate.exchange(url + "/date/", HttpMethod.GET, requestEntity, String.class);
			String challenge_date=responseEntity.getBody();
			User user = new User();
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(appContext);
			int twitter_id=settings.getInt(PREFS_TWITTER_ID, 0);
			user.twitter_id=twitter_id;
			user.username=settings.getString(PREFS_USER, "");
			user.password=settings.getString(PREFS_PASS, "");

			String hashed_password=ToolKit.i.getHash(user.password);
			token=ToolKit.i.getHash(hashed_password+challenge_date);
			user.password=token;

			HttpEntity<User> commentEntity = new HttpEntity<User>(user, requestHeaders);
			ResponseEntity<Integer> responseSecondEntity;
			if (twitter_id == 0)
				 responseSecondEntity = restTemplate.exchange(url + "/user/login", HttpMethod.POST, commentEntity, Integer.class);
			else
				responseSecondEntity = restTemplate.exchange(url + "/user/login/external", HttpMethod.POST, commentEntity, Integer.class);
			int response=responseSecondEntity.getBody();

			requestHeaders.remove("Eetac_challenge");
			requestHeaders.set("Eetac_token", token);
			requestEntity = new HttpEntity<Object>(requestHeaders);
			
			SharedPreferences.Editor editPrefs = settings.edit();
			editPrefs.putInt(PREFS_USER_ID, response);
			editPrefs.commit();
			
			return response;

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.NOT_ACCEPTABLE)){
				Log.d(TAG, e.toString());
				return -1;
			}
			return -3;
		}catch(Exception e){
			Log.d(TAG, e.toString());
			return -2;
		}		
	}
	public void logoutUser(){
		try{
			ResponseEntity<Integer> responseEntity = restTemplate.exchange(url + "/user/login", HttpMethod.DELETE, requestEntity, Integer.class);
			requestHeaders.remove("Eetac_token");
			requestEntity = new HttpEntity<Object>(requestHeaders);
		}catch(HttpClientErrorException e){}
	}
	public int putUser(User user){
		try{
			user.password=ToolKit.i.getHash(user.password);
			user.twitter_id=0;
			HttpEntity<User> userEntity = new HttpEntity<User>(user, requestHeaders);
			ResponseEntity<TokenAndId> responseEntity = restTemplate.exchange(url + "/user/", HttpMethod.PUT, userEntity, TokenAndId.class);
			TokenAndId token_and_id =responseEntity.getBody();
			requestHeaders.set("Eetac_token", token_and_id.token);
			requestEntity = new HttpEntity<Object>(requestHeaders);
			SharedPreferences.Editor editPrefs = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
			editPrefs.putInt(PREFS_USER_ID, token_and_id.user_id);
			editPrefs.commit();
			
			return token_and_id.user_id;

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return putUser(user);
			}else if (e.getStackTrace().equals(HttpStatus.NOT_ACCEPTABLE))
				return 0;
			Log.d(TAG, e.toString());
			return -1;
		}	
	}

	public ShortSite[] getSites(){
		try{
			ResponseEntity<ShortSite[]> responseEntity = restTemplate.exchange(url + "/site", HttpMethod.GET, requestEntity, ShortSite[].class);
			return responseEntity.getBody();

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return getSites();
			}
			Log.d(TAG, e.toString());
			return null;
		}	
	}
	public ShortSite[] getSites(ArrayList<Integer> sites){
		try{
			HttpEntity<ArrayList<Integer>> sitestEntity = new HttpEntity<ArrayList<Integer>>(sites, requestHeaders);
			ResponseEntity<ShortSite[]> responseEntity = restTemplate.exchange(url + "/site", HttpMethod.POST, sitestEntity, ShortSite[].class);
			return responseEntity.getBody();

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return getSites();
			}
			Log.d(TAG, e.toString());
			return null;
		}	
	}

	public Site getSite(String site){
		try{
			ResponseEntity<Site> responseEntity = restTemplate.exchange(url + "/site/"+site, HttpMethod.GET, requestEntity, Site.class);
			return responseEntity.getBody();

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return getSite(site);
			}
			Log.d(TAG, e.toString());
			return null;
		}	
	}
	public ShortSite getShortSite(String site){
		try{
			ResponseEntity<ShortSite> responseEntity = restTemplate.exchange(url + "/site/"+site + "?type=short", HttpMethod.GET, requestEntity, ShortSite.class);
			return responseEntity.getBody();

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return getShortSite(site);
			}
			Log.d(TAG, e.toString());
			return null;
		}	
	}
	public Comment[] getSiteComments(String site){
		try{
			ResponseEntity<Comment[]> responseEntity = restTemplate.exchange(url + "/site/"+site+"/comment/", HttpMethod.GET, requestEntity, Comment[].class);
			return responseEntity.getBody();

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return getSiteComments(site);
			}
			Log.d(TAG, e.toString());
			return null;
		}
	}
	public Comment[] getPictureComments(String site, String picture){
		try{
			ResponseEntity<Comment[]> responseEntity = restTemplate.exchange(url + "/site/"+site+"/picture/"+ picture + "/comment/", HttpMethod.GET, requestEntity, Comment[].class);
			return responseEntity.getBody();

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return getPictureComments(site,picture);
			}
			Log.d(TAG, e.toString());
			return null;
		}
	}
	public Comment putComment(String site, Comment c){
		try{
			HttpEntity<Comment> commentEntity = new HttpEntity<Comment>(c, requestHeaders);
			ResponseEntity<Comment> responseEntity = restTemplate.exchange(url + "/site/"+site+"/comment/", HttpMethod.PUT, commentEntity, Comment.class);
			return responseEntity.getBody();

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return putComment(site,c);
			}
			Log.d(TAG, e.toString());
			return null;
		}	
	}

	public Comment putComment(String site,String picture, Comment c){
		try{
			HttpEntity<Comment> commentEntity = new HttpEntity<Comment>(c, requestHeaders);
			ResponseEntity<Comment> responseEntity = restTemplate.exchange(url + "/site/"+site+"/picture/"+picture+"/comment/", HttpMethod.PUT, commentEntity, Comment.class);
			return responseEntity.getBody();

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return putComment(site,picture,c);
			}
			Log.d(TAG, e.toString());
			return null;
		}	
	}
	public boolean deleteComment(String site, int comment_id){
		try{

			restTemplate.exchange(url + "/site/"+site+"/comment/"+ comment_id, HttpMethod.DELETE, requestEntity, String.class);
			return true;

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return deleteComment(site,comment_id);
			}
			Log.d(TAG, e.toString());
			return false;
		}	
	}

	public int putCheckin(String site, Checkin checkin){
		try{
			HttpEntity<Checkin> checkinEntity = new HttpEntity<Checkin>(checkin, requestHeaders);
			//			restTemplate.put(url + "/site/"+site+"/checkin/", checkinEntity);
			ResponseEntity<Integer> responseEntity = restTemplate.exchange(url + "/site/"+site+"/checkin/", HttpMethod.PUT, checkinEntity, Integer.class);
			return responseEntity.getBody();

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return putCheckin(site,checkin);
			}
			Log.d(TAG, e.toString());
			if (e.getStatusCode().equals(HttpStatus.NOT_ACCEPTABLE))
				return -2;
			else
				return -3;
		}	
	}

	public boolean deleteCheckin(String site, int checkin_id){
		try{

			restTemplate.exchange(url + "/site/"+site+"/checkin/"+ checkin_id, HttpMethod.DELETE, requestEntity, String.class);
			return true;

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return deleteCheckin(site,checkin_id);
			}
			Log.d(TAG, e.toString());
			return false;
		}	
	}


	public Bitmap getImage(String path){
		try{
			return restTemplate.execute(url_image + "/" + path, HttpMethod.GET, ACCEPT_CALLBACK, new BitmapResponseExtractor(new File(cache_path,path.substring(path.lastIndexOf('/') + 1))));

		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return getImage(path);
			}
			Log.d(TAG, e.toString());
			return null;
		}
	}
	public Picture postImage(String site, String file_url){
		try{
			MultiValueMap<String, Object> parts = new LinkedMultiValueMap<String, Object>();
			parts.add("file", new FileSystemResource(new File(file_url)));

			HttpEntity<MultiValueMap<String, Object>> imageEntity = new HttpEntity<MultiValueMap<String, Object>>(parts, requestHeaders);
			ResponseEntity<Picture> responseEntity = restTemplate.exchange(url+"/site/"+site+"/picture/", HttpMethod.POST, imageEntity, Picture.class);
			return responseEntity.getBody();


		}catch(HttpClientErrorException e){
			if (e.getStatusCode().equals(HttpStatus.FORBIDDEN)){
				Log.d(TAG, "Re-logging");
				if (postUser() > 0) return postImage(site,file_url);
			}
			Log.d(TAG, e.toString());
			return null;
		}	
	}


	public static void setUrl(String url) {
		//		Processor.url="http://192.168.1.4:8080/DXAT_Server/rest";
		Processor.url = "http://" + url + ":8080/Rest_Server/rest";
		Processor.url_image = "http://" + url + ":8080/Rest_Server";
	}
}