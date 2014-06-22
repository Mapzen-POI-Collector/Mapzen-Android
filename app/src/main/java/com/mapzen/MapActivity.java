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

import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.views.MapView;

import com.mapzen.constants.MapzenConstants;
import com.mapzen.controls.CustomZoomControl;
import com.mapzen.controls.LoadingPoisIndicator;
import com.mapzen.controls.PlaceMovedNotificationControl;
import com.mapzen.controls.ZoomInToAddPois;
import com.mapzen.data.SettingsManager;
import com.mapzen.data.StatisticManager;
import com.mapzen.data.osm.OsmNode;
import com.mapzen.io.OSMFacade;
import com.mapzen.map.overlays.MyLocationOverlayCustom;
import com.mapzen.map.overlays.OsmPoisOverlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MapActivity extends Activity implements
        OsmPoisOverlay.OnPoiCalloutTapListener,
        OsmPoisOverlay.MapDataDownloadProgressListener,
        OsmPoisOverlay.OnPoiMovedListener, MapzenConstants, MapListener {

    private static final String TAG = MapActivity.class.getSimpleName();

    /** Main map view */
    private static MapView mOsmv;

    /** Overlay for my current location */
    private static MyLocationOverlayCustom mMyLocationOverlay;
    /** Overlay for OSM POIs */
    private static OsmPoisOverlay mPoisOverlay;

    /** Progress indicator control for map data download */
    private LoadingPoisIndicator progressIndicator;
    private ZoomInToAddPois zoomInToAddPoisView;
    private CustomZoomControl zoomControl;
    private PlaceMovedNotificationControl placeMovedNotification;

    /** Indicates, that user canceled moving of POI */
    private static boolean wasCanceled = false;

    private static final int DIALOG_ABOUT = 0;

    protected Dialog onCreateDialog(int id) {
        final AlertDialog dialog;
        AlertDialog.Builder builder;
        Context mContext = getApplicationContext();
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout;

        switch (id) {
        case DIALOG_ABOUT:
            layout = inflater.inflate(R.layout.about_dialog,
                    (ViewGroup) findViewById(R.id.about_dialog_layout_root));
            String appVersion = ((Mapzen)this.getApplication()).getAppVersion();
            builder = new AlertDialog.Builder(this).setView(layout).setTitle(
                    getString(R.string.app_name) + " " + appVersion)
                    .setPositiveButton(R.string.dialog_ok_button,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    removeDialog(DIALOG_ABOUT);

                                }
                            }).setNeutralButton(
                            R.string.dialog_view_license_button,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    Intent i = new Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("http://creativecommons.org/licenses/by-sa/2.0/"));
                                    startActivity(i);
                                }
                            });
            TextView statsData = (TextView) layout
                    .findViewById(R.id.placed_added_edited_deleted_value_id);
            statsData.setText(StatisticManager.getCreated() + "/"
                    + StatisticManager.getUpdated() + "/"
                    + StatisticManager.getDeleted());

            dialog = builder.create();
            break;
        default:
            dialog = null;
            break;
        }
        return dialog;
    }

    /** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mOsmv = (MapView) findViewById(R.id.mapViewId);
        mOsmv.setMultiTouchControls(true);

        //Set MapListener (zoom & scroll events)
        DelayedMapListener dml = new DelayedMapListener(this, 50);
        mOsmv.setMapListener(dml);

        // Set basic coordinates & zoom
        mOsmv.getController().setZoom(
                SettingsManager.getInstance().getInt(PREFS_ZOOM_LEVEL, 1));
        mOsmv.scrollTo(SettingsManager.getInstance().getInt(PREFS_SCROLL_X, 0),
                SettingsManager.getInstance().getInt(PREFS_SCROLL_Y, 0));

        // MyLocation overlay
        mMyLocationOverlay = new MyLocationOverlayCustom(this
                .getApplicationContext(), mOsmv);
        mOsmv.getOverlayManager().add(mMyLocationOverlay);

        // OsmPois overlay
        mPoisOverlay = new OsmPoisOverlay(this.getApplicationContext(), mOsmv,
                this, this);
        mPoisOverlay.setOnPoiMovedListener(this);
        mOsmv.getOverlayManager().add(mPoisOverlay);

        // other controls initialization
        progressIndicator = (LoadingPoisIndicator) findViewById(R.id.loadingProgressIndicatorId);
        zoomInToAddPoisView = (ZoomInToAddPois) findViewById(R.id.zoomInToAddPoisViewId);
        zoomInToAddPoisView
                .setVisibility(mOsmv.getZoomLevel() < minZoomForOsmDataDownload ? View.VISIBLE
                        : View.INVISIBLE);
        zoomInToAddPoisView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MapActivity.mOsmv.getController().setZoom(17);

            }
        });

        zoomControl = (CustomZoomControl) findViewById(R.id.customZoomControlId);
        zoomControl.setOnZoomOutClickListener(new OnClickListener() {

            public void onClick(View v) {
                mOsmv.getController().zoomOut();

//					Animation pinAddedAnimation = AnimationUtils.loadAnimation(MapActivity.this, R.anim.pin_drop);
//					zoomInToAddPoiImageView.startAnimation(pinAddedAnimation);
//
            }
        });
        zoomControl.setOnZoomInClickListener(new OnClickListener() {

            public void onClick(View v) {
                mOsmv.getController().zoomIn();
            }
        });

        placeMovedNotification = (PlaceMovedNotificationControl) findViewById(R.id.place_moved_notification_control_id);
        placeMovedNotification.setOnCancelButtonClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                wasCanceled = true;
                placeMovedNotification.setVisibility(View.GONE);
            }
        });

        if (mOsmv.getZoomLevel() >= minZoomForOsmDataDownload)
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    boolean isOsmDataDownloadAutomatic = SettingsManager.getInstance().getBoolean(MapzenPreferenceActivity.PREF_DOWNLOAD_OSM, true);
                    if(isOsmDataDownloadAutomatic)
                        mPoisOverlay.tryUpdateData();
                }
            }, 500);
    }

    public void onPause() {
        super.onPause();
        //mMyLocationOverlay.disableMyLocation();
    }

    protected void onStop() {
        SettingsManager.getInstance().putInt(PREFS_ZOOM_LEVEL,
                mOsmv.getZoomLevel());
        SettingsManager.getInstance()
                .putInt(PREFS_SCROLL_X, mOsmv.getScrollX());
        SettingsManager.getInstance()
                .putInt(PREFS_SCROLL_Y, mOsmv.getScrollY());
        StatisticManager.saveToPreferences();
        super.onStop();
    }

    /* Constructs menu from resources */
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        boolean isOsmDataDownloadAutomatic = SettingsManager.getInstance().getBoolean(MapzenPreferenceActivity.PREF_DOWNLOAD_OSM, true);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(isOsmDataDownloadAutomatic ? R.menu.main_menu : R.menu.main_menu_custom, menu);
        return true;
    }

    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.my_location:
            mMyLocationOverlay.getMyLocation();
            return true;
        case R.id.add_poi:
            addNewPoi();
            return true;
        case R.id.settings_menu_item_id:
            Intent i = new Intent(MapActivity.this, MapzenPreferenceActivity.class);
            startActivity(i);
            return true;
        case R.id.about_application:
            showDialog(DIALOG_ABOUT);
            return true;
        case R.id.refresh_osm_data_menu_item_id:
            mPoisOverlay.tryUpdateData();
            return true;
        }
        return false;
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        return mOsmv.onTrackballEvent(event);
    }

    /** OsmPoisOverlay.OnPoiTapListener event handler */

    public boolean onPoiCalloutTap(OsmNode aItem) {

        long id = aItem.getId();
        Intent i = new Intent();
        i.setClass(this.getApplicationContext(),
                ((id < 0) ? EditPoiActivity.class : ViewPoiActivity.class));
        i.putExtra("id", aItem.getId());
        startActivity(i);
        return true;
    }

    public void onMapDataDownloadFinished() {
        progressIndicator.setVisibility(View.GONE);
    }

    public void onMapDataDownloadStarted() {
        progressIndicator.setVisibility(View.VISIBLE);

    }

    private void addNewPoi() {
        if (mOsmv.getZoomLevel() >= minZoomForOsmDataDownload) {
            int latE6 = mOsmv.getMapCenter().getLatitudeE6();
            int lonE6 = mOsmv.getMapCenter().getLongitudeE6();
            OsmNode newNode = new OsmNode(latE6 / 1.E6, lonE6 / 1.E6, -1l, -1l,
                    0);
            OsmPoisOverlay.addPoi(newNode);
            OsmPoisOverlay.setActivePoiId(newNode.getId());
            mOsmv.invalidate();
        }
    }

    @Override
    public boolean onPoiMoved(final OsmNode movedPoi) {
        placeMovedNotification.setText(movedPoi.getName());
        placeMovedNotification.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                placeMovedNotification.setVisibility(View.GONE);
                if (!wasCanceled) {
                    OSMFacade.updateNode(movedPoi);
                } else {
                    movedPoi.revertCoordinates();
                }
                wasCanceled = false;
                mOsmv.invalidate();
            }
        }, 3000);

        Log.d(TAG, "OnPoiMoved");
        return true;
    }


    /******************************************
     * MapListener interface implementation
     ******************************************/
    @Override
    public boolean onScroll(ScrollEvent event) {
        return mPoisOverlay.onScroll(event);
    }

    @Override
    public boolean onZoom(ZoomEvent event) {

        if (event.getZoomLevel() < minZoomForOsmDataDownload) {
            progressIndicator.setVisibility(View.GONE);
            zoomInToAddPoisView.setVisibility(View.VISIBLE);
        } else
            zoomInToAddPoisView.setVisibility(View.GONE);

        zoomControl.setIsZoomInEnabled(mOsmv.canZoomIn());
        zoomControl.setIsZoomOutEnabled(mOsmv.canZoomOut());

        return mPoisOverlay.onZoom(event);
    }
}
