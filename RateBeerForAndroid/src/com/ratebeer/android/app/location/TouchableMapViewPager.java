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
package com.ratebeer.android.app.location;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ratebeer.android.R;

/**
 * A custom ViewPager that allows touching of MapView elements. Loosely based on 
 * http://stackoverflow.com/a/7258579/243165 Will make sure that the ViewPager does
 * not handle any touch events that happen within a FrameLayout with R.id.map
 * @author erickok
 */
public class TouchableMapViewPager extends ViewPager {

	private int activePage = 0;

	public TouchableMapViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPageChangeListener(pageChangeListener );
    }   
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		View map = findViewById(R.id.map);
		if (map != null) {
			Rect rect = new Rect();
            map.getHitRect(rect);
            int pageOffset = getMeasuredWidth() * activePage ;
            if (rect.contains(pageOffset + (int) ev.getX(), (int) ev.getY())) {
                return false;
            }
		}
		return super.onInterceptTouchEvent(ev);
	}

	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			activePage = position;
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels2) {
			activePage = position;
		}
		
	};

	public OnPageChangeListener getOnPageChangeListener() {
		return pageChangeListener;
	}
	
}
