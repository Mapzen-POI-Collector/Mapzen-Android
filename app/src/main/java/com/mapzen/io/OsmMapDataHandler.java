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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.mapzen.configuration.OsmDriver;
import com.mapzen.data.osm.MapDataSet;
import com.mapzen.data.osm.OsmNode;
import com.mapzen.data.osm.OsmRelation;
import com.mapzen.data.osm.OsmWay;

public class OsmMapDataHandler extends DefaultHandler {

    private OsmNode currentPoi;
    private HashMap<Long, OsmNode> mPoiList;
    private LinkedList<OsmWay> mWaysList;
    private LinkedList<OsmRelation> mRelationsList;

    private boolean nodeFlag = false;
    private boolean wayFlag = false;
    private boolean relationFlag = false;
    private OsmWay currentWay;
    private OsmRelation currentRelation;

    private final static String nodeTag = "node";
    private final static String wayTag = "way";
    private final static String relationTag = "relation";
    private final static String ndTag = "nd";
    private final static String memberTag = "member";
    private final static String tag = "tag";

    private final static String keyAttr = "k";
    private final static String valueAttr = "v";
    private final static String refAtrr = "ref";
    private final static String typeAttr = "type";

    private final static String idAttr = "id";
    private final static String latAttr = "lat";
    private final static String lonAttr = "lon";
    private final static String changesetAttr = "changeset";
    private final static String versionAttr = "version";

    private static ArrayList<String> alreadyParsedAttributes = new ArrayList<String>();

    static {
        alreadyParsedAttributes.add(idAttr);
        alreadyParsedAttributes.add(latAttr);
        alreadyParsedAttributes.add(lonAttr);
        alreadyParsedAttributes.add(changesetAttr);
        alreadyParsedAttributes.add(versionAttr);
    }

    private static ArrayList<String> ignoredTags = new ArrayList<String>();
    static {
        ignoredTags.add("created_by");
    }

    private static ArrayList<String> mandatoryTags = new ArrayList<String>();
    static {
        mandatoryTags.add("shop");
        mandatoryTags.add("amenity");
        mandatoryTags.add("tourism");
        mandatoryTags.add("leisure");
        mandatoryTags.add("natural");
    }

    /*
     * private final static String visibleAttr = "visible"; private final static
     * String userAttr = "user"; private final static String uidAttr = "uid";
     * private final static String timestampAttr = "timestamp";
     */

    public OsmMapDataHandler() {
        mPoiList = new HashMap<Long, OsmNode>();
        mWaysList = new LinkedList<OsmWay>();
        mRelationsList = new LinkedList<OsmRelation>();
    }

    @Override
    public void startDocument() throws SAXException {
        Log.d("DOC_START", "doc_start");
    }

    @Override
    public void endDocument() throws SAXException {
        Log.d("DOC_END", "doc_end");
    }

    @Override
    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {

        String reuseString;

        if (localName.equals(nodeTag)) {
            nodeFlag = true;
            reuseString = atts.getValue(latAttr);
            double lat = Double.parseDouble(reuseString);
            reuseString = atts.getValue(lonAttr);
            double lon = Double.parseDouble(reuseString);
            reuseString = atts.getValue(changesetAttr);
            long changesetId = Long.parseLong(reuseString);
            reuseString = atts.getValue(versionAttr);
            int version = Integer.parseInt(reuseString);
            reuseString = atts.getValue(idAttr);
            long id = Long.parseLong(reuseString);
            currentPoi = new OsmNode(lat, lon, id, changesetId, version);
            for (int i = 0; i < atts.getLength(); i++) {
                if (!alreadyParsedAttributes.contains(atts.getLocalName(i)))
                    currentPoi.addAttribute(atts.getLocalName(i), atts.getValue(i));
            }
        } else if (localName.equals(wayTag)) {
            wayFlag = true;
            reuseString = atts.getValue(idAttr);
            long id = Long.parseLong(reuseString);
            currentWay = new OsmWay(id);
        } else if (localName.equals(relationTag)) {
            relationFlag = true;
            reuseString = atts.getValue(idAttr);
            long id = Long.parseLong(reuseString);
            currentRelation = new OsmRelation(id);
        } else if (localName.equals(tag) & nodeFlag) {
            if (atts.getValue(keyAttr).equals("name"))
                currentPoi.setName(atts.getValue(valueAttr));
            else if (atts.getValue(keyAttr).equals("description"))
                currentPoi.set_description(atts.getValue(valueAttr));
            else if (atts.getValue(keyAttr).equals("website"))
                currentPoi.set_website(atts.getValue(valueAttr));
            else if (atts.getValue(keyAttr).equals("phone"))
                currentPoi.set_phone(atts.getValue(valueAttr));
            else if (atts.getValue(keyAttr).equals("opening_hours"))
                currentPoi.set_opening_hours(atts.getValue(valueAttr));
            else if (atts.getValue(keyAttr).equals("addr:housenumber"))
                currentPoi.set_addr_housenumber(atts.getValue(valueAttr));
            else if (atts.getValue(keyAttr).equals("addr:street"))
                currentPoi.set_addr_street(atts.getValue(valueAttr));
            else if (ignoredTags.contains(atts.getValue(keyAttr)))
                return;
            else
                currentPoi
                        .put(atts.getValue(keyAttr), atts.getValue(valueAttr));
        } else if (localName.equals(ndTag) && wayFlag) {
            reuseString = atts.getValue(refAtrr);
            long id = Long.parseLong(reuseString);
            currentWay.addNodeId(id);
        } else if (localName.equals(memberTag) && relationFlag) {
            if (atts.getValue(typeAttr).equals("node")) {
                reuseString = atts.getValue(refAtrr);
                long id = Long.parseLong(reuseString);
                currentRelation.addNodeId(id);
            }
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        // Log.d("TAG_END", localName);
        if (localName.equals(nodeTag) && currentPoi.hasTags()) {
            currentPoi.setType(OsmDriver.getInstance().resolveType(
                    currentPoi.getTags()));
            if ((currentPoi.getType() != null) ||
                    (currentPoi.getTags().containsKey("amenity")))
                mPoiList.put(currentPoi.getId(), currentPoi);
            nodeFlag = false;
        }
        if (localName.equals(wayTag)) {
            wayFlag = false;
            mWaysList.add(currentWay);

        }
        if (localName.equals(relationTag)) {
            relationFlag = false;
            mRelationsList.add(currentRelation);
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        // nothing to do
    }

    public HashMap<Long, OsmNode> getPois() {
        return mPoiList;
    }

    public List<OsmWay> getWays() {
        return mWaysList;
    }

    public List<OsmRelation> getRelations() {
        return mRelationsList;
    }

    public MapDataSet getMapDataSet() {
        return new MapDataSet(mPoiList, mWaysList, mRelationsList);
    }
}
