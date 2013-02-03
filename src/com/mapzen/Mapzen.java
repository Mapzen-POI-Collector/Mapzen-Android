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

package com.mapzen;

import org.acra.CrashReportingApplication;
import org.osmdroid.tileprovider.util.CloudmadeUtil;

import com.mapzen.configuration.OsmDriver;
import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.ResourceManager;
import com.mapzen.data.SettingsManager;
import com.mapzen.data.StatisticManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class Mapzen extends CrashReportingApplication implements MapzenConstants {

	private static SharedPreferences mSettings;
	
	public String getFormId() {
		return isCrashReportingEnabled ? "dFJ2ekYyOHp1WkJIVk11dnZnUWNKZlE6MQ" : "";
	}
	
	public Bundle getCrashResources() {
	    Bundle result = new Bundle();
	    if (isCrashReportingEnabled)
	    	result.putInt(RES_TOAST_TEXT, R.string.crash_toast_text);
	    return result;
	}
	
	public String getAppVersion() {
		String appVersion;
		try {
			appVersion = getPackageManager().getPackageInfo(
					getPackageName(), PackageManager.GET_META_DATA).versionName;
		} catch (NameNotFoundException e) {
			appVersion = "";
		}
		return appVersion;
	}

	private void initDataManagers() {
		ResourceManager.getInstance().init(getApplicationContext());
		OsmDriver.getInstance().init(
				getApplicationContext().getResources().openRawResource(
						R.raw.types),
				getApplicationContext().getResources().openRawResource(
						R.raw.category_to_type));
		
		mSettings = getApplicationContext().getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE);
		SettingsManager.getInstance().init(mSettings);
		
		StatisticManager.loadFromPreferences();

	}
	
	public static SharedPreferences getSharedPreferences() {
		return mSettings;
	}
	
	@Override
	public void onCreate() {
		// Retrieve CloudMade API key from Manifest
		CloudmadeUtil.retrieveCloudmadeKey(this.getApplicationContext());
		initDataManagers();
		super.onCreate();
	}
}
