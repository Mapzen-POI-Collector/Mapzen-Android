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

import com.mapzen.R;
import com.mapzen.location.MyLocation;
import com.mapzen.location.MyLocation.LocationResult;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.location.Location;
import android.widget.Toast;

/**
 *
 * @author Manuel Stahl
 *
 */
public class MyLocationOverlayCustom extends Overlay {

    public static final String TAG = MyLocationOverlayCustom.class.getSimpleName();

    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    private final Context mContext;

    protected final Paint mPaint = new Paint();
    protected final Paint mCirclePaint = new Paint();
    protected final Bitmap CENTER_POINT_ICON;

    private final IMapController mMapController;
    private final Point mMapCoords = new Point();

    private Location mLocation;
    protected boolean mFollow = true; // follow location updates

    /** Coordinates the feet of the person are located. */
    protected final android.graphics.Point CENTER_POINT_HOTSPOT;

    private final LocationResult mLocationResult = new LocationResult() {

        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                mLocation = location;

                mMapController.setCenter(new GeoPoint(location));
                mMapController.setZoom(17);
            }
        }
    };

    // ===========================================================
    // Constructors
    // ===========================================================

    public MyLocationOverlayCustom(final Context ctx,
            final MapView mapView) {
        super(ctx);
        mContext = ctx;
        mMapController = mapView.getController();

        mCirclePaint.setARGB(0, 100, 100, 255);
        mCirclePaint.setAntiAlias(true);
        CENTER_POINT_ICON = BitmapFactory.decodeResource(ctx.getResources(),
                R.drawable.location_center);
        CENTER_POINT_HOTSPOT = new Point(CENTER_POINT_ICON.getWidth()/2, CENTER_POINT_ICON.getHeight()/2);
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public void getMyLocation() {
        MyLocation myLocation = new MyLocation();
        if (!myLocation.getLocation(mContext, mLocationResult))
            Toast.makeText(mContext, R.string.message_locationunavailable, Toast.LENGTH_SHORT).show();
    }

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    public void draw(final Canvas c, final MapView osmv, boolean drawShadow) {
        if (this.mLocation != null) {
            final MapView.Projection pj = osmv.getProjection();
            pj.toPixels(new GeoPoint(mLocation), mMapCoords);
            final float radius = pj.metersToEquatorPixels(this.mLocation
                    .getAccuracy());

            if (mFollow) {
                this.mCirclePaint.setAlpha(50);
                this.mCirclePaint.setStyle(Style.FILL);
                c.drawCircle(mMapCoords.x, mMapCoords.y, radius,
                        this.mCirclePaint);

                this.mCirclePaint.setAlpha(150);
                this.mCirclePaint.setStyle(Style.STROKE);
                c.drawCircle(mMapCoords.x, mMapCoords.y, radius,
                        this.mCirclePaint);

                c.drawBitmap(CENTER_POINT_ICON, mMapCoords.x
                        - CENTER_POINT_HOTSPOT.x, mMapCoords.y
                        - CENTER_POINT_HOTSPOT.y, this.mPaint);
            }
        }
    }
}
