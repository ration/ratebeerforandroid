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
    
    Originally part of smooth-wheel
    by Qixiang Chen
    http://code.google.com/p/smooth-wheel/
    
	Licensed under the Apache License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may obtain
	a copy of the License at
		http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package com.ratebeer.android.gui.wheel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.ratebeer.android.R;

public class WheelView<T> extends View {

	private static final int TEXT_SIZE = 34;
	private static final float RESISTANCE = 0.9f;
	private static final float SPRING = 0.3f;
	private static final float EP = 0.5f; // distance between two values before target move to next

	private WheelAdapter<T> _adp;
	
	private final NinePatchDrawable ind9;
	private Boolean update = false;
	private Boolean automove = false;
	private int targetPos;
	private int curPos;
	private float velocity = 0f;
	private float lastTouchY;
	private boolean disable = false;
	private int preNum;
	private int[] center;
	private int[] adpRange;
	
	private final float textSize;
	private float visible_offset;
	private float overflow;

	/**
	 * Constructor
	 */
	public WheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		// Set text size based on screen density
		final float density = getResources().getDisplayMetrics().density;
		textSize = TEXT_SIZE * density;
		visible_offset = 3 * textSize;
		overflow = textSize;
		
		Bitmap indbp = BitmapFactory.decodeResource(this.getResources(), R.drawable.wheel_ind);
		if (indbp != null) {
			this.ind9 = new NinePatchDrawable(this.getResources(), indbp,
					indbp.getNinePatchChunk(), new Rect(0, 0, 0, 0), null);
		} else {
			this.ind9 = null;
		}
		this.setBackgroundResource(R.drawable.wheel_bg);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		return super.dispatchTouchEvent(event);
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	public WheelAdapter<T> getAdapter() {
		return this._adp;
	}

	private int[] getCenter() {
		int[] res = { this.getWidth() / 2, this.getHeight() / 2 };
		return res;
	}

	private int[] getTargetOffset() {
		this.adpRange = new int[] { 0, 0 };
		this.center = this.getCenter();
		this.preNum = this._adp.getBeforeNum();

		float txtOffset = textSize;

		float upperLimit = this.preNum;// Math.min(_cutoff, pre);
		// int lowerLimit = postNum;// Math.min(_cutoff, post);

		// adp_range[0] = _adp.getCurIndex() - upperLimit;
		// adp_range[1] = _adp.getCurIndex() + lowerLimit;
		this.adpRange[0] = 0;
		this.adpRange[1] = this._adp.getSize();

		this.targetPos = (int) (this.center[1] - txtOffset * upperLimit
				+ textSize / 2F);

		return this.adpRange;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (disable)
			this.setBackgroundResource(R.drawable.wheel_disabled_bg);
		else
			this.setBackgroundResource(R.drawable.wheel_bg);

		Paint pText = new Paint();
		pText.setTextSize(textSize);
		pText.setAntiAlias(true);
		pText.setColor(Color.BLACK);
		pText.setTextAlign(Paint.Align.CENTER);
		// canvas.drawColor(Color.WHITE);

		if (this._adp == null) {
			return;
		}

		int[] center = this.getCenter();
		int[] adpRange = this.getTargetOffset();

		if (!this.update) {
			this.curPos = this.targetPos;
		}
		
		float y = this.curPos - 5;
		for (int i = adpRange[0]; i < adpRange[1]; i++) {
			if (y > (center[1] - visible_offset)
					&& y < (center[1] + visible_offset))
				canvas.drawText(_adp.getLabel(i), center[0], y, pText);
			y += textSize;

		}

		if (this.update) {
			if (this.curPos == this.targetPos) {
				this.update = false;
			} else if (this.automove == true) {
				// sprint back if velocity = 0
				float vmag = Math.abs(this.velocity);

				if (vmag < textSize / 2) {
					float delta = this.targetPos - this.curPos;
					this.curPos += WheelView.SPRING * delta;

				} else {

					this.velocity = this.velocity * WheelView.RESISTANCE;
					if (vmag < 0.1)
						this.velocity = 0;

					this.curPos += this.velocity;

					if (this.curPos - this.targetPos == 0
							|| this.isOverflow(this.velocity) != 0) {
						this.velocity = 0;
					}
				}
				this.updateTargetOffset();

			}
			this.invalidate();
		}

		int h9 = (int) (textSize / 2);

		this.ind9.setBounds(0, center[1] - h9, this.getWidth(), center[1] + h9);
		this.ind9.draw(canvas);

		// DEBUG
		// Paint ip = new Paint();
		// ip.setColor(Color.RED);
		// ip.setAlpha(220);
		// ip.setStrokeWidth(1);
		// canvas.drawLine(0, center[1], getWidth(), center[1], ip);
		// canvas.drawText("" + _adp.getCurIndex(), 10, 10, ip);
		// canvas.drawText("" + this.curPos + " -> " +this.targetPos, 10, 20,
		// ip);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int wmode = MeasureSpec.getMode(widthMeasureSpec);
		int hmode = MeasureSpec.getMode(heightMeasureSpec);
		int wsize = MeasureSpec.getSize(widthMeasureSpec);
		int hsize = MeasureSpec.getSize(heightMeasureSpec);

		int width = 60;
		int height = (int) (2 * textSize);

		if (wmode == MeasureSpec.EXACTLY) {
			width = wsize;
		}
		if (hmode == MeasureSpec.EXACTLY) {
			height = hsize;
		}

		this.visible_offset = height / 2 + textSize;

		this.setMeasuredDimension(width, height);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (disable)
			return true;

		this.getParent().requestDisallowInterceptTouchEvent(true);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.lastTouchY = event.getY();
			this.automove = false;
			// System.out.println("##" + lastTouchY);
			break;

		case MotionEvent.ACTION_MOVE:
			float delta = event.getY() - this.lastTouchY;

			if (this.isOverflow(delta) != 0) {
				this.velocity = 0;
			} else {
				this.curPos += delta;
				this.velocity = delta;
				this.updateTargetOffset();
			}

			this.lastTouchY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			this.getParent().requestDisallowInterceptTouchEvent(false);
			this.automove = true;
			break;

		}

		this.update = true;
		this.invalidate();

		return true;
	}

	private int isOverflow(float delta) {
		if (this._adp.getSelectedIndex() == 0 && delta > 0) {
			// I'm at top, and is scrolling up
			this.curPos = (int) Math.min(this.curPos + delta, this.targetPos
					+ this.overflow);
			return 1;

		} else if (this._adp.getSelectedIndex() == this._adp.getSize() - 1
				&& delta < 0) {
			// I'm at bottom and scrolling down
			this.curPos = (int) Math.max(this.curPos + delta, this.targetPos
					- this.overflow);
			return -1;
		}

		return 0;
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		return super.onTrackballEvent(event);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
	}

	private void updateTargetOffset() {
		if (this.curPos - this.targetPos > textSize * EP) {
			// if we have moved down 70% of the text size, then advance target
			// index
			this._adp.prevIndex();
			this.getTargetOffset();
			// System.out.println("##MOVE");
		} else if (this.curPos - this.targetPos < -1 * textSize * EP) {
			this._adp.nextIndex();
			this.getTargetOffset();
			// System.out.println("##UP");
		}
	}

	public void setAdapter(WheelAdapter<T> adp) {
		this._adp = adp;
		this.invalidate();
	}
	
	public void setDisabled(boolean state) {
		disable = state;
	
	}

}