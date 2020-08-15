/*
package com.example.myapplication8.controllers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.ContextCompat;

import com.firstwap.celltrax.R;
import com.firstwap.celltrax.activities.MainActivity;
import com.firstwap.celltrax.models.Cell;
import com.firstwap.celltrax.models.ClusteredCenterMarker;
import com.firstwap.celltrax.models.ProfileSettingSingleton;
import com.firstwap.celltrax.models.Wifi;
import com.firstwap.celltrax.ui.CenterMarkerRenderer1;
import com.firstwap.celltrax.ui.CenterMarkerRenderer2;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.MarkerManager;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.GridBasedAlgorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.clustering.algo.PreCachingAlgorithmDecorator;

public class GoogleMapController
{

    private GoogleMap googleMap;
    private Marker currentLocationMarker;

    private ClusterManager<ClusteredCenterMarker> clusterManager1;
    private ClusterManager<ClusteredCenterMarker> clusterManager2;

    private MainActivity mainActivity;
    private ProfileSettingSingleton profileSettingSingleton;

    public GoogleMapController()
    {
    }

    public void setGoogleMap( MainActivity mainActivity, final GoogleMap googleMap )
    {
        this.googleMap = googleMap;
        this.mainActivity = mainActivity;
        profileSettingSingleton = ProfileSettingSingleton.getInstance();
        MarkerManager markerManager = new MarkerManager(googleMap);
        clusterManager1 = new ClusterManager<>(mainActivity, this.googleMap, markerManager);
        clusterManager2 = new ClusterManager<>(mainActivity, this.googleMap, markerManager);
        ClusterController clusterController = new ClusterController(mainActivity);
        this.googleMap.setOnMarkerClickListener(markerManager);
        this.googleMap.setInfoWindowAdapter(markerManager);

        //info window will be shown if cluster clicked
        clusterManager1.setOnClusterClickListener(clusterController);
        clusterManager1.setOnClusterInfoWindowClickListener(clusterController);
        clusterManager1.getClusterMarkerCollection().setOnInfoWindowAdapter(clusterController);
        clusterManager1.setRenderer(new CenterMarkerRenderer1(mainActivity, this.googleMap, clusterManager1));
        clusterManager1.setAlgorithm(new NonHierarchicalDistanceBasedAlgorithm<ClusteredCenterMarker>());

        clusterManager2.setOnClusterClickListener(clusterController);
        clusterManager2.setOnClusterInfoWindowClickListener(clusterController);
        clusterManager2.getClusterMarkerCollection().setOnInfoWindowAdapter(clusterController);
        clusterManager2.setRenderer(new CenterMarkerRenderer2(mainActivity, this.googleMap, clusterManager2));
        clusterManager2.setAlgorithm(new PreCachingAlgorithmDecorator<>(new GridBasedAlgorithm<ClusteredCenterMarker>()));
    }

    public void addColorToAreaWithZoom( double latitude, double longitude )
    {
        if( null == googleMap )
        {
            return;
        }

        LatLng latLng = new LatLng(latitude, longitude);

        float zoomLevelGoogle = (googleMap.getCameraPosition().zoom);
        if( MainActivity.isScanning && MainActivity.firstTimeScanning )
        {
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            googleMap.animateCamera(yourLocation);
            MainActivity.firstTimeScanning = false;
        }
        else if( MainActivity.isScanning )
        {
            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevelGoogle);
            googleMap.animateCamera(yourLocation);
        }
    }

    public Circle addColorToAreaWithType( double latitude, double longitude, double accuracy, int color, int outliner )
    {
        if( null == googleMap )
        {
            return null;
        }

        CircleOptions circleOptions;
        LatLng current = new LatLng(latitude, longitude);
        circleOptions = new CircleOptions().center(current).radius(accuracy).fillColor(color).strokeColor(outliner).strokeWidth(3f);
        return googleMap.addCircle(circleOptions);

    }

    public void zoomToArea( double latitude, double longitude, int zoomLevel )
    {
        if( null == googleMap )
        {
            return;
        }
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoomLevel);
        googleMap.animateCamera(yourLocation);
        googleMap.moveCamera(yourLocation);
    }

    public Polygon drawPolygonOnMap( PolygonOptions polygonOptions )
    {
        if( null == googleMap )
        {
            return null;
        }
        return googleMap.addPolygon(polygonOptions);
    }

    public Circle addCellLocation( Cell cell, int color, double radius, int outline )
    {
        if( null == googleMap )
        {
            return null;
        }

        CircleOptions co = new CircleOptions();

        LatLng position = new LatLng(cell.getLatitude(), cell.getLongitude());
        co.center(position);
        co.radius(radius);
        co.fillColor(color);
        co.strokeWidth(3f);
        co.strokeColor(outline);
        return googleMap.addCircle(co);

    }

    public Circle addWifiLocation( Wifi wifi, int color, double radius, int outlinecolor )
    {
        if( null == googleMap )
        {
            return null;
        }

        CircleOptions co = new CircleOptions();
        LatLng position = new LatLng(wifi.getLatitude(), wifi.getLongitude());
        co.center(position);
        co.radius(radius);
        co.fillColor(color);
        co.strokeWidth(3f);
        co.strokeColor(outlinecolor);
        return googleMap.addCircle(co);

    }

    public Polyline addPolyLine( PolylineOptions polylineOptions, int color )
    {
        if( null == googleMap )
        {
            return null;
        }

        polylineOptions.color(color);
        return googleMap.addPolyline(polylineOptions);

    }

    public void animateCameraPosition( LatLngBounds bounds )
    {
        if( null == googleMap )
        {
            return;
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));
    }

    public void clear()
    {
        if( null == googleMap )
        {
            return;
        }
        googleMap.clear();
        clearAllCluster();
    }

    // check scanning mode collect all data from latitude longitude then compare with data that already set in scanning mode then draw.
    public Marker addLocationArrowMarker( LatLng point, int marker )
    {
        if( null == googleMap )
        {
            return null;
        }

        Location loc = new Location("location");
        loc.setLatitude(point.latitude);
        loc.setLongitude(point.longitude);

        LatLng current = new LatLng(point.latitude, point.longitude);
        MarkerOptions centerMarkerOptions = new MarkerOptions();
        Drawable drawable = ContextCompat.getDrawable(mainActivity, marker);
        BitmapDescriptor bitmapDescriptor = getMarkerIconFromDrawable(drawable);
        centerMarkerOptions.icon(bitmapDescriptor);
        centerMarkerOptions.position(current);
        //we can use this to show where user pointing maybe for later
        //if(lastlong!= 0 && lastLat !=0 )
        //{
        //    centerMarkerOptions.rotation(before.getBearing());
        //}
        centerMarkerOptions.anchor(0.5f, 0.25f);

        return googleMap.addMarker(centerMarkerOptions);

    }

    private BitmapDescriptor getMarkerIconFromDrawable( Drawable drawable )
    {
        Drawable background = ContextCompat.getDrawable(mainActivity, R.drawable.arrow_background);
        background.setBounds(0, 0, background.getIntrinsicWidth(), background.getIntrinsicHeight());
        drawable.setBounds(5, 5, drawable.getIntrinsicWidth() + 3, drawable.getIntrinsicHeight() + 3);
        drawable.setColorFilter(profileSettingSingleton.getOverviewLocation().getCenterPointIcon().getColor(), PorterDuff.Mode.SRC_IN);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(), background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public void addLocationPinPointMarker( LatLng latLng )
    {
        addLocationArrowMarker(latLng, R.drawable.arrow);
    }

    // Move to point 0,0 and zoom out to the minimum level
    public void showWorldMap()
    {
        if( googleMap != null )
        {
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(0, 0), googleMap.getMinZoomLevel())));
        }
    }

    public GoogleMap getGoogleMap()
    {
        return googleMap;
    }

    public ClusterManager getClusterManager1()
    {
        return clusterManager1;
    }

    public ClusterManager getClusterManager2()
    {
        return clusterManager2;
    }

    public void addClusterItem1( ClusteredCenterMarker clusteredCenterMarker )
    {
        if( null == clusterManager1 )
        {
            return;
        }
        if( clusteredCenterMarker != null )
        {
            clusterManager1.addItem(clusteredCenterMarker);
            clusterManager1.cluster();
        }
    }

    public void addClusterItem2( ClusteredCenterMarker clusteredCenterMarker )
    {
        if( null == clusterManager2 )
        {
            return;
        }
        if( clusteredCenterMarker != null )
        {
            clusterManager2.addItem(clusteredCenterMarker);
            clusterManager2.cluster();
        }
    }

    public void removeClusterItem1( ClusteredCenterMarker marker )
    {
        if( null == clusterManager1 )
        {
            return;
        }
        clusterManager1.removeItem(marker);
        clusterManager1.cluster();
    }

    public void removeClusterItem2( ClusteredCenterMarker marker )
    {
        if( null == clusterManager2 )
        {
            return;
        }
        clusterManager2.removeItem(marker);
        clusterManager2.cluster();
    }

    public void clearAllCluster()
    {
        if( null == clusterManager1 || null == clusterManager2 )
        {
            return;
        }
        clusterManager1.clearItems();
        clusterManager1.cluster();
        clusterManager2.clearItems();
        clusterManager2.cluster();
    }

    public void getMyCurrentLocation( Location location )
    {

        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        if( null != currentLocationMarker )
        {
            currentLocationMarker.remove();
        }

        currentLocationMarker = addLocationArrowMarker(point, R.drawable.arrow);
        zoomToArea(location.getLatitude(), location.getLongitude(), (int) googleMap.getCameraPosition().zoom);

    }

    public void registerEventGmap( GoogleMap.OnMapLongClickListener registerEvent )
    {
        googleMap.setOnMapLongClickListener(registerEvent);
    }

}
*/
