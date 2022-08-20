package buaa.uavswarm.urbanmissionmap.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.duke.udp.UDPReceiveHelper;
import com.duke.udp.UDPSendHelper;
import com.duke.udp.util.UDPListener;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import buaa.uavswarm.urbanmissionmap.R;
import buaa.uavswarm.urbanmissionmap.util.MarkerWithLabel;
import egolabsapps.basicodemine.offlinemap.Interfaces.GeoPointListener;
import egolabsapps.basicodemine.offlinemap.Interfaces.MapListener;
import egolabsapps.basicodemine.offlinemap.Utils.MapUtils;
import egolabsapps.basicodemine.offlinemap.Views.OfflineMapView;

import static buaa.uavswarm.urbanmissionmap.util.ByteTransFormUtil.bytesToHex;
import static buaa.uavswarm.urbanmissionmap.util.ByteTransFormUtil.hexToByteArray;
import static java.lang.Math.pow;


public class AnimateThread implements Runnable{
    private MapView mMapview;
    public Marker marker;
    public GeoPoint geopoint;
    public float yaw;
    public AnimateThread( MapView mMapview_,Marker marker_, GeoPoint geopoint_, float yaw_)
    {
        this.mMapview = mMapview_;
        this.marker = marker_;
        this.geopoint = geopoint_;
        this.yaw = yaw_;
    }
    public void run()
    {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMapview.getProjection();
        Point startPoint = proj.toPixels(marker.getPosition(), null);
        final IGeoPoint startGeoPoint = proj.fromPixels(startPoint.x, startPoint.y);
        final long duration = 300;
        final Interpolator interpolator = new LinearInterpolator();
        marker.setRotation(90-yaw);//飞机航向角

        long elapsed = SystemClock.uptimeMillis() - start;
        float t = interpolator.getInterpolation((float) elapsed / duration);
        double lng = t * geopoint.getLongitude() + (1 - t) * startGeoPoint.getLongitude();
        double lat = t * geopoint.getLatitude() + (1 - t) * startGeoPoint.getLatitude();
        //显示轨迹
        Marker marker1 = new Marker(mMapview);
        marker1.setAnchor((float)0.5, (float)0.5);
        marker1.setPosition(new GeoPoint(lat, lng));
        //marker1.setIcon(getDrawable(R.drawable.cyantrack));
        mMapview.getOverlays().add(marker1);

        //marker.setRotation(90-yaw);//飞机航向角
        marker.setPosition(new GeoPoint(lat, lng));//移动飞机
        if (t < 1.0) {
            handler.postDelayed(this, 15);
        }
        mMapview.postInvalidate();
    }
}

