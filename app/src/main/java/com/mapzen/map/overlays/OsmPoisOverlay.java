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
package com.mapzen.map.overlays;

import java.security.acl.LastOwnerException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.mapzen.MapzenPreferenceActivity;
import com.mapzen.R;
import com.mapzen.R.string;
import com.mapzen.constants.MapzenConstants;
import com.mapzen.data.SettingsManager;
import com.mapzen.data.osm.MapDataSet;
import com.mapzen.data.osm.OsmNode;
import com.mapzen.data.osm.OsmRelation;
import com.mapzen.data.osm.OsmWay;
import com.mapzen.io.OSMFacade;
import com.mapzen.util.Callout;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Draws a list of {@link com.mapzen.data.osm.OsmNode} above the map. The item with the lowest index is
 * drawn as last and therefore the 'topmost' marker. It also gets checked for
 * onTap first.
 *
 * @author Vitalii Grygoruk
 *
 */
public class OsmPoisOverlay extends Overlay implements
        MapzenConstants, MapListener {
    // ===========================================================
    // Constants
    // ===========================================================
    private static final String TAG = OsmPoisOverlay.class.getSimpleName();

    protected static final Point DEFAULTMARKER_HOTSPOT = new Point(13, 47);

    // ===========================================================
    // Fields
    // ===========================================================

    protected OnPoiCalloutTapListener mOnPoiCalloutTapListener;
    protected MapDataDownloadProgressListener progressListener;
    protected OnPoiMovedListener mOnPoiMovedListener;

    protected static HashMap<Long, OsmNode> _poisHashMap;
    protected static List<OsmWay> _waysList;
    protected static List<OsmRelation> _relationsList;

    private static long mActivePoiId = -10;
    private static boolean mActivePoiInDragMode = false;

    private static Context context;
    private static MapView mOsmv;

    private DownloadMapDataTask activeDownloadDataTask;
    private BoundingBoxE6 lastDownloadedBboxE6;

    // ===========================================================
    // Constructors
    // ===========================================================

    public OsmPoisOverlay(final Context ctx, MapView mapView,
            final OnPoiCalloutTapListener aOnItemTapListener,
            MapDataDownloadProgressListener mapDataDownloadProgressListener) {
        super(ctx);

        context = ctx;
        this.mOnPoiCalloutTapListener = aOnItemTapListener;
        this.progressListener = mapDataDownloadProgressListener;
        _poisHashMap = new HashMap<Long, OsmNode>();
        mOsmv = mapView;
        lastDownloadedBboxE6 = new BoundingBoxE6(0, 0, 0, 0);
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public void draw(final Canvas c, final MapView mapView, boolean drawShadow) {

        // don't draw overlay, if currentZoom is less then 17
        if (mapView.getZoomLevel() < minZoomForOsmDataDownload)
            return;

        final Projection pj = mapView.getProjection();
        final Point curScreenCoords = new Point();

        /*
         * Draw POIs
         */
        for (Entry<Long, OsmNode> poiEntry : _poisHashMap.entrySet()) {
            if (poiEntry.getKey() != mActivePoiId) {
                OsmNode item = poiEntry.getValue();
                pj.toMapPixels(item.getCoordinates(), curScreenCoords);
                item.onDraw(c, curScreenCoords, 1, false);
            }
        }

        /* Draw active POI and its callout / or dragged POI */
        if (_poisHashMap.containsKey(mActivePoiId)) {
            OsmNode item = _poisHashMap.get(mActivePoiId);
            pj.toMapPixels(item.getCoordinates(), curScreenCoords);

            if (mActivePoiInDragMode) {
                item.onDraw(c, curScreenCoords, poiScaleFactor, true);
            } else {
                item.onDraw(c, curScreenCoords, poiScaleFactor, false);
                if (item.getId() > 0) {
                    Point calloutBasePoint = new Point(curScreenCoords.x,
                            curScreenCoords.y - calloutVerticalOffset);
                    /* draw callout here */
                    Callout.drawCallout(c, calloutBasePoint, context, item
                            .getName());
                } else {
                    Point calloutBasePoint = new Point(curScreenCoords.x,
                            curScreenCoords.y - calloutVerticalOffset * 2);
                    /* draw callout here */
                    Callout.drawCallout(c, calloutBasePoint, context,
                            context.getString(R.string.calloutCaptionNewPoi));
                }
            }
        }

    }

    @Override
    public boolean onSingleTapUp(final MotionEvent event,
            final MapView mapView) {

        if (mapView.getZoomLevel() < minZoomForOsmDataDownload)
            return false;

        final Projection pj = mapView.getProjection();
        final int eventX = (int) event.getX();
        final int eventY = (int) event.getY();

        /* These objects are created to avoid construct new ones every cycle. */
        final Rect curMarkerBounds = new Rect();
        final Point curScreenCoords = new Point();
        final Point curScreenCoords2 = new Point();

        /* In case of active POI callout was tapped */
        if (_poisHashMap.containsKey(mActivePoiId)) {
            final OsmNode item = _poisHashMap.get(mActivePoiId);
            pj.toMapPixels(item.getCoordinates(), curScreenCoords);
            Point calloutBasePoint = new Point(curScreenCoords.x,
                    curScreenCoords.y - calloutVerticalOffset);
            Rect activeZone = Callout.getCalloutActiveZone(calloutBasePoint,
                    context, item.getName());
            pj.fromMapPixels(eventX, eventY, curScreenCoords2);
            if (activeZone.contains(curScreenCoords2.x, curScreenCoords2.y)) {
                this.onPoiCalloutTap(item);
                Log.d("Overlay","OnCalloutSingleTapUp");
                return true;
            }
        }

        for (final OsmNode item : _poisHashMap.values()) {

            pj.toMapPixels(item.getCoordinates(), curScreenCoords);

            final int left = (curScreenCoords.x - item.hotSpot.x);
            final int right = left + item.icon.getIntrinsicWidth();
            final int top = (curScreenCoords.y - item.hotSpot.y);
            final int bottom = top + item.icon.getIntrinsicHeight();
            curMarkerBounds.set(left - activeZoneAroundPoi, top
                    - activeZoneAroundPoi, right + activeZoneAroundPoi, bottom
                    + activeZoneAroundPoi);

            pj.fromMapPixels(eventX, eventY, curScreenCoords2);
            if (curMarkerBounds
                    .contains(curScreenCoords2.x, curScreenCoords2.y)) {
                if (onPoiTap(item.getId())) {
                    mActivePoiInDragMode = false;
                    Log.d("Overlay","OnPoiSingleTapUp");
                    mapView.invalidate();
                    return true;
                }
            }
        }
        return super.onSingleTapUp(event, mapView);
    }

    public boolean onLongPress(MotionEvent e, MapView mapView) {

        if (_poisHashMap.containsKey(mActivePoiId)) {
            final OsmNode item = _poisHashMap.get(mActivePoiId);

            if (!possibleToDrag(item.getId()))
                return false;

            final int eventX = (int) e.getX();
            final int eventY = (int) e.getY();

            final Projection pj1 = mapView.getProjection();
            final Rect curMarkerBounds = new Rect();
            final Point curScreenCoords = new Point();
            final Point curScreenCoords2 = new Point();

            pj1.toMapPixels(item.getCoordinates(), curScreenCoords);

            final int left = (curScreenCoords.x - item.hotSpot.x);
            final int right = left + item.icon.getIntrinsicWidth();
            final int top = (curScreenCoords.y - item.hotSpot.y);
            final int bottom = top + item.icon.getIntrinsicHeight();
            curMarkerBounds.set(left - activeZoneAroundPoi, top
                    - activeZoneAroundPoi, right + activeZoneAroundPoi, bottom
                    + activeZoneAroundPoi);

            pj1.fromMapPixels(eventX, eventY, curScreenCoords2);

            if (curMarkerBounds
                    .contains(curScreenCoords2.x, curScreenCoords2.y)) {
                mActivePoiInDragMode = true;
                item.setPrevCoordinates(item.getCoordinates());
                mapView.invalidate();
                Log.d("Overlay","onPoiLongPress");
                return true;
            }
        }
        return false;
    }

    // if event wasn't classified as gesture
    public boolean onTouchEvent(final MotionEvent event,
            final MapView mapView) {

        boolean result = false;
        OsmNode movedPoi = _poisHashMap.get(mActivePoiId);

        if (movedPoi == null)
            return false;

        switch (event.getAction()) {
        case MotionEvent.ACTION_MOVE:
            if (mActivePoiInDragMode) {
                Projection pj = mapView.getProjection();
                IGeoPoint gp = pj.fromPixels(event.getX(), event.getY()
                        - draggedPinVerticalOffset);
                movedPoi.setCoordinates(gp);
                mapView.invalidate();
                result = true;
            } else {
                result = false;
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_OUTSIDE:
            if ((mapView.getZoomLevel() >= minZoomForOsmDataDownload)) {
                boolean tempVar = mActivePoiInDragMode;
                mActivePoiInDragMode = false;
                result = true;
                mOsmv.postInvalidate();
                if ( tempVar && (movedPoi.getId() > 0)) {
                    if (mOnPoiMovedListener != null)
                        mOnPoiMovedListener.onPoiMoved(movedPoi);
                }
            }
            result = false;
            break;
        default:
            break;
        }
        return result;
    }


    /**
     * Should update data if necessary:
     * - if zoomed out
     * - if zoomed in from zoom < 17
     * - if current bbox contain area, which wasn't downloaded
     */
    public void tryUpdateData() {
        if (mOsmv.getZoomLevel() >= minZoomForOsmDataDownload) {
            BoundingBoxE6 mapBbox = mOsmv.getBoundingBox();
            if (!lastDownloadedBboxE6.contains(mapBbox.getCenter())) {
                if (activeDownloadDataTask != null) {
                    activeDownloadDataTask.cancel(true);
                }
                activeDownloadDataTask = new DownloadMapDataTask(mOsmv);
                activeDownloadDataTask.execute(mapBbox);
                mActivePoiId = -10;
            }
        }
    }

    // ===========================================================
    // Methods
    // ===========================================================


    private boolean onPoiTap(long pId) {

        if (mActivePoiId == pId)
            mActivePoiId = -10;
        else
            mActivePoiId = pId;

        return true;
    }

    private boolean onPoiCalloutTap(OsmNode item) {
        if (this.mOnPoiCalloutTapListener != null)
            return this.mOnPoiCalloutTapListener.onPoiCalloutTap(item);
        else
            return false;
    }

    /**
     * Method for adding new POI to overlay
     */
    public static void addPoi(OsmNode pPoi) {
        _poisHashMap.put(pPoi.getId(), pPoi);
    }

    /**
     * Method for adding POI List to overlay
     */
    public static void addPois(HashMap<Long, OsmNode> pPoiList) {
        _poisHashMap.putAll(pPoiList);
    }

    public static OsmNode getPoi(long id) {
        return _poisHashMap.get(id);
    }

    public static OsmNode getActivePoi() {
        return _poisHashMap.get(mActivePoiId);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
    public static interface OnPoiCalloutTapListener {
        public boolean onPoiCalloutTap(OsmNode aItem);
    }

    public static interface MapDataDownloadProgressListener {
        public void onMapDataDownloadFinished();

        public void onMapDataDownloadStarted();
    }

    public static interface OnPoiMovedListener {
        public boolean onPoiMoved(OsmNode movedPoi);
    }



    // field for determining, whether map data download finished or not.
    private static short indicator = 0;

    private class DownloadMapDataTask extends
            AsyncTask<BoundingBoxE6, Void, MapDataSet> {

        private BoundingBoxE6 bbox;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressListener != null) {
                progressListener.onMapDataDownloadStarted();
            }
            indicator++;
        }

        private MapView mOsmView;

        public DownloadMapDataTask(MapView pOsmView) {
            mOsmView = pOsmView;
        }

        @Override
        protected MapDataSet doInBackground(BoundingBoxE6... params) {
            bbox = params[0];
            return new OSMFacade().getPois(params[0]);
        }

        @Override
        protected void onPostExecute(MapDataSet dataset) {
            if (dataset != null) {
                mActivePoiId = -10;
                OsmNode backup = _poisHashMap.get(-1l);
                _poisHashMap.clear();
                if (backup != null)
                    addPoi(backup);
                addPois(dataset.getNodes());
                _waysList = dataset.getWays();
                _relationsList = dataset.getRelations();
                mOsmView.invalidate();
                lastDownloadedBboxE6 = bbox;
            } else {
                //Toast.makeText(context, R.string.err_msg_no_osm_connection,
                    //Toast.LENGTH_LONG).show();
            }
            if (!this.isCancelled())
                decreaseIndicatorValue();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            decreaseIndicatorValue();
        }

        private void decreaseIndicatorValue() {
            indicator--;
            if ((indicator == 0) && (progressListener != null))
                progressListener.onMapDataDownloadFinished();
        }

    }

    public static void setActivePoiId(long id) {
        mActivePoiId = id;
    }

    public static boolean possibleToDrag(long nodeId) {
        if (nodeId > 0) {
            if (getPoi(nodeId).getType() == null)
                return false;
            for (OsmWay way : _waysList) {
                if (way.contains(nodeId))
                    return false;
            }
            for (OsmRelation relation : _relationsList) {
                if (relation.contains(nodeId))
                    return false;
            }
        }
        return true;
    }

    public static void removePoi(long id) {
        _poisHashMap.remove(id);
    }

    public void setOnPoiMovedListener (OnPoiMovedListener listener) {
        mOnPoiMovedListener = listener;
    }


    /***********************************************
     * MapListener interface implementation
     ***********************************************/
    public boolean onScroll(ScrollEvent scrollEvent) {
        Log.d("MapListener", scrollEvent.toString());
        boolean isOsmDataDownloadAutomatic = SettingsManager.getInstance().getBoolean(MapzenPreferenceActivity.PREF_DOWNLOAD_OSM, true);
        if (isOsmDataDownloadAutomatic)
            tryUpdateData();
        return true;
    }

    public boolean onZoom(ZoomEvent zoomEvent) {
        Log.d("MapListener", zoomEvent.toString());
        boolean isOsmDataDownloadAutomatic = SettingsManager.getInstance().getBoolean(MapzenPreferenceActivity.PREF_DOWNLOAD_OSM, true);
        if(isOsmDataDownloadAutomatic)
            tryUpdateData();
        return true;
    }
}
