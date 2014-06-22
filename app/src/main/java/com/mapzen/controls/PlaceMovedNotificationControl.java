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

import com.mapzen.R;

import android.app.Application;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlaceMovedNotificationControl extends RelativeLayout {
	
	private Button cancelButton;
	private TextView textView;
	
	private Context context;
	
	public PlaceMovedNotificationControl(Context context) {	
		super(context);
		init(context);
	}
	
	public PlaceMovedNotificationControl(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context ctx) {
		context = ctx;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Application.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.place_moved_notification_control, null); 
		addView(layout);
		cancelButton = (Button)findViewById(R.id.place_moved_cancel_button_id);
		textView = (TextView)findViewById(R.id.place_moved_notification_poi_name_id);
	}
	
	public void setOnCancelButtonClickListener(OnClickListener l) {
		cancelButton.setOnClickListener(l);
	}
	
	public void setText(String text) {
		StringBuilder sb = new StringBuilder(context.getString(R.string.poi_was_moved))
			.append(": ").append((text != null) ? text : "");
		textView.setText(sb.toString());
	}
}
