package android.ui.map;
import static android.utils.Actions.GET_SITE;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.ui.R;
import android.ui.explore.ExploreLargeActivity;
import android.ui.pojos.ShortSite;
import android.util.Log;
import android.utils.MenuHelper;
import android.utils.MyResultReceiver;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ericsson.android.indoormaps.IndoorMapActivity;
import com.ericsson.android.indoormaps.ItemizedOverlay;
import com.ericsson.android.indoormaps.MapController;
import com.ericsson.android.indoormaps.MapController.LoadingListener;
import com.ericsson.android.indoormaps.MapController.MapItemOnFocusChangeListener;
import com.ericsson.android.indoormaps.MapManager;
import com.ericsson.android.indoormaps.MapView;
import com.ericsson.android.indoormaps.MyLocationOverlay;
import com.ericsson.android.indoormaps.Overlay;
import com.ericsson.android.indoormaps.OverlayItem;
import com.ericsson.android.indoormaps.Projection;
import com.ericsson.android.indoormaps.location.IndoorLocationProvider;
import com.ericsson.android.indoormaps.location.IndoorLocationProvider.IndoorLocationListener;
import com.ericsson.android.indoormaps.location.IndoorLocationProvider.IndoorLocationRequestStatus;
import com.ericsson.android.indoormaps.routing.DefaultRoutingService;
import com.ericsson.indoormaps.model.GeoPoint;
import com.ericsson.indoormaps.model.Location;
import com.ericsson.indoormaps.model.MapItem;
import com.ericsson.indoormaps.model.Node;
import com.ericsson.indoormaps.model.Point;
import com.ericsson.indoormaps.routing.Route;
import com.ericsson.indoormaps.routing.RouteItem;
import com.ericsson.indoormaps.routing.RoutingService;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.AbstractAction;
/**
 * Acitivty that shows the indoor mapping. It uses Ericsson API for the positioning and mapping.
 * It could receive a ShortSite = ...getParcelableExtra(GET_SITE) from ExploreTabSelectionActivity.
 * It could send the site_id (...putExtra(GET_SITE, Integer.valueOf(siteId))) to ExploreLargeActivity.*/

public class MapActivity extends IndoorMapActivity implements LoadingListener,
		View.OnClickListener {

	/*
	 * Configuration
	 */
	//private static final int C4_BUILDING_ID = 5128; //5008
	private static final int C4_UNDER_FLOOR_MAP_ID = 0;//21
	private static final int C4_MALL_FLOOR_MAP_ID = 235;//21
	private static final int C4_FIRST_FLOOR_MAP_ID = 309;//21
	private static final int C4_SECOND_FLOOR_MAP_ID = 0;//21
	private static final int C4_THIRD_FLOOR_MAP_ID = 0;//21
	
	ItemizedOverlay overlayOfRoomNames = null;
	

	private static final int EXAMPLE_STYLE_ID = 132;//1
	private static final String API_KEY = "i5IDNM4xhD74C2wPaB3j8DcXG32RXhxITXqZhqCl";

	/*
	 * Debug
	 */
	protected static final String LOG_TAG = "AndroidExampleClient";

	/*
	 * Declaraciones
	 */
	private MapController mMapController;
	private Route mRoute;
	private MyLocationOverlay mMyLocationOverlay;
	private ImageButton mRouteButton;
	private ImageButton next_button;
	private ImageButton prev_button;
	private Button Change_floor_button;



 
	
	/*
	 * Menu
	 */
	private MenuHelper helper;
	public MapActivity(){
		super();
		this.helper=new MenuHelper(this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {		
		return helper.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return helper.onOptionsItemSelected(item);
	}
		
	/*
	  * Source and Destination Points for route calculation.
	  */
	private Point from = null;
	private Point to = null;
	private int fromMapId = 0;
	private int toMapId = 0;
	private int indexMapOverlay;	//index for overlay used in rote calculation.
	


	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		// layout assignation
		setMapView((MapView) findViewById(R.id.indoor_map_view));
		

		//Controller
		mMapController = getMapView().getMapController();
		MenuHelper.setActionBar(this, MenuHelper.ACTIONBAR_MAP);
		// Set listener to get callback when map and style is loading. Good
		// thing to set before calling setMap/setStyle.
		mMapController.setLoadingListener(this);
		mMapController.setMap(C4_MALL_FLOOR_MAP_ID, API_KEY, true);
		mMapController.setStyle(EXAMPLE_STYLE_ID, API_KEY, true);
		
		
		
		// Display zoom controls
		getMapView().setBuiltInZoomControls(true);
		
		/*
		 *  Navigation buttons
		 */
		mRouteButton = (ImageButton) findViewById(R.id.buttonCancelRoute);
		mRouteButton.setOnClickListener(this);
		mRouteButton.setVisibility(View.GONE);
		
		next_button = (ImageButton) findViewById(R.id.buttonNext);
		next_button.setOnClickListener(this);
		next_button.setVisibility(View.GONE);
		
		prev_button = (ImageButton) findViewById(R.id.buttonPrev);
		prev_button.setOnClickListener(this);
		prev_button.setVisibility(View.GONE);
		
		Change_floor_button = (Button) findViewById(R.id.buttonChangeFloor);
		Change_floor_button.setOnClickListener(this);
		Change_floor_button.setVisibility(View.VISIBLE);
		
		/*
		 * Other buttons
		 */
		
		// Add Location button in actionBar
		GetLocationClick onGetLocationClick = new GetLocationClick();
		MenuHelper.actionBar.setHomeAction(onGetLocationClick);
		


		/*
		 * Listen to focus change of items on the map.
		 * Se activa al dar sobre un sitio en el mapa.
		 */
		mMapController.setOnFocusChangeListener(new MapItemOnFocusChangeListener() {

					public void onMapItemFocusChange(MapItem mapItem) {
						//The site exists?
						if (mapItem != null) {
							Map<String, String> tags = mapItem.getTags();
							mMapController.animateTo(mapItem.getCenter());
							//String text = tags.get("room:type")+":"+tags.get("name");
							//toast(text);
							if (tags.get("room") != null)
								chooseActionForSite(tags.get("room:code"), tags.get("room:id") );
						} else {
							// mapItem == null means no MapItem has focus any longer
							toast("MapItem lost focus");
						}
					}
			
			
				});
		/*
		mRouteButton = (ImageButton) findViewById(R.id.buttonRoute);
		mRouteButton.setOnClickListener(this);
		findViewById(R.id.buttonNext).setOnClickListener(this);
		findViewById(R.id.buttonPrev).setOnClickListener(this);
		findViewById(R.id.buttonLocation).setOnClickListener(this);
		findViewById(R.id.buttonTest).setOnClickListener(this);
		*/	
		
	}
	
	
	/**
	 * 
	 * fromMyLocationTo(): Allow to go a site from our current position.
	 * @param ShortSite
	 */
	protected void onResume() {
		MenuHelper.actionBar=(ActionBar) findViewById(R.id.actionbar);
		super.onResume();
	}
	public void fromMyLocationTo(ShortSite site) {
		
		//LIMITATION: We only can go to the same floor.
		toMapId = getMapView().getMapId(); //Provisional
		fromMapId = getMapView().getMapId(); //Provisional 
		
		//HashMap allow to define the keys for looking a site in the Map.
		final HashMap<String, String> withTags2 = new HashMap<String, String>();
		withTags2.put("room:id", String.valueOf(site.id));
		final List<MapItem> mapSite = MapManager.getMapItems(withTags2, C4_MALL_FLOOR_MAP_ID, this);
		
		//Map<String, String> tags = mapSite.get(0).getTags();
		//toast("The room id is "+tags.get("room:id"));
		
		//toast("Hay algo?:"+String.valueOf(mapSite.isEmpty())+" Tama–o:"+mapSite.size());
		
		if (!mapSite.isEmpty()) {
			
			Log.d(LOG_TAG, "goTo(). We found the mapSite!");
			to = mapSite.get(0).getCenter();
			
			// Setup Indoor Location API and request a location
			IndoorLocationProvider locationProvider = new IndoorLocationProvider();
			IndoorLocationListener locationListener = new IndoorLocationListener() {

				public void onIndoorLocation(double latitude, double longitude,
							int buildingId, int floorId, int horizontalAccuracy) {
					
					GeoPoint geoPoint = new GeoPoint(latitude, longitude);
					geoPoint.setBuildingId(buildingId);
					geoPoint.setFloorId(floorId);
					Location location = getMapView().getProjection().getLocation(geoPoint);

					if (location != null) {
						from = location.getPoint();
						getRoute();
					} else {
						toast("Impossible get your location.");
						//Cargar una lista de sites para IR si no se puede obtener la posici—n
					}
					Log.d(LOG_TAG,"MapActivity.doThings(...).new IndoorLocationHandler() {...}.onIndoorLocation() - "+ geoPoint);
					//toast("Location received: " + geoPoint);
				}

				public void onError(IndoorLocationRequestStatus status,
						String message) {
					Log.d(LOG_TAG,"MapActivity.doThings(...).new IndoorLocationHandler() {...}.onError() - "+ message + " status: " + status);
					toast("Location not found: " + message);
				}
			};
			locationProvider.requestIndoorLocation(locationListener, this, API_KEY);
			
		} else {
			Log.d(LOG_TAG, "goTo(). PROBLEMS!!");
			toast("Some Problem has succeed (no one or more than one site have this id). Please check Maps.");
		}
		
		
	}

	
	private void changeFloor() {
		
		final CharSequence[] items = {"-1", "Mall", "First", "Second", "Third"};


		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select the floor");
		builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
			});
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    	int mapID = C4_MALL_FLOOR_MAP_ID;

				switch (item) {
				case 0:
					mapID = C4_UNDER_FLOOR_MAP_ID;
					break;
				case 1:
					mapID = C4_MALL_FLOOR_MAP_ID;
					break;
				case 2:
					mapID = C4_FIRST_FLOOR_MAP_ID;
					break;
				case 3:
					mapID = C4_SECOND_FLOOR_MAP_ID;
					break;
				case 4:
					mapID = C4_THIRD_FLOOR_MAP_ID;
					break;
		    	}
				mMapController.setMap(mapID, API_KEY, true);
				mMapController.setStyle(EXAMPLE_STYLE_ID, API_KEY, true);


		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * When "Test" button is clicked. This method show API features. Read
	 * through it to get ideas of what you can do with the API.
	 * 
	 * @param v
	 */
	/*
	public void testClick(View v) {
		toast("Check logcat output to see what happens here");
		// Find rooms and set them selected.
		final HashMap<String, String> withTags = new HashMap<String, String>();
		withTags.put("room", "yes");
		final List<MapItem> mapItems = MapManager.getMapItems(withTags,
				EXAMPLE_MAP_ID, getApplicationContext());
		mMapController.setSelected(mapItems);

		// Set only restroom poi:s visible
		ArrayList<String> poiTypes = new ArrayList<String>();
		poiTypes.add("restroom");
		mMapController.setVisiblePOITypes(poiTypes);

		
		
		// Get overlay
		List<Overlay> mapOverlays = mMapController.getOverlays();

		// Create custom itemized overlay and set its default marker icon
		CustomItemizedOverlay overlay = new CustomItemizedOverlay();
		Drawable defaultIcon = getResources().getDrawable(R.drawable.icon);
		overlay.setDefaultMarker(defaultIcon);
		
		

		// Add overlay item that use overlays default icon
		Point point = new Point(100, 10);
		OverlayItem item = new OverlayItem(point, null, "Overlay item 1");
		overlay.addItem(item);
		mapOverlays.add(overlay);

		// Add overlay item that use another icon and has some extra
		// info
		Drawable icon2 = getResources().getDrawable(R.drawable.icon2);
		Point point2 = new Point(50, 10);
		OverlayItem item2 = new OverlayItem(point2, icon2, "Overlay item 2");
		overlay.addItem(item2);
		
		

		// Get all rooms
		final HashMap<String, String> withTags2 = new HashMap<String, String>();
		withTags2.put("room", "yes");
		final List<MapItem> mapItemOnAnotherMap = MapManager.getMapItems(
				withTags2, 3, this);
		for (final MapItem mapItem : mapItemOnAnotherMap) {
			for (final Entry<String, String> entry : mapItem.getTags()
					.entrySet()) {
				Log.d(LOG_TAG,
						"other tag: " + entry.getKey() + ", "
								+ entry.getValue());
			}
		}

		// Run some of the request to get some info about maps, buildings and
		// styles.
		getDescriptionsAsync();
	}
	*/

	/**
	 * Request location button clicked.
	 * 
	 * @param v
	 */
	public void requestLocation() {
		// Setup Indoor Location API and request a location
		IndoorLocationProvider locationProvider = new IndoorLocationProvider();
		IndoorLocationListener locationListener = new IndoorLocationListener() {

			public void onIndoorLocation(double latitude, double longitude,
						int buildingId, int floorId, int horizontalAccuracy) {
				
				GeoPoint geoPoint = new GeoPoint(latitude, longitude);
				geoPoint.setBuildingId(buildingId);
				geoPoint.setFloorId(floorId);
				Location location = getMapView().getProjection().getLocation(geoPoint);

				if (location != null) {
					addMyLocationOverlayIfMissing();
					mMyLocationOverlay.setLocation(location);
					mMyLocationOverlay.setAccuracy(horizontalAccuracy);
					mMyLocationOverlay.setShowAccuracy(true);
					mMapController.animateTo(mMyLocationOverlay.getLocation().getPoint());
				}
				Log.d(LOG_TAG,"MapActivity.doThings(...).new IndoorLocationHandler() {...}.onIndoorLocation() - "+ geoPoint);
				toast("Location received: " + geoPoint);
			}

			public void onError(IndoorLocationRequestStatus status,
					String message) {
				Log.d(LOG_TAG,"MapActivity.doThings(...).new IndoorLocationHandler() {...}.onError() - "+ message + " status: " + status);
				toast("Location not found: " + message);
			}
		};
		locationProvider.requestIndoorLocation(locationListener, this, API_KEY);
	}

	/**
	 * Example of how to get descriptive objects for maps, buildings and styles.
	 * Getting descriptions are blocking calls and if requested from the server
	 * connectivity dependent. So make them async to avoid blocking the UI
	 * thread.
	 */
	/*
	private void getDescriptionsAsync() {

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				// Get a single map description
				MapDescription mapDescription;
				try {
					mapDescription = MapManager.getMapDescription(
							EXAMPLE_MAP_ID, API_KEY, MapActivity.this);
					Log.d(LOG_TAG, mapDescription.toString());
				} catch (IOException e) {
					Log.e(LOG_TAG,
							"Map description returned error: " + e.getMessage(),
							e);
				}
				// Get a single building description
				BuildingDescription buildingDescription;
				try {
					buildingDescription = MapManager.getBuildingDescription(
							EXAMPLE_BUILDING_ID, API_KEY, MapActivity.this);
					Log.d(LOG_TAG, buildingDescription.toString());
				} catch (IOException e) {
					Log.e(LOG_TAG,
							"Building description returned error: "
									+ e.getMessage(), e);
				}
				try {
					Context c = getApplicationContext();
					// Get map descriptions for all maps cached on the device
					for (MapDescription b : MapManager.getMapDescriptions(c,
							true, API_KEY)) {
						Log.d(LOG_TAG, "Maps - On device: " + b.toString());
					}
					// Get map descriptions for all maps accessible for your API
					// key on the server
					for (MapDescription b : MapManager.getMapDescriptions(c,
							false, API_KEY)) {
						Log.d(LOG_TAG, "Maps - On server: " + b.toString());
					}
					// Get building descriptions for all buildings stored on the
					// device
					for (BuildingDescription b : MapManager
							.getBuildingDescriptions(c, true, API_KEY)) {
						Log.d(LOG_TAG, "Building - On device: " + b.toString());
					}
					// Get building descriptions for all buildings accessible
					// for the API_KEY on
					// the server
					for (BuildingDescription b : MapManager
							.getBuildingDescriptions(c, false, API_KEY)) {
						Log.d(LOG_TAG, "Building - On server: " + b.toString());
					}
					// Get style descriptions for all styles accessible for the
					// API_KEY on the server
					for (StyleDescription b : MapManager.getStyleDescriptions(
							c, API_KEY)) {
						Log.d(LOG_TAG, "Style - On server: " + b.toString());
					}
				} catch (IOException e) {
					Log.d(LOG_TAG, "Could not get maps from server", e);
				}

				return null;
			}

		}.execute();
	}
	*/
	
	/*
	 * Add the room codes to classrooms, labs and others rooms
	 */
	private void addOverlayOfRoomNames(int mapID){
		
		//Controller of overlays
		List<Overlay> mapOverlays = mMapController.getOverlays();
		
		//if overlay is already used -> Erase overlay of map
		if (overlayOfRoomNames != null) 
			mapOverlays.remove(overlayOfRoomNames);
		
		//New overlay
		overlayOfRoomNames = new ItemizedOverlay();
		
		//Get room:code 
		final HashMap<String, String> withTags2 = new HashMap<String, String>();
		withTags2.put("room", "yes");
		final List<MapItem> mapSite = MapManager.getMapItems(withTags2, mapID, this);
	
		if (!mapSite.isEmpty()) {
			
			for (int i=0; i<mapSite.size(); i++)
			{				
				Map<String, String> tags = mapSite.get(i).getTags();
				if (tags.get("room:code") != null)
				{
					Point point = mapSite.get(i).getCenter();
					OverlayItem item = new OverlayItem(point, null, tags.get("room:code"));
					overlayOfRoomNames.addItem(item);
				}
			}
			mapOverlays.add(overlayOfRoomNames);
		}
	}
	/*
	 * FIN
	 */

	/**
	 * 
	 * A custom overlay that draws a circled on the map item in focus. It also
	 * handles touch events to set the location for MyLocationOverlay if the
	 * user long press the map view.
	 * 
	 */
	class CustomOverlay extends Overlay {
		private final GestureDetector mGestureDetector;
		private MapView mTouchedMapView;

		public CustomOverlay() {
			mGestureDetector = new GestureDetector(new MyGestureListener());
		}

		@Override
		public void onTouchEvent(MotionEvent event, MapView mapView) {
			mGestureDetector.onTouchEvent(event);
			mTouchedMapView = mapView;
		}

		@Override
		public void draw(Canvas canvas, MapView mapView) {
			MapItem focusedMapItem = mMapController.getFocusedMapItem();
			if (focusedMapItem != null) {
				// Get canvas coordinates from map coordinates with help from
				// projection
				Projection projection = mapView.getProjection();
				float canvasX = projection.getCanvasCoord(focusedMapItem
						.getCenter().getX());
				float canvasY = projection.getCanvasCoord(focusedMapItem
						.getCenter().getY());

				// Draw circle in the center of focused item
				Paint p = new Paint();
				p.setColor(Color.MAGENTA);
				p.setAlpha(90);
				p.setAntiAlias(true);
				canvas.drawCircle(canvasX, canvasY, 10, p);
			}
		}

		class MyGestureListener extends SimpleOnGestureListener {

			@Override
			public void onLongPress(MotionEvent e) {
				// Calculate the map coordinates for the long pressed pixel
				// coordinate
				Projection p = mTouchedMapView.getProjection();
				Point point = new Point(p.getMapX(e.getX()),
						p.getMapY(e.getY()));

				// Create a Location object for MyLocation set by the long press
				// and att it to MyLocationOverlay
				Location myFakeLocation = new Location(point, getMapView()
						.getBuildingId(), getMapView().getFloorId());

				addMyLocationOverlayIfMissing();

				mMyLocationOverlay.setLocation(myFakeLocation);
				mMyLocationOverlay.setShowAccuracy(false);
				mTouchedMapView.invalidate();
				// Some tactile feedback
				Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				v.vibrate(100);
				super.onLongPress(e);
			}

		}
	}

	private void addMyLocationOverlayIfMissing() {
		// My location overlay
		if (mMyLocationOverlay == null) {
			mMyLocationOverlay = new MyLocationOverlay();
			mMapController.getOverlays().add(mMyLocationOverlay);
		}
	}

	/**
	 * Custom {@link ItemizedOverlay} that listen to tap on a
	 * {@link OverlayItem} and toasts its text.
	 * 
	 */
	
	class CustomItemizedOverlay extends ItemizedOverlay {
		@Override
		public boolean singleTap(OverlayItem item) {
			if (super.singleTap(item)) {
				// Toast the text of the item
				toast(item.getText());
				// Return true to tell MapView that tap was handeld by overlay.
				return true;
			}
			return false;
		}
	}
	
/*
 * (non-Javadoc)
 * @see com.ericsson.android.indoormaps.MapController.LoadingListener#onMapLoading(com.ericsson.android.indoormaps.MapController.LoadingListener.LoadingState, int, java.lang.String)
 */
	public void onMapLoading(LoadingState state, int mapId, String message) {
		Log.d(LOG_TAG, "MapActivity.onMapLoading() - state: " + state + ", message: " + message);

		switch (state) {
		case FINISHED:
			
			// Get overlays
			List<Overlay> mapOverlays = mMapController.getOverlays();

			// Give example of how to use Projection to get map coordinates for
			// a latitude, longitude coordinate.
			showCaseProjection();

			// CustomOverlay that draws a circle and handles touch input
			// We want only one instance of it in the mapview
			CustomOverlay customOverlay = null;
			for (Overlay o : mapOverlays) {
				if (o instanceof CustomOverlay) {
					customOverlay = (CustomOverlay) o;
					break;
				}
			}
			mapOverlays.remove(customOverlay);
			mapOverlays.add(new CustomOverlay());
			
			Log.d(LOG_TAG, "onMapLoading() - Before getSite: ");
			ShortSite site = getIntent().getParcelableExtra(GET_SITE);
			
			if (site != null) {
				Log.d(LOG_TAG, "onMapLoading() -  After getSite. -> site : " +site.name);
				fromMyLocationTo(site);
			}
			
			addOverlayOfRoomNames(mapId);
							
			break;
		case ERROR:
			System.out.println("ERROR");
			Toast.makeText(MapActivity.this, message, Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.ericsson.android.indoormaps.MapController.LoadingListener#onStyleLoading(com.ericsson.android.indoormaps.MapController.LoadingListener.LoadingState, int, java.lang.String)
	 */
	public void onStyleLoading(LoadingState state, int styleId, String message) {
		Log.d(LOG_TAG, "MapActivity.onStyleLoading() - state: " + state);
		if (state == LoadingState.ERROR) {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Gives example of how to convert a lat, lon coordinate to map coordinates.
	 */
	private void showCaseProjection() {
		
		// Create a new overlay
		ItemizedOverlay overlay = new ItemizedOverlay();
		
		// Create a location (representation of map coordinates) from a
		// latitude, longitude point.
		Location locationFromLonLat = getMapView().getProjection().getLocation(new GeoPoint(59.40299, 17.94793));
		
		// The location might be null if the map is missing reference points
		if (locationFromLonLat != null) {
			
			Point point = locationFromLonLat.getPoint();
			
			// Create new overlay item and add to overlay
			overlay.addItem(new OverlayItem(point, getResources().getDrawable(R.drawable.icon), null));
			
			// Add overlay to map view
			getMapView().getMapController().getOverlays().add(overlay);
		}
	}

	private void toast(String text) {
		Toast.makeText(MapActivity.this, text, Toast.LENGTH_LONG).show();
	}
	
	private void chooseActionForSite(final String siteName, final String siteId) {
		final CharSequence[] items = {"Set Origen", "Set Destination", "Go to Site"};


		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(siteName);
		builder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
			});
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
				final MapItem focusedMapItem = mMapController.getFocusedMapItem();

				switch (item) {
				case 0:
					from = focusedMapItem.getCenter();
					fromMapId = getMapView().getMapId(); //Provisional
					toast("Origen fixed in: "+siteName+"(map:"+fromMapId+")");
					break;
				case 1:
					to = focusedMapItem.getCenter();
					toMapId = getMapView().getMapId(); //Provisional
					if (from != null && fromMapId != 0){
						getRoute();						
					}
					else {
						toast("Origen not stablished");
					}
					
					toast("Destination fixed in: "+siteName+"(map:"+fromMapId+")");
					break;
				case 2:
					toast("Id del site es:"+siteId+" y nombre :"+siteName);
					
					//Receiver que nos notificar‡ la llegada del Site
					MyResultReceiver mReceiver = new MyResultReceiver(new Handler());
					
					//Petici—n del Site
//					Bundle b= new Bundle();
//					b.putString(SITE_TYPE, "short");
//					ServiceHelper.startService(GET_SITE, siteId ,mReceiver);	
					Intent i =new Intent(getBaseContext(), ExploreLargeActivity.class);
					i.putExtra(GET_SITE, Integer.valueOf(siteId));
					startActivity(i);
					
					break;
		    	}
		        //Toast.makeText(getApplicationContext(), items[item], Toast.LENGTH_LONG).show();
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.utils.MyResultReceiver.Receiver#onReceiveResult(int, android.os.Bundle)
	 * Es el receiver de services.
	 */	
	/*
	 * Route calculation
	 */
	public void getRoute() {
		
		//addMyLocationOverlayIfMissing(); //?
		//final Location location = mMyLocationOverlay.getLocation();
		//final MapItem focusedMapItem = mMapController.getFocusedMapItem();
		
		toast("FROM "+from);
		
		if (mRoute == null){
	
			//Run routing async, since it is a blocking call
			new AsyncTask<Void, Void, Route>() {

				Handler handler = new Handler();

				@Override
				protected Route doInBackground(Void... params) {
				
					// Get a route from MyLocation to selected item
					RoutingService routingService = DefaultRoutingService.getRoutingService(getApplicationContext());
					//Point from = location.getPoint();
					//Point to = focusedMapItem.getCenter();
					try {
						
						Route r = routingService.getRoute(from, fromMapId, to, toMapId);
						//Route r = routingService.getRoute(from, EXAMPLE_MAP_ID,to, EXAMPLE_MAP_ID);
						return r;
						
						} catch (final IllegalArgumentException e) {
							
							// Tell the user why route could not be calculated. Use
							// handler to display toast on UI thread.
							handler.post(new Runnable() {
								public void run() {
									Toast.makeText(MapActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
								}
							});
							return null;
						}
				}
			
				@Override
				protected void onPostExecute(Route r) {
					mRoute = r;
					//Adding navigation buttons
					if (mRoute != null) {
						getMapView().displayRoute(mRoute);
						mMapController.animateTo(to);
						mRouteButton.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
						next_button.setVisibility(View.VISIBLE);
						mRouteButton.setVisibility(View.VISIBLE);
						prev_button.setVisibility(View.VISIBLE);
					}
					//Adding final flag
					List<Overlay> mapOverlays = mMapController.getOverlays();
					List<LinkedList<Node>> nodesList= r.getNodes();
					LinkedList<Node> LastNode = nodesList.get(nodesList.size()-1);
					Node node = LastNode.getLast();
					ItemizedOverlay overlay = new ItemizedOverlay();
					
					Point point = new Point(0, 0);
					point.setX(node.getX());
					point.setY(node.getY());
					Drawable finish_flag = getResources().getDrawable(R.drawable.flag_finish_icon);
					OverlayItem item = new OverlayItem(point, finish_flag, "Destination");
					overlay.addItem(item);
					mapOverlays.add(overlay);
					indexMapOverlay = mapOverlays.indexOf(overlay);
				}

			}.execute();
			
		} else {
			mRoute = null;
			getMapView().hideRoute();
			mRouteButton.setImageResource(android.R.drawable.ic_media_play);
			//test_button.setVisibility(View.GONE);
			next_button.setVisibility(View.GONE);
			mRouteButton.setVisibility(View.GONE);
			prev_button.setVisibility(View.GONE);
			List<Overlay> mapOverlays = mMapController.getOverlays();
			mapOverlays.remove(indexMapOverlay);		
		}
	}


	// Next button click
	public void next(View v) {
		if (mRoute != null) {
			getMapView().invalidate();
			RouteItem step = mRoute.next();
			mMapController.animateTo(step.getPoint());
			Toast.makeText(this,step.getInstruction() + " left: "
							+ mRoute.getDistanceLeft() + " covered: "
							+ mRoute.getDistanceCovered() + " total: "
							+ mRoute.length(), Toast.LENGTH_SHORT).show();
		}
	}

	// Previous button click
	public void prev(View v) {
		if (mRoute != null) {
			getMapView().invalidate();
			RouteItem step = mRoute.prev();
			Toast.makeText(this, step.getInstruction(), Toast.LENGTH_SHORT).show();
		}
	}
	
	/*
	 * Allow to add a button in ActionBar
	 */
    private class GetLocationClick extends AbstractAction {

        public GetLocationClick() {
            super(R.drawable.ic_menu_location);
        }
        public void performAction(View view) {
        	requestLocation();
        }

    }

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonCancelRoute:
			getRoute();
			break;
		case R.id.buttonNext:
			next(v);
			break;
		case R.id.buttonPrev:
			prev(v);
			break;
		case R.id.buttonChangeFloor:
			changeFloor();
			break;
		default:
			break;
		}

	}

	
}