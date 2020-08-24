
package com.example.myapplication8.controllers;

import android.content.Context;
import android.location.Location;

import com.example.myapplication8.models.ClusteredCenterMarker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.collections.MarkerManager;

public class GoogleMapController
{

    private GoogleMap googleMap;
    private Marker marker;

    private ClusterManager<ClusteredCenterMarker> clusterManager;

    private Context context;

    public GoogleMapController()
    {
    }

    public void setGoogleMap( Context context, final GoogleMap googleMap )
    {
        this.googleMap = googleMap;
        this.context = context;
        MarkerManager markerManager = new MarkerManager(googleMap);
        clusterManager = new ClusterManager<>(context, this.googleMap, markerManager);
        this.googleMap.setOnMarkerClickListener(markerManager);
        this.googleMap.setInfoWindowAdapter(markerManager);

    }

    public void addColorToAreaWithZoom( double latitude, double longitude )
    {
        if( null == googleMap )
        {
            return;
        }

        LatLng latLng = new LatLng(latitude, longitude);

        float zoomLevelGoogle = (googleMap.getCameraPosition().zoom);

        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevelGoogle);
        googleMap.animateCamera(yourLocation);

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

    public ClusterManager getClusterManager()
    {
        return clusterManager;
    }

    public void addClusterItem( ClusteredCenterMarker clusteredCenterMarker )
    {
        if( null == clusterManager )
        {
            return;
        }
        if( clusteredCenterMarker != null )
        {
            clusterManager.addItem(clusteredCenterMarker);
            clusterManager.cluster();
        }
    }

    public void removeClusterItem( ClusteredCenterMarker marker )
    {
        if( null == clusterManager )
        {
            return;
        }
        clusterManager.removeItem(marker);
        clusterManager.cluster();
    }


    public void clearAllCluster()
    {
        if( null == clusterManager )
        {
            return;
        }
        clusterManager.clearItems();
        clusterManager.cluster();
    }

    public void getMyCurrentLocation( Location location )
    {

        LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
        if( null != marker )
        {
            marker.remove();
        }

        zoomToArea(location.getLatitude(), location.getLongitude(), (int) googleMap.getCameraPosition().zoom);

    }

    public void registerEventGmap( GoogleMap.OnMapLongClickListener registerEvent )
    {
        googleMap.setOnMapLongClickListener(registerEvent);
    }

}
