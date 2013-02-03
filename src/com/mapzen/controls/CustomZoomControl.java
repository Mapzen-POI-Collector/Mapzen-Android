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

package com.mapzen.controls;

import com.mapzen.data.ResourceManager;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ZoomControls;


public class CustomZoomControl extends ZoomControls {

	public CustomZoomControl(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public CustomZoomControl(Context context) {
		super(context);
		this.setFocusable(false);
		this.setFocusableInTouchMode(false);
	}
	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// Ugly code : force buttons be equal size.
		float displayDensity = ResourceManager.getInstance().getDisplayDensity();
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		LayoutParams layoutParams = (android.widget.LinearLayout.LayoutParams) getChildAt(0).getLayoutParams();
		layoutParams.width = (int)(60*displayDensity) ;
		layoutParams = (android.widget.LinearLayout.LayoutParams) getChildAt(1).getLayoutParams();
		layoutParams.width = (int)(60*displayDensity) ;
		super.onMeasure(getMeasuredWidth(), getMeasuredHeight());
	}	
}