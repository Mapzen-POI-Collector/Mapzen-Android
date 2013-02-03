/*Copyright (c) 2011-2012, Cloudmade
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met: 

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution. 

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the FreeBSD Project.
*/
/**
 * 
 */
package com.mapzen.util;

import com.mapzen.R;
import com.mapzen.constants.MapzenConstants;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;

/**
 * @author Vitalii Grygoruk
 *
 */
public final class Callout implements MapzenConstants {
	private static Drawable callout_center;
	private static Drawable callout_left;
	private static Drawable callout_right;
	private static Drawable callout_fill;
	
	private static Paint mPaint;
	private static final float mTextSize = 18f; //to be scaled later
	
	private static float scale;

	static {	
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextAlign(Align.CENTER);
		mPaint.setColor(Color.WHITE);
		mPaint.setTypeface(Typeface.SANS_SERIF);
		mPaint.setFakeBoldText(true);
	}

	//TODO: refactor
	public static void drawCallout(final Canvas c, final Point basePointCoordinates, 
			final Context context, final String text) {

		scale = context.getResources().getDisplayMetrics().density;

		String calloutCaption = (text != null) ? text 
				: context.getString(R.string.calloutCaptionDefault);

		/* callout center */
		callout_center = context.getResources().getDrawable(R.drawable.callout_center);
		callout_left = context.getResources().getDrawable(R.drawable.callout_left);
		callout_right = context.getResources().getDrawable(R.drawable.callout_right);
		callout_fill = context.getResources().getDrawable(R.drawable.callout_fill);

		int left = basePointCoordinates.x - (int)(callout_center.getIntrinsicWidth()/2);
		int right = left + (int)(callout_center.getIntrinsicWidth());
		int bottom = basePointCoordinates.y;
		int top = bottom - (int)(callout_center.getIntrinsicHeight());

		callout_center.setBounds(left, top, right, bottom);
		callout_center.draw(c);

		/* callout text*/
		mPaint.setTextSize(mTextSize*scale);
		final int numberOfRenderedCharacters = mPaint.breakText(calloutCaption, true, maxCalloutWidth*scale, null);
		String textToRender = calloutCaption.substring(0, numberOfRenderedCharacters);
		int textWidth = Math.max((int) mPaint.measureText(textToRender), (int)(minCalloutWidth*scale));

		
		/* draw left callout fill */
		int c_f_left = basePointCoordinates.x - textWidth/2 + (int)(callout_left.getIntrinsicWidth()/4);
		int c_f_bottom = top + (int)(callout_fill.getIntrinsicHeight());
		callout_fill.setBounds(c_f_left, top, left, c_f_bottom);
		callout_fill.draw(c);
		
		/* draw right callout fill */
		int c_f_right = basePointCoordinates.x + textWidth/2 - (int)(callout_left.getIntrinsicWidth()/4);
		callout_fill.setBounds(right, top, c_f_right, c_f_bottom);
		callout_fill.draw(c);
		
		/* left callout ending */
		callout_left.setBounds(c_f_left - (int)(callout_left.getIntrinsicWidth()), top, c_f_left, c_f_bottom);
		callout_left.draw(c);
		
		/* right callout ending */
		callout_right.setBounds(c_f_right, top, (int)(c_f_right + callout_right.getIntrinsicWidth()), c_f_bottom);
		callout_right.draw(c);
		
		c.drawText(textToRender, basePointCoordinates.x - callout_left.getIntrinsicWidth()/2, top + (callout_left.getIntrinsicHeight())/2, mPaint);
	}
	
	
	public static Rect getCalloutActiveZone(final Point basePointCoordinates, 
			final Context context, final String text) {
		
		scale = context.getResources().getDisplayMetrics().density;
		
		String calloutCaption = (text != null) ? text 
				: context.getString(R.string.calloutCaptionDefault);
		
		int top =  basePointCoordinates.y - (int)(callout_center.getIntrinsicHeight());

		mPaint.setTextSize(mTextSize*scale);
		final int numberOfRenderedCharacters = mPaint.breakText(calloutCaption, true, maxCalloutWidth*scale, null);
		String textToRender = calloutCaption.substring(0, numberOfRenderedCharacters);
		int textWidth = Math.max((int) mPaint.measureText(textToRender), (int)(minCalloutWidth*scale));

		/* callout_fill left x coordinate */
		int c_f_left = basePointCoordinates.x - textWidth/2 + (int) (callout_left.getIntrinsicWidth()/4);
		
		int c_f_right = basePointCoordinates.x + textWidth/2 - (int) (callout_left.getIntrinsicWidth()/4);
		int active_right = c_f_right + (int)(callout_right.getIntrinsicWidth());
		int active_left = c_f_left - (int)(callout_left.getIntrinsicWidth());
		int active_bottom = top + (int)(callout_right.getIntrinsicHeight());
		
		Rect activeZone = new Rect(active_left, top, active_right, active_bottom);
		return activeZone;
	}
}
