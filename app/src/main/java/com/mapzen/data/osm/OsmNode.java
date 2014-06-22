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
package com.mapzen.data.osm;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;

import com.mapzen.R;
import com.mapzen.data.ResourceManager;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.util.Log;

/**
 * @author Vitalii Grygoruk
 */
public class OsmNode implements Tagged {

    //it should be initialized on app start.
    private static ResourceManager resourceManager = ResourceManager.getInstance();

    //we suppose that there are no duplicates of 'key' field for one POI.
    private Map<String, String> _tags = new HashMap<String, String>();
    private Map<String, String> _attributes = new HashMap<String, String>();

    public Point hotSpot = new Point();
    public Drawable icon;

    private long _id;
    private int _version;
    private IGeoPoint _coordinates;
    private IGeoPoint _previousCoordinates;
    private long _changesetId;

    // Properties, initialized from tags
    private String _name;
    private String _addr_housenumber;
    private String _addr_street;
    private String _website;
    private String _phone;
    private String _opening_hours;
    private String _description;

    // Properties, initialized from tags + mapzen configuration
    private String _type;
    private boolean _draggable = false;

    public String get_addr_housenumber() {
        return _addr_housenumber;
    }

    public void set_addr_housenumber(String addrHousenumber) {
        _addr_housenumber = checkString(addrHousenumber);
    }

    public String get_addr_street() {
        return _addr_street;
    }

    public void set_addr_street(String addrStreet) {
        _addr_street = checkString(addrStreet);
    }

    public String get_website() {
        return _website;
    }

    public void set_website(String website) {
        _website = checkString(website);
    }

    public String get_phone() {
        return _phone;
    }

    public void set_phone(String phone) {
        _phone = checkString(phone);
    }

    public String get_opening_hours() {
        return _opening_hours;
    }

    public void set_opening_hours(String openingHours) {
        _opening_hours = checkString(openingHours);
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String description) {
        _description = checkString(description);
    }

    public String getFullAddressString() {
        if ((_addr_housenumber == null) && (_addr_street == null))
            return null;

        String prefix = _addr_housenumber != null ? _addr_housenumber + " " : " ";
        String suffix = _addr_street != null ? _addr_street : "";

        return prefix + suffix;
    }

    /** Constructors */
    public OsmNode(final double aLatitude, final double aLongitude, Map<String, String> tags) {
        _coordinates = new GeoPoint(aLatitude, aLongitude);
        _tags = tags;
    }

    public OsmNode(final double aLatitude, final double aLongitude) {
        _coordinates = new GeoPoint(aLatitude, aLongitude);
    }

    public OsmNode(final int aLatitudeE6, final int aLongitudeE6, Map<String, String> tags) {
        this(aLatitudeE6/1.E6, aLongitudeE6/1.E6, tags);
    }

    public OsmNode(final Location aLocation, final Map<String, String> tags) {
        this(aLocation.getLatitude(), aLocation.getLongitude(), tags);
    }

    public OsmNode(final double aLatitude, final double aLongitude, final long id, final long changesetId, final int version) {
        this(aLatitude, aLongitude);
        _id = id;
        _changesetId = changesetId;
        _version = version;
    }

    public OsmNode(OsmNode poi) {
        if (poi._addr_housenumber != null)
            this._addr_housenumber = new String(poi._addr_housenumber);
        if (poi._addr_street != null)
            this._addr_street = new String(poi._addr_street);
        if (poi._attributes != null)
            this._attributes = new HashMap<String, String>(poi._attributes);
        this._changesetId = new Long(poi._changesetId);
        this._coordinates = new GeoPoint(poi._coordinates.getLatitudeE6(), poi._coordinates.getLongitudeE6());
        if (poi._description != null)
            this._description = new String(poi._description);
        this._draggable = new Boolean(poi._draggable);
        this._id = new Long(poi._id);
        if (poi._name != null)
            this._name = new String(poi._name);
        if (poi._opening_hours != null)
            this._opening_hours = new String(poi._opening_hours);
        if (poi._phone != null)
            this._phone = new String(poi._phone);
        if (poi._tags != null)
            this._tags = new HashMap<String, String>(poi._tags);
        if (poi._type != null)
            this._type = new String(poi._type);
        this._version = new Integer(poi._version);
        if (poi._website != null)
            this._website = new String(poi._website);
        this.hotSpot = new Point(poi.hotSpot);
    }

    public IGeoPoint getCoordinates() {
        return this._coordinates;
    }

    public void setCoordinates(final IGeoPoint gp) {
        this._coordinates = gp;
    }

    public IGeoPoint getPrevCoordinates() {
        return this._previousCoordinates;
    }

    public void setPrevCoordinates(final IGeoPoint gp) {
        this._previousCoordinates = gp;
    }

    public boolean isReadOnly() {
        boolean result = false;
        if (_type == null)
            result = true;
        else
            if ("unknown".equals(_type))
                result = true;
        return result;
    }

    public void onDraw(Canvas c, final Point curScreenCoords, float scaleFactor, boolean isDragged) {
        int left,right,top,bottom;
        float displayDensity = resourceManager.getDisplayDensity();
        float iconScaleFactor = scaleFactor*displayDensity;
        if (!isDragged) {
            if (this.getId() < 0) {
                iconScaleFactor = 1;
                icon = ResourceManager.getInstance().getDrawableResource(R.drawable.pin_new);
                //icon = ResourceManager.getInstance().getDrawableAsset("pin_new.png");
                hotSpot.set(icon.getIntrinsicWidth()/2, icon.getIntrinsicHeight());
            }
            else {
                iconScaleFactor = scaleFactor*displayDensity;
                String iconName = (_type != null) ? _type : "unknown";
                icon = ResourceManager.getInstance().getDrawableAsset("icons_16x16/"+iconName+".png");
                hotSpot.set((int)(icon.getIntrinsicWidth()/2),(int)(icon.getIntrinsicHeight()/2));

            }
            left = (int)(curScreenCoords.x - iconScaleFactor*hotSpot.x);
            right = (int)(left + iconScaleFactor*icon.getIntrinsicWidth());
            top = (int)(curScreenCoords.y - iconScaleFactor*hotSpot.y);
            bottom = (int)(top + iconScaleFactor*icon.getIntrinsicHeight());
            icon.setBounds(left, top, right, bottom);
            icon.draw(c);
        }
        else {
            if (this.getId() < 0) {
                //moving new POI
                icon = ResourceManager.getInstance().getDrawableResource(R.drawable.pin_move);
                //icon = ResourceManager.getInstance().getDrawableAsset("pin_move.png");
                hotSpot.set(icon.getIntrinsicWidth()/2, icon.getIntrinsicHeight()/2);
                left = (int)(curScreenCoords.x - hotSpot.x);
                right = (int)(left + icon.getIntrinsicWidth());
                top = (int)(curScreenCoords.y - hotSpot.y);
                bottom = (int)(top + icon.getIntrinsicHeight());
                icon.setBounds(left, top, right, bottom);
                icon.draw(c);
            } else {
                //moving poi, that was downloaded from OSM
                iconScaleFactor = displayDensity;
                String iconName = (_type != null) ? _type : "unknown";
                icon = ResourceManager.getInstance().getDrawableAsset("icons_16x16/"+iconName+".png");
                Drawable iconPin = ResourceManager.getInstance().getDrawableResource(R.drawable.pin_bottom);
                //Drawable iconPin = ResourceManager.getInstance().getDrawableAsset("pin_bottom.png");
                hotSpot.set(iconPin.getIntrinsicWidth()/2, iconPin.getIntrinsicHeight()/2);
                left = (int)(curScreenCoords.x - hotSpot.x);
                right = (int)(left + iconPin.getIntrinsicWidth());
                top = (int)(curScreenCoords.y - hotSpot.y);
                bottom = (int)(top + iconPin.getIntrinsicHeight());
                iconPin.setBounds(left, top, right, bottom);
                iconPin.draw(c);

                int left2 = (int)(curScreenCoords.x - icon.getIntrinsicWidth()/2*iconScaleFactor);
                icon.setBounds(left2,(int)(top-(icon.getIntrinsicHeight()-5)*iconScaleFactor), (int)(left2+icon.getIntrinsicWidth()*iconScaleFactor), (int)(top+5*iconScaleFactor));
                icon.draw(c);
            }
        }

    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o instanceof OsmNode) {
            OsmNode other = (OsmNode)o;
            result =
                (other.getId() == _id)
                && (other.getVersion() == _version)
                && (_name == null ? other.getName() == null : _name.equals(other.getName()))
                && (_type == null ? other.getType() == null : _type.equals(other.getType()))
                && (_description == null ? other.get_description() == null : _description.equals(other.get_description()))
                && (_addr_housenumber == null ? other.get_addr_housenumber() == null : _addr_housenumber.equals(other.get_addr_housenumber()))
                && (_addr_street == null ? other.get_addr_street() == null : _addr_street.equals(other.get_addr_street()))
                && (_opening_hours == null ? other.get_opening_hours() == null : _opening_hours.equals(other.get_opening_hours()))
                && (_phone == null ? other.get_phone() == null : _phone.equals(other.get_phone()))
                && (_website == null ? other.get_website() == null : _website.equals(other.get_website()))
                && (_coordinates == null ? other.getCoordinates() == null : _coordinates.equals(other.getCoordinates()))
                && (_tags == null ? other.getTags() == null : _tags.equals(other.getTags()));
        }
        return result;
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + (int)_id;
        hash = hash * 31 + _version;
        return hash;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = checkString(name);
    }

    public String getType() {
        return _type;
    }

    public void setType(String type) {
        _type = type;
    }

    public void setVersion(int version) {
        _version = version;
    }

    public int getVersion() {
        return _version;
    }

    public void addAttribute(final String key, final String value) {
        _attributes.put(key, value);
    }

    public Map<String, String> getAttributes() {
        return _attributes;
    }

    /*
     * Tagged interface implementation
     */
    public String get(String key) {
        return _tags.get(key);
    }

    public void put(final String key, final String value) {
        _tags.put(key, value);
    }

    public Map<String, String> getTags() {
        return _tags;
    }

    public void setTags(Map<String, String> tags) {
        _tags = tags;
    }

    public boolean hasTags() {
        return !_tags.isEmpty();
    }

    public Collection<String> keySet() {
        return _tags.keySet();
    }

    public void remove(String key) {
        _tags.remove(key);
    }

    public void removeAll() {
        _tags.clear();
    }

    public long getId() {
        return this._id;
    }

    public void setId(long id) {
        this._id = id;
    }

    public void setChangesetId(long changesetId) {
        this._changesetId = changesetId;
    }

    public void setOsmIdAndVersion(long id, int version) {
        this._id = id;
        this._version = version;
    }


    //helper
    private String checkString(String parameter) {
        if (parameter.length() == 0)
            parameter = null;
        return parameter;
    }

    public void revertCoordinates() {
        this.setCoordinates(new GeoPoint(_previousCoordinates.getLatitudeE6(), _previousCoordinates.getLongitudeE6()));
    }
}
