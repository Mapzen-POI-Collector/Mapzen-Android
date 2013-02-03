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
package com.mapzen.io;

import org.osmdroid.util.BoundingBoxE6;

import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.osm.MapDataSet;

import android.util.Log;

/**
 * @author Vitalii Grygoruk
 * 
 */
@Deprecated
public class OSMClient implements MapzenConstants {

	private static final String TAG = "OSMClient";
	private OsmXmlReader xmlprocessor;

	public OSMClient() {
		xmlprocessor = new OsmXmlReader();
	}
	
	public MapDataSet getMapData(final double north, final double east, final double south, final double west ) {
		//GET api/0.6/map?bbox=left,bottom,right,top
		final String url = OSM_SERVER_ADDRESS+"/api/0.6/map?bbox="+west+","+south+","+east+","+north;
		Log.d(TAG, url);
		MapDataSet dataset = xmlprocessor.downloadAndParse(url);
		return dataset;
	}

	public MapDataSet getMapData(BoundingBoxE6 bbox) {
		return getMapData(bbox.getLatNorthE6() / 1.E6, bbox
				.getLonEastE6() / 1.E6, bbox.getLatSouthE6() / 1.E6, bbox
				.getLonWestE6() / 1.E6);
	}
}
