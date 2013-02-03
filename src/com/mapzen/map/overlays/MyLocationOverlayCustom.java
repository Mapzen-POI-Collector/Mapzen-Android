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

import java.util.LinkedList;

import com.mapzen.R;
import com.mapzen.R.drawable;
import com.mapzen.location.MyLocation;
import com.mapzen.location.MyLocation.LocationResult;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.NetworkLocationIgnorer;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapController;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.MapView.Projection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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

	private final MapView mMapView;
	private final MapController mMapController;
	private final LocationManager mLocationManager;
	private boolean mMyLocationEnabled = false;
	private LinkedList<Runnable> mRunOnFirstFix = new LinkedList<Runnable>();
	private final Point mMapCoords = new Point();

	private Location mLocation;
	protected boolean mFollow = true; // follow location updates
	private NetworkLocationIgnorer mIgnorer = new NetworkLocationIgnorer();

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
			//mMapView.invalidate(); // redraw the my location icon
			
		}
	};

	// ===========================================================
	// Constructors
	// ===========================================================

	public MyLocationOverlayCustom(final Context ctx,
			final MapView mapView) {
		super(ctx);
		mContext = ctx;
		mMapView = mapView;
		mLocationManager = (LocationManager) ctx
				.getSystemService(Context.LOCATION_SERVICE);
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
	
	public Location getLastFix() {
		return mLocation;
	}

	/**
	 * Return a GeoPoint of the last known location, or null if not known.
	 */
//	public GeoPoint getMyLocation() {
//		if (mLocation == null) {
//			return null;
//		} else {
//			return new GeoPoint(mLocation);
//		}
//	}

	public boolean isMyLocationEnabled() {
		return mMyLocationEnabled;
	}

	public boolean isLocationFollowEnabled() {
		return mFollow;
	}

	public void followLocation(boolean enable) {
		mFollow = enable;
	}

	// ===========================================================
	// Methods from SuperClass/Interfaces
	// ===========================================================
	
	public void draw(final Canvas c, final MapView osmv, boolean drawShadow) {
		if (this.mLocation != null) {
			final Projection pj = osmv.getProjection();
			pj.toMapPixels(new GeoPoint(mLocation), mMapCoords);
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

	
//	public void onLocationChanged(final Location location) {
//		if (DEBUGMODE) {
//			Log.d(TAG, "onLocationChanged(" + location + ")");
//		}
//
//		// ignore temporary non-gps fix
//		if (mIgnorer.shouldIgnore(location.getProvider(), System
//				.currentTimeMillis())) {
//			Log.d(TAG, "Ignore temporary non-gps location");
//			return;
//		}
//
//		mLocation = location;
//		if (mFollow) {
//			mMapController.setCenter(new GeoPoint(location));
//			mMapController.setZoom(17);
//			disableMyLocation();
//		} else {
//			mMapView.invalidate(); // redraw the my location icon
//		}
//	}

	
//	public void onProviderDisabled(String provider) {
//	}
//
//	
//	public void onProviderEnabled(String provider) {
//	}
//
//	
//	public void onStatusChanged(String provider, int status, Bundle extras) {
//		if (status == LocationProvider.AVAILABLE) {
//			final Thread t = new Thread(new Runnable() {
//				
//				public void run() {
//					for (Runnable runnable : mRunOnFirstFix) {
//						runnable.run();
//					}
//					mRunOnFirstFix.clear();
//				}
//			});
//			t.run();
//		}
//	}
	
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		//if (event.getAction() == MotionEvent.ACTION_MOVE)
			//mFollow = false;

		return super.onTouchEvent(event, mapView);
	}

	// ===========================================================
	// Methods
	// ===========================================================

//	public void disableMyLocation() {
//		mLocationManager.removeUpdates(this);
//		mMyLocationEnabled = false;
//	}
//
//	public boolean enableMyLocation() {
//		if (!mMyLocationEnabled) {
//			for (final String provider : mLocationManager.getAllProviders()) {
//				mLocationManager.requestLocationUpdates(provider, 0, 0, this);
//			}
//		}
//		return mMyLocationEnabled = true;
//	}
//
//	public boolean runOnFirstFix(Runnable runnable) {
//		if (mMyLocationEnabled) {
//			runnable.run();
//			return true;
//		} else {
//			mRunOnFirstFix.addLast(runnable);
//			return false;
//		}
//	}
//	
//	//TODO: current method is implemented with bugs
//	//unused. remove it.
//	public BoundingBoxE6 getBboxForCurrentAccuracy() {
//		final Projection pj = mMapView.getProjection();
//		pj.toMapPixels(new GeoPoint(mLocation), mMapCoords);
//	
//		final float radius = pj.metersToEquatorPixels(this.mLocation
//				.getAccuracy());
//		float left = mMapCoords.x-radius;
//		float right = mMapCoords.x+radius;
//		float top = mMapCoords.y-radius;
//		float bottom = mMapCoords.y+radius;
//		GeoPoint left_bottom = pj.fromPixels(left, bottom);
//		GeoPoint right_top = pj.fromPixels(right, top);
//		BoundingBoxE6 bbox = new BoundingBoxE6(right_top.getLatitudeE6(), right_top.getLongitudeE6(), left_bottom.getLatitudeE6(), left_bottom.getLongitudeE6());
//		return bbox;
//	}
	

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
}
