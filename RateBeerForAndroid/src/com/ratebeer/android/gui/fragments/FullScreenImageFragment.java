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
package com.ratebeer.android.gui.fragments;

import android.widget.ImageView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.FragmentArg;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.DiscCacheUtil;
import com.nostra13.universalimageloader.core.assist.MemoryCacheUtil;
import com.ratebeer.android.R;
import com.ratebeer.android.app.RateBeerForAndroid;
import com.ratebeer.android.gui.components.RateBeerFragment;

@EFragment(R.layout.fragment_fullscreenphoto)
@OptionsMenu(R.menu.refresh)
public class FullScreenImageFragment extends RateBeerFragment {

	@FragmentArg
	@InstanceState
	protected String photoLowResUrl;
	@FragmentArg
	@InstanceState
	protected String photoHighResUrl;
	
	@ViewById
	protected ImageView fullscreenImage;

	public FullScreenImageFragment() {
	}
	
	@AfterViews
	public void init() {
		refreshImage();
	}

	@OptionsItem(R.id.menu_refresh)
	protected void onRefresh() {
		
		// Forcefully remove any cached version of this image and then ask for a refresh
		MemoryCacheUtil.removeFromCache(photoLowResUrl, ImageLoader.getInstance().getMemoryCache());
		MemoryCacheUtil.removeFromCache(photoHighResUrl, ImageLoader.getInstance().getMemoryCache());
		DiscCacheUtil.removeFromCache(photoLowResUrl, ImageLoader.getInstance().getDiscCache());
		DiscCacheUtil.removeFromCache(photoHighResUrl, ImageLoader.getInstance().getDiscCache());

		refreshImage();
		
	}

	private void refreshImage() {
		RateBeerForAndroid.getImageCache(getActivity()).displayImage(photoHighResUrl, fullscreenImage);
	}

}
