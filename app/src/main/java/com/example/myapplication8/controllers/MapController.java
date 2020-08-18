package com.example.myapplication8.controllers;

import android.content.Context;
import android.location.Location;

import com.example.myapplication8.models.MapSingleton;
import com.example.myapplication8.utilities.Config;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class MapController
{
    private GoogleMapController gmapController;
    private OpenStreetMapController openStreetMapController;
    private List<Location> locationList;
    private double lastZoomLevel;
    private MapSingleton mapSingleton = MapSingleton.getInstance();
    private boolean allowShowResume = false;
    private Context context;
    private boolean updateInfoWindow;
    private double latitudeLongClick;
    private double longitudeLongClick;
    private boolean isLongPress;
    private boolean insideException;
    public double zoomLevelGoogle;
    public double zoomLevelOSM;
    private GoogleMap googleMap;
    private MapView mapView;

    public MapController( MapView mapView, Context context )
    {
        this.context = context;
        this.openStreetMapController = new OpenStreetMapController(mapView, context);
        this.gmapController = new GoogleMapController();
        this.mapView = mapView;
        locationList = new ArrayList<>();
    }

    public void setGoogleMap( Context context, GoogleMap googleMap )
    {
        this.context = context;
        this.gmapController.setGoogleMap(context, googleMap);
        this.googleMap = googleMap;
        CameraIdleListener cameraIdleListener = new CameraIdleListener(gmapController.getClusterManager());
        openStreetMapController.getMapView().addMapListener(cameraIdleListener);
        gmapController.getGoogleMap().setOnCameraIdleListener(cameraIdleListener);
    }

    public void registerEventMapWrapper()
    {
        if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            gmapController.registerEventGmap(registerEventGmap);
        }
        else
        {
            openStreetMapController.registerEventOsm(registerEventOsm);
        }
    }

    public GoogleMapController getGmapController()
    {
        return gmapController;
    }

    public OpenStreetMapController getOpenStreetMapController()
    {
        return openStreetMapController;
    }

    public void addLocation( Location location )
    {
        locationList.add(location);
    }

    public void setLastZoomLevel( double zoomLevel )
    {
        this.lastZoomLevel = zoomLevel;
    }

    public double getLastZoomLevel()
    {
        return lastZoomLevel;
    }

    public void zoomToBound( LatLngBounds centerBound )
    {
        if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            openStreetMapController.animateCameraToBound(centerBound);
        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            gmapController.animateCameraPosition(centerBound);
        }
    }


    public void addLocationPolyline( PolylineOptions polylineOptions, Polyline polyline )
    {
        if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            gmapController.addPolyLine(polylineOptions, polylineOptions.getColor());
        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            openStreetMapController.addPolyline(polyline);
        }
    }

    // Start Removing section
    public void clearMap()
    {
        if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            gmapController.clear();
        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            openStreetMapController.clear();
            openStreetMapController.clearClusterItem();
        }
    }

    public void clearMapAndShowWorld()
    {
        gmapController.clear();
        gmapController.showWorldMap();
        openStreetMapController.clear();
        openStreetMapController.showWorldOsm();

    }

    public void removeClusterMarkerItemOsm()
    {
        for( Marker currentMarker : openStreetMapController.getMarkerOsm() )
        {
            openStreetMapController.removeFromOsmMap(currentMarker);
        }
    }

    public void zoomToArea( double latitude, double longitude )
    {
        int zoomLevel = 13;
        if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            gmapController.zoomToArea(latitude, longitude, zoomLevel);
        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            openStreetMapController.setOSMZoomLevel(latitude, longitude, zoomLevel);
        }

    }

    private class CameraIdleListener implements GoogleMap.OnCameraIdleListener, MapListener
    {
        private ClusterManager clusterManager;

        private LatLngBounds.Builder builder;

        public CameraIdleListener( ClusterManager clusterManager )
        {
            this.clusterManager = clusterManager;
            builder = new LatLngBounds.Builder();
        }

        @Override
        public void onCameraIdle()
        {
            clusterManager.onCameraIdle();
            zoomLevelGoogle = getLastZoomLevel();
            zoomLevelOSM = getLastZoomLevel() + 3;
            lastZoomLevel = (int) googleMap.getCameraPosition().zoom - 2;

            if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
                lastZoomLevel = zoomLevelOSM;
            else if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
                lastZoomLevel = zoomLevelGoogle;
            setLastZoomLevel(getLastZoomLevel());

        }

        @Override
        public boolean onScroll( ScrollEvent event )
        {
            return false;
        }

        @Override
        public boolean onZoom( ZoomEvent event )
        {
            lastZoomLevel = event.getZoomLevel();

            zoomLevelGoogle = getLastZoomLevel() - 3;
            zoomLevelOSM = getLastZoomLevel();

            if( mapView != null )
            {
                IMapController mapController = mapView.getController();
                if( zoomLevelOSM <= mapView.getMinZoomLevel() )
                {
                    mapController.setZoom((double) 3);
                    setLastZoomLevel(0);
                }
                else if( getLastZoomLevel() >= mapView.getMinZoomLevel() )
                {
                    mapController.setZoom(zoomLevelOSM);
                    setLastZoomLevel(zoomLevelOSM - 3);
                }
            }
            if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
                lastZoomLevel = zoomLevelOSM;
            else if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
                lastZoomLevel = zoomLevelGoogle;
            setLastZoomLevel(getLastZoomLevel());
            return false;
        }
    }

    public GoogleMap.OnMapLongClickListener registerEventGmap = new GoogleMap.OnMapLongClickListener()
    {
        @Override
        public void onMapLongClick( LatLng latLng )
        {

        }
    };

    //map events osm
    public MapEventsReceiver registerEventOsm = new MapEventsReceiver()
    {
        @Override
        public boolean singleTapConfirmedHelper( GeoPoint geoPoint )
        {
            return false;
        }

        @Override
        public boolean longPressHelper( GeoPoint geoPoint )
        {
            return true;
        }
    };


    private void resetLocationMarker()
    {
        if( mapSingleton.getSelectedMap() != Config.GOOGLEMAP )
        {
            openStreetMapController.resetLocationMarker();
        }
    }

    public MapEventsReceiver getRegisterEventOsm()
    {
        return registerEventOsm;
    }

    public boolean isUpdateInfoWindow()
    {
        return updateInfoWindow;
    }

    public void setUpdateInfoWindow( boolean updateInfoWindow )
    {
        this.updateInfoWindow = updateInfoWindow;
    }

    public double getLatitudeLongClick()
    {
        return latitudeLongClick;
    }

    public double getLongitudeLongClick()
    {
        return longitudeLongClick;
    }

    public void setLongPress( boolean longPress )
    {
        isLongPress = longPress;
    }

    public boolean isInsideException()
    {
        return insideException;
    }

}
