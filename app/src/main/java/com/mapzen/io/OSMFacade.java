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
package com.mapzen.io;

import org.osmdroid.util.BoundingBoxE6;

import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.StatisticManager;
import com.mapzen.data.osm.Changeset;
import com.mapzen.data.osm.MapDataSet;
import com.mapzen.data.osm.OsmNode;
import com.mapzen.io.exceptions.ChangesetClosedException;
import com.mapzen.io.exceptions.OsmApiException;
import com.mapzen.io.exceptions.OsmTransferException;
/**
 * @author Vitalii Grygoruk
 *
 */
public class OSMFacade implements MapzenConstants {

	private static final String TAG = OSMFacade.class.getSimpleName();
	private OsmXmlReader _osmXmlReader;
	private static OsmApi _osmApi = OsmApi.getOsmApi();
	private static Changeset currentChangeset = Changeset.getInstance();
	
	public OSMFacade() {
		_osmXmlReader = new OsmXmlReader();	
		currentChangeset.loadFromPrefs();
	}

	
	/** 
	 * Get places of interest for specified BBox
	 *  
	 */
	public MapDataSet getPois(final double north, final double east, final double south, final double west ) {
		final String url = OSM_SERVER_ADDRESS+"/api/0.6/map?bbox="+west+","+south+","+east+","+north;
		return _osmXmlReader.downloadAndParse(url);
	}
	
	/** 
	 * Get places of interest for specified BBox
	 */
	public MapDataSet getPois(BoundingBoxE6 bbox) {
		
		return getPois(bbox.getLatNorthE6() / 1.E6, bbox
				.getLonEastE6() / 1.E6, bbox.getLatSouthE6() / 1.E6, bbox
				.getLonWestE6() / 1.E6);
	}
	
	public static boolean createNode(OsmNode poi) {

		if ((currentChangeset.isNew()) || (!currentChangeset.isOpen())) {
			try {
				openChangeset();
			} catch (OsmTransferException e) {
				e.printStackTrace();
				//Error while opening changeset
				return false;
			}
		}
		_osmApi.setChangeset(currentChangeset);
		
		try {
			_osmApi.createNode(poi);
			}
			catch (ChangesetClosedException e) {
				currentChangeset.setId(-1);
				currentChangeset.setOpen(false);
				createNode(poi);
			}
			catch (OsmApiException e) {
				//TODO: this is temporary. should be deleted for production
				currentChangeset.setId(-1);
				currentChangeset.setOpen(false);
				createNode(poi);
			}
			catch (OsmTransferException e) {
				//error while uploading data
				return false;
			}
		StatisticManager.logCreated();
		return true;
	}
	
	public static boolean updateNode(OsmNode poi) {
		if ((currentChangeset.isNew()) || (!currentChangeset.isOpen())) {
			try {
				openChangeset();
			} catch (OsmTransferException e) {
				e.printStackTrace();
				//Error while opening changeset
				return false;
			}
		}
		_osmApi.setChangeset(currentChangeset);
		
		try {
			_osmApi.modifyNode(poi);
			}
			catch (ChangesetClosedException e) {
				currentChangeset.setId(-1);
				currentChangeset.setOpen(false);
				updateNode(poi);
			}
			catch (OsmTransferException e) {
				//error while uploading data
				return false;
			}
		StatisticManager.logUpdated();	
		return true;
	}
	
	public static boolean removeNode(OsmNode poi) {
		if ((currentChangeset.isNew()) || (!currentChangeset.isOpen())) {
			try {
				openChangeset();
			} catch (OsmTransferException e) {
				e.printStackTrace();
				//Error while opening changeset
				return false;
			}
		}
		_osmApi.setChangeset(currentChangeset);
		
		try {
			_osmApi.deleteNode(poi);
			}
			catch (ChangesetClosedException e) {
				currentChangeset.setId(-1);
				currentChangeset.setOpen(false);
				removeNode(poi);
			}
			catch (OsmTransferException e) {
				//error while uploading data
				return false;
			}
			StatisticManager.logDeleted();
		return true;
	}
	
	/**
	 * Creates new changeset on OSM server and sets new id and "open" state values to Changeset object.
	 * @throws OsmTransferException
	 */
	private static void openChangeset() throws OsmTransferException {
		_osmApi.openChangeset(currentChangeset);
		currentChangeset.saveToPrefs();
	}
	
	
}
