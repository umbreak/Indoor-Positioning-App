package android.utils;

import android.ui.pojos.Picture;
import android.ui.pojos.ShortSite;
import android.ui.pojos.Site;
/**
 * public static attributes used to share information between activities.
 * In this case, between GETService and ExploreLargeActivity.*/

public class ActualSite {
	private static ActualSite actualSite;

	public static ActualSite getInstance() {
		if (actualSite == null)
			actualSite = new ActualSite();
		// to update the context each time a service helper it's called
		return actualSite;
	}
	public static Site site;
	public static ShortSite shortSite;
}
