/*
    This file is part of RateBeer For Android.
    
    RateBeer for Android is free software: you can redistribute it 
    and/or modify it under the terms of the GNU General Public 
    License as published by the Free Software Foundation, either 
    version 3 of the License, or (at your option) any later version.

    RateBeer for Android is distributed in the hope that it will be 
    useful, but WITHOUT ANY WARRANTY; without even the implied warranty 
    of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with RateBeer for Android.  If not, see 
    <http://www.gnu.org/licenses/>.
 */
package com.ratebeer.android.app;

import java.io.File;

import android.app.Application;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.cache.disc.impl.FileCountLimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.ratebeer.android.api.RateBeerApi;

public class RateBeerForAndroid extends Application {

	public static final String LOG_NAME = "RateBeerForAndroid";

	public static final String DEFAULT_FILES_DIR = Environment.getExternalStorageDirectory().toString()
			+ "/RateBeerForAndroid";

	private ApplicationSettings settings;
	private RateBeerApi api;
	private ImageLoader imageCache;

	/**
	 * Returns the application-wide user settings
	 * @return The user settings object
	 */
	public ApplicationSettings getSettings() {
		if (settings == null) {
			settings = new ApplicationSettings(getApplicationContext(),
					PreferenceManager.getDefaultSharedPreferences(this));
		}
		return settings;
	}

	/**
	 * Returns the API to the ratebeer.com website
	 * @return The RateBeer API object
	 */
	public RateBeerApi getApi() {
		if (api == null) {
			api = new RateBeerApi(getSettings().getUserSettings());
		}
		return api;
	}

	public ImageLoader getImageCache() {
		if (imageCache == null) {
			imageCache = ImageLoader.getInstance();
			File imageCacheDir = new File(RateBeerForAndroid.DEFAULT_FILES_DIR + "/cache/");
			imageCacheDir.mkdirs();
			imageCache.init(new ImageLoaderConfiguration.Builder(this)
					.defaultDisplayImageOptions(new DisplayImageOptions.Builder().cacheInMemory().cacheOnDisc().
							imageScaleType(ImageScaleType.IN_SAMPLE_INT).build())
					.memoryCache(new UsingFreqLimitedMemoryCache(2 * 1024 * 1024))
					.discCache(new FileCountLimitedDiscCache(imageCacheDir, new Md5FileNameGenerator(), 25)).build());
		}
		return imageCache;
	}

}
