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

package com.mapzen.constants;

import android.app.Activity;

/**
 * @author Vitalii
 *
 */
public interface MapzenConstants {
	/* callout rendering constants */
	public static final int minCalloutWidth = 60;
	public static final int maxCalloutWidth = 200;
	public static final int calloutVerticalOffset = 20;
	
	public static final float poiScaleFactor = 1.3f; //applied for drawing of active poi
	public static final int activeZoneAroundPoi = 7; //7 px around poi for onTap action
	public static final int draggedPinVerticalOffset = 50; //50px
	
	public static final int minZoomForOsmDataDownload = 17;
	public static final int mapMovingDeadZone = 10; //px
	
	/* OAuth constants start */
//	public static final String OSM_SERVER_ADDRESS = "http://10.1.0.200:3010";
//	public static final String CONSUMER_KEY = "nJ8oWL2xiqeckDfCDtaI2g";
//	public static final String CONSUMER_SECRET = "0SoBtrSyuvxzR7SUWbISUwuauTryh8oNEBexRdyNXk";
//	public static final String OSM_API_BASE_URL = OSM_SERVER_ADDRESS+"/api";
	/* api06 test server*/
	public static final String OSM_SERVER_ADDRESS = "http://api06.dev.openstreetmap.org";
	public static final String CONSUMER_KEY = "4j3ARote3hTjVea6cmnMSgGn0LEehNCxdrgCdVfJ";
	public static final String CONSUMER_SECRET = "UKRafyuSIXETGmvKz5RUmxg10KH4Tklg955651Ce";
	public static final String OSM_API_BASE_URL = OSM_SERVER_ADDRESS+"/api";
	/* Production server settings */
//	public static final String OSM_SERVER_ADDRESS = "http://www.openstreetmap.org";
//	public static final String CONSUMER_KEY = "";
//	public static final String CONSUMER_SECRET = "";
//	public static final String OSM_API_BASE_URL = "http://api.openstreetmap.org/api";
	/* General OAuth settings */
	public static final String CALLBACK_URL = "mapzen://osm_callback";
	public static final String REQUEST_TOKEN_URL = OSM_SERVER_ADDRESS
			+ "/oauth/request_token";
	public static final String ACCESS_TOKEN_URL = OSM_SERVER_ADDRESS
			+ "/oauth/access_token";
	public static final String AUTHORIZATION_URL = OSM_SERVER_ADDRESS 
			+ "/oauth/authorize";
	/* OAuth string constants */
	public static final String REQUEST_TOKEN = "request_token";
	public static final String REQUEST_SECRET = "request_secret";
	public static final String ACCESS_TOKEN = "user_token";
	public static final String ACCESS_SECRET = "user_secret";
	/* OAuth constants end */
	
	public static final String OSM_API_VERSION = "0.6";
	
	public static final String OSM_CREATOR_INFO = "Mapzen POI Collector for Android 0.5";

	
	public static final String PREFERENCES_FILE = "Mapzen.preferences";
	public static final String PREFS_ZOOM_LEVEL = "startZoom";
	public static final String PREFS_SCROLL_X = "startX";
	public static final String PREFS_SCROLL_Y = "startY";
	
	
	public static final int EXIT_APP_ACTIVITY_RESULT = Activity.RESULT_FIRST_USER+1;
	public static final int POI_SAVED_ACTIVITY_RESULT = EXIT_APP_ACTIVITY_RESULT+1;
	public static final int POI_DELETED_ACTIVITY_RESULT = POI_SAVED_ACTIVITY_RESULT+1;
	public static final int POI_CANCELED_ACTIVITY_RESULT = POI_DELETED_ACTIVITY_RESULT+1;
	public static final int OSM_LOGIN_ACTIVITY_AUTHORIZED_RESULT = POI_CANCELED_ACTIVITY_RESULT+1;
	
	public static final int OSM_LOGIN_ACTIVITY_REQUEST_CODE = 200;
	public static final int EDIT_POI_ACTIVITY_REQUEST_CODE = OSM_LOGIN_ACTIVITY_REQUEST_CODE+1;
	public static final int VIEW_POI_ACTIVITY_REQUEST_CODE = EDIT_POI_ACTIVITY_REQUEST_CODE+1;
	public static final int SELECT_POI_CATEGORY_REQUEST_CODE = VIEW_POI_ACTIVITY_REQUEST_CODE+1;
	public static final int SELECT_POI_SUB_TYPE_REQUEST_CODE = SELECT_POI_CATEGORY_REQUEST_CODE+1;
	
	
	public static final boolean isAdMobEnabled = false;
	public static final boolean isCrashReportingEnabled = true;
}
