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

package com.mapzen.data;

import java.io.IOException;

import com.mapzen.R;
import com.mapzen.R.drawable;
import com.mapzen.R.string;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class ResourceManager {
	private static final String TAG = ResourceManager.class.getSimpleName();
	
	private static Context _baseContext;
	private static final ResourceManager _instance = new ResourceManager();
	private static AssetManager assetManager;
	
	private ResourceManager() {};
	
	public static ResourceManager getInstance() {
		return _instance;
	}

	public void init(Context context) {
		_baseContext = context;
		assetManager = context.getAssets();
	}
	
	public String getStringResource(String name) {
		
		int resId;
		String result = null;
		try {
			resId = Integer.parseInt(R.string.class.getField(name).get(null).toString());
			result = _baseContext.getString(resId);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e(TAG, "ResourceManager is not initialized",e);
		}
		return result;
	}
	
	public Drawable getDrawableResource(String name) {
		int resId;
		Drawable result = null;
		try {
			resId = Integer.parseInt(R.drawable.class.getField(name).get(null).toString());
			result = _baseContext.getResources().getDrawable(resId);	
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			Log.e(TAG, "ResourceManager is not initialized",e);
		}
		return result;
	}
	
	public BitmapDrawable getDrawableAsset(String path) {
		BitmapDrawable result = null;
		try {
			result = (BitmapDrawable)Drawable.createFromStream(assetManager.open(path), path);
			result.setTargetDensity(getDensityDpi());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public Drawable getDrawableResource(int resId) {
		return _baseContext.getResources().getDrawable(resId);
	}
	
	public float getDisplayDensity() {
		return _baseContext.getResources().getDisplayMetrics().density;
	}
	
	public int getDensityDpi() {
		return _baseContext.getResources().getDisplayMetrics().densityDpi;
	}
}
