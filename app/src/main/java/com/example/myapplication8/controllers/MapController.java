package com.example.myapplication8.controllers;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;


import com.example.myapplication8.activities.MainActivity;
import com.example.myapplication8.models.MapSingleton;
import com.example.myapplication8.utilities.Config;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class MapController
{
    private GoogleMapController gmapController;
    private OpenStreetMapController openStreetMapController;
    private List<Location> locationList;
    private double lastZoomLevel;
    private MapSingleton mapSingleton = MapSingleton.getInstance();
    private boolean allowShowResume = false;
    private MainActivity mainActivity;
    private PinLocationProvider pinLocationProvider;
    private boolean updateInfoWindow;
    private double latitudeLongClick;
    private double longitudeLongClick;
    private boolean isLongPress;
    private boolean insideException;
    public double zoomLevelGoogle;
    public double zoomLevelOSM;
    private GoogleMap googleMap;
    private MapView mapView;

    public MapWrapper( MapView mapView, MainActivity mainActivity )
    {
        this.mainActivity = mainActivity;
        this.openStreetMapController = new OpenStreetMapController(mapView, mainActivity);
        this.gmapController = new GoogleMapController();
        this.mapView = mapView;
        locationList = new ArrayList<>();
    }

    public void setGoogleMap( MainActivity activity, GoogleMap googleMap )
    {
        this.mainActivity = activity;
        this.gmapController.setGoogleMap(activity, googleMap);
        this.googleMap = googleMap;
        CameraIdleListener cameraIdleListener = new CameraIdleListener(gmapController.getClusterManager1(), gmapController.getClusterManager2());
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

    public void addCircleWithZoom( Location location )
    {
        if( null != pinLocationProvider && (null != pinLocationProvider.getPinLocation()) )
        {
            resetLocationMarker();
        }

        addLocationArrowMarker(location.getLatitude(), location.getLongitude(), R.drawable.arrow);

        if( mainActivity.getMapToolsController().isViewingSession() )
        {
            LatLngBounds.Builder builder = mainActivity.getLeftSessionController().getScannedListFragment().getBuilder();
            builder.include(new LatLng(location.getLatitude(), location.getLongitude()));
            zoomToBound(builder.build());
            return;
        }

        if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            openStreetMapController.addCircleWithZoom(location.getLatitude(), location.getLongitude(), location.getAccuracy());
        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            gmapController.addColorToAreaWithZoom(location.getLatitude(), location.getLongitude());
        }

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

    // Radio drawing section
    private void drawCellOnMap( CellCalculation calculation, SettingRadio radio )
    {
        ArrayList<Cell> cellList = calculation.getCellList();
        Iterator<Cell> cellIterator = cellList.iterator();
        Cell currentCell;
        double radius;

        while ( cellIterator.hasNext() )
        {
            currentCell = cellIterator.next();
            radius = currentCell.getSignalStrength();
            if( radius > radio.getMaxCell() )
            {
                radius = radio.getMaxCell();
            }
            else if( radius < radio.getMinCell() )
            {
                radius = radio.getMinCell();
            }
            radius = radius + 103;
            currentCell.setAccuracy(radius);

            if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
            {
                calculation.addCircleOsm(openStreetMapController.addColorToArea(currentCell.getLatitude(), currentCell.getLongitude(), radius, radio.getMeasuredRadio().getColor(), radio.getOutlineColor()));
            }
            else if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
            {
                calculation.addCircle(gmapController.addColorToAreaWithType(currentCell.getLatitude(), currentCell.getLongitude(), radius, radio.getMeasuredRadio().getColor(), radio.getOutlineColor()));
            }
        }
    }

    public void drawClusterOsmOnMap()
    {
        openStreetMapController.clearClusterOsm();
        openStreetMapController.zoomClusterMarker();
        openStreetMapController.showClusterOsm();
    }

    public void clearClusterOsmOnMap()
    {
        openStreetMapController.clearClusterOsm();
    }

    private void drawWifiOnMap( CellCalculation calculation, SettingRadio radio )
    {
        ArrayList<Wifi> wifiList = calculation.getWifiList();
        Iterator<Wifi> wifiIterator = wifiList.iterator();
        Wifi currentWifi;
        double radius;

        while ( wifiIterator.hasNext() )
        {
            currentWifi = wifiIterator.next();
            radius = currentWifi.getSignalStrength();

            // Wifi range in dBm is -100 (minimum) to -10 (maximum)
            if( radius > radio.getMaxCell() )
            {
                radius = radio.getMaxCell();
            }
            else if( radius < radio.getMinCell() )
            {
                radius = radio.getMinCell();
            }
            radius = radius + 103;
            if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
            {
                if( currentWifi.getLatitude() != 0 && currentWifi.getLongitude() != 0 )
                {
                    calculation.addCircleOsm(openStreetMapController.addColorToArea(currentWifi.getLatitude(), currentWifi.getLongitude(), radius, radio.getMeasuredRadio().getColor(), radio.getOutlineColor()));
                }

            }
            else if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
            {
                if( currentWifi.getLatitude() != 0 && currentWifi.getLongitude() != 0 )
                {
                    calculation.addCircle(gmapController.addColorToAreaWithType(currentWifi.getLatitude(), currentWifi.getLongitude(), radius, radio.getMeasuredRadio().getColor(), radio.getOutlineColor()));
                }
            }
        }
    }

    public void drawRadioBasedOnProfile( CellCalculation cellCalculation, SettingRadio radio, int radioType )
    {
        int radioCenterPolygonColor;
        int radioCenterEllipseColor;
        int radioOutlineColor = radio.getOutlineColor();

        if( radio.getMeasuredRadio().isShowing() )
        {
            if( radioType == Global.SCANNED_TYPE_CELL )
            {
                drawCellOnMap(cellCalculation, radio);
            }
            else if( radioType == Global.SCANNED_TYPE_WIFI )
            {
                drawWifiOnMap(cellCalculation, radio);
            }
        }

        if( radio.getCalculatedAreaIcon().isShowing() )
        {
            if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
            {
                if( cellCalculation.getClusteredMarker().getType() == Global.SCANNED_TYPE_CELL )
                {
                    gmapController.addClusterItem1(cellCalculation.getClusteredMarker());
                }
                else if( cellCalculation.getClusteredMarker().getType() == Global.SCANNED_TYPE_WIFI )
                {
                    gmapController.addClusterItem2(cellCalculation.getClusteredMarker());
                }
            }
            else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
            {
                Marker markerOsm = openStreetMapController.addItemMarker(cellCalculation.getClusteredMarker());
                cellCalculation.setCenterMarkerOptionsOsm(markerOsm);
                openStreetMapController.addListClusterMarker(markerOsm);

            }
        }

        if( radio.getCalculatedAreaEllipse().isShowing() )
        {
            radioCenterEllipseColor = radio.getCalculatedAreaEllipse().getColor();

            if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
            {
                PolygonOptions polygonOptions = cellCalculation.getCenterPolygonOptions(radioCenterEllipseColor, radioOutlineColor);
                if( polygonOptions != null )
                {
                    cellCalculation.setCenterPolygon(gmapController.drawPolygonOnMap(polygonOptions));
                }
            }
            else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
            {
                org.osmdroid.views.overlay.Polygon centerPolygonOsm = cellCalculation.getCenterPolygonOptionsOsm(openStreetMapController.getContext(), radioCenterEllipseColor, radioOutlineColor);
                cellCalculation.setCenterPolygonOsm(centerPolygonOsm);
                openStreetMapController.drawOnOsmMap(centerPolygonOsm);
            }
        }

        if( radio.getCalculatedAreaPolygon().isShowing() )
        {
            radioCenterPolygonColor = radio.getCalculatedAreaPolygon().getColor();

            if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
            {
                PolygonOptions po = cellCalculation.setHullPolylineOptions(radioCenterPolygonColor, radioOutlineColor);
                if( po != null && po.getPoints().size() > 0 )
                {
                    cellCalculation.setPolyline(gmapController.drawPolygonOnMap(po));
                }
            }
            else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
            {
                Polyline polyline = cellCalculation.getHullPolylineOptionsOsm(radioCenterPolygonColor);
                cellCalculation.setPolylineOsm(openStreetMapController.addPolyline(polyline));
            }

        }
    }

    public void drawLocationBasedOnProfile( MeasuredLocation measuredLocation, SettingLocation location )
    {

        SettingSubProfileItem gpsAcc = location.getGpsAccuracy();
        SettingSubProfileItem radioCount = location.getLocationRadioCount();
        SettingSubProfileItem centerPoint = location.getCenterPointIcon();

        int gpsAccuracyColor;
        int cellColor;
        int wifiColor;
        int outlineColor;

        gpsAccuracyColor = gpsAcc.getColor();
        cellColor = radioCount.getColor();
        wifiColor = location.getRadioCountWifiColor();
        outlineColor = location.getOutlineColor();


        // Draw GPS Accuracy
        if( gpsAcc.isShowing() )
        {
            addMeasuredLocation(measuredLocation, gpsAccuracyColor, true, false, outlineColor);
        }

        // Draw Radio Count
        if( radioCount.isShowing() )
        {
            addMeasuredLocation(measuredLocation, cellColor, false, true, outlineColor);
            addMeasuredLocation(measuredLocation, wifiColor, false, false, outlineColor);
        }

        // Draw Center Point icon
        if( centerPoint.isShowing() )
        {
            addLocationArrowMarker(measuredLocation.getLatitude(), measuredLocation.getLongitude(), R.drawable.arrow);
        }
    }

    // End of Radio Drawing section

    // Location Drawing section
    public void addMeasuredLocation( MeasuredLocation location, int color, boolean radiusByAccuracy, boolean isCell, int outlineColor )
    {
        double radius;
        if( radiusByAccuracy )
        {
            radius = location.getAccuracy();
        }
        else
        {
            if( isCell )
            {
                radius = location.getNumCell();
            }
            else
            {
                radius = location.getNumWifi();
            }
        }


        if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            gmapController.addColorToAreaWithType(location.getLatitude(), location.getLongitude(), radius, color, outlineColor);

        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            if( location.getLatitude() != 0 && location.getLongitude() != 0 )
            {
                openStreetMapController.addColorToArea(location.getLatitude(), location.getLongitude(), radius, color, outlineColor);
            }
        }
    }

    public void addLocationArrowMarker( double latitude, double longitude, int marker )
    {
        Drawable icon = ResourcesCompat.getDrawable(openStreetMapController.getContext().getResources(), marker, null);
        if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            gmapController.addLocationArrowMarker(new LatLng(latitude, longitude), marker);
        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            Marker osmMarker = openStreetMapController.addLocationMarker(latitude, longitude, icon);
            if( null != mainActivity.getGooglePlacesController().getPlace() )
            {
                osmMarker.setOnMarkerClickListener(mainActivity.getGooglePlacesController());
            }
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
        if(MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
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

    public void removeLocationBasedOnSelectedProfile( CellCalculation cellCalculation )
    {
        if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            cellCalculation.removeLocationCenterIconListMarkers();
            cellCalculation.removeLocationListMarkers();
            cellCalculation.removeLocationPolyline();

        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            removeLocationCenterIconListMarkersOsm(cellCalculation);
            removeLocationListMarkersOsm(cellCalculation);
            removeClusterMarkerItemOsm();
            openStreetMapController.removeFromOsmMap(cellCalculation.getLocationPolylineOsm());
            // TODO:: remove osm cluster here
        }
    }

    public void removeRadioBasedOnProfile( CellCalculation cellCalculation, SettingRadio radio )
    {
        if( radio.getMeasuredRadio().isShowing() )
        {
            removeCellListMarker(cellCalculation);
        }

        if( radio.getCalculatedAreaIcon().isShowing() )
        {
            gmapController.removeClusterItem1(cellCalculation.getClusteredMarker());
            gmapController.removeClusterItem2(cellCalculation.getClusteredMarker());
            openStreetMapController.removeClusterItem(cellCalculation.getClusteredMarkerOsm());

        }

        if( radio.getCalculatedAreaEllipse().isShowing() )
        {
            removeCenterPolygon(cellCalculation);
        }

        if( radio.getCalculatedAreaPolygon().isShowing() )
        {
            removeConvexHull(cellCalculation);
        }
    }

    public void removeLocationListMarkersOsm( CellCalculation cellCalculation )
    {
        Iterator<org.osmdroid.views.overlay.Polygon> cellListMarkerIteratorOsm = cellCalculation.getLocationListOsm().iterator();
        org.osmdroid.views.overlay.Polygon currentCircle;

        while ( cellListMarkerIteratorOsm.hasNext() )
        {
            currentCircle = cellListMarkerIteratorOsm.next();
            openStreetMapController.removeFromOsmMap(currentCircle);
        }
    }

    public void removeClusterMarkerItemOsm()
    {
        for( Marker currentMarker : openStreetMapController.getMarkerOsm() )
        {
            openStreetMapController.removeFromOsmMap(currentMarker);
        }
    }

    public void removeLocationCenterIconListMarkersOsm( CellCalculation cellCalculation )
    {
        Iterator<Marker> locationCenterIconListMarkerIterator = cellCalculation.getLocationCenterIconListMarkerOsm().iterator();
        Marker currentMarker;
        while ( locationCenterIconListMarkerIterator.hasNext() )
        {
            currentMarker = locationCenterIconListMarkerIterator.next();
            openStreetMapController.removeFromOsmMap(currentMarker);
        }
    }

    // Removing radio data
    public void removeCellListMarker( CellCalculation cellCalculation )
    {
        if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            cellCalculation.removeCellListMarkers();
        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            removeCellListMarkersOsm(cellCalculation);
        }
    }

    public void removeCenterPolygon( CellCalculation cellCalculation )
    {
        if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            cellCalculation.removeCenterPolygon();
        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            openStreetMapController.removeFromOsmMap(cellCalculation.getCenterPolygonOsm());
        }
    }

    public void removeConvexHull( CellCalculation cellCalculation )
    {
        if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
        {
            cellCalculation.removeConvexHull();
        }
        else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
        {
            openStreetMapController.removeFromOsmMap(cellCalculation.getConvexHullOsm());
        }
    }

    public void removeCellListMarkersOsm( CellCalculation cellCalculation )
    {
        Iterator<org.osmdroid.views.overlay.Polygon> cellListMarkerIteratorOsm = cellCalculation.getCellListMarkerOsm().iterator();
        org.osmdroid.views.overlay.Polygon currentCircle;

        while ( cellListMarkerIteratorOsm.hasNext() )
        {
            currentCircle = cellListMarkerIteratorOsm.next();
            openStreetMapController.removeFromOsmMap(currentCircle);
        }
    }
    // End of Removing section

    /**
     * used to init redraw location when using autocomplete
     * can redraw location with condition pinLocationProvider == null
     */
    private void redrawAutoCompleteLocations()
    {
        if( null != mainActivity.getGooglePlacesController() && !isLongPress )
        {
            Place place = mainActivity.getGooglePlacesController().getPlace();
            LatLng apiLocationResult = null == place ? null : place.getLatLng();
            boolean isLocationApproveToMock = mainActivity.getGooglePlacesController().isLocationApproveToMock();

            if( !mainActivity.getMapToolsController().isViewingSession() )
            {
                double lat = null != apiLocationResult ? apiLocationResult.latitude : (null == pinLocationProvider ? 0 : (null == pinLocationProvider.getPinLocation() ? 0 : pinLocationProvider.getPinLocation().getLatitude()));
                double lng = null != apiLocationResult ? apiLocationResult.longitude : (null == pinLocationProvider ? 0 : (null == pinLocationProvider.getPinLocation() ? 0 : pinLocationProvider.getPinLocation().getLongitude()));

                int marker = null != apiLocationResult && !isLocationApproveToMock ? R.drawable.arrow_blue : R.drawable.arrow;

                if( (lat > 0 || lat < 0) && (lng > 0 || lng < 0) )
                {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(new LatLng(lat, lng));

                    zoomToArea(lat, lng);
                    addLocationArrowMarker(lat, lng, marker);
                }
            }
            else
            {
                clearMap();
            }
        }
    }

    // Called when User switch maps. All measured locations are stored in locationList.
    // Clear all the maps first, and redraw measured locations to current selected map
    public void redrawLocations()
    {
        int locSize = 0;

        for( Location loc : locationList )
        {
            locSize++;
            if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
            {
                gmapController.addColorToAreaWithZoom(loc.getLatitude(), loc.getLongitude());
            }
            else if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
            {
                openStreetMapController.addCircleWithZoom(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy());
                if( locSize == locationList.size() )
                {
                    openStreetMapController.setOSMZoomLevel(loc.getLatitude(), loc.getLongitude(), getLastZoomLevel());
                }
            }
        }

        if( pinLocationProvider != null && pinLocationProvider.getPinLocation() != null && isLongPress )
        {
            if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
            {
                LatLng lastTakeGmap = new LatLng(pinLocationProvider.getPinLocation().getLatitude(), pinLocationProvider.getPinLocation().getLongitude());
                gmapController.addLocationPinPointMarker(lastTakeGmap);
                zoomToArea(pinLocationProvider.getPinLocation().getLatitude(), pinLocationProvider.getPinLocation().getLongitude());
            }
            else
            {
                GeoPoint lastTakeOsm = new GeoPoint(pinLocationProvider.getPinLocation().getLatitude(), pinLocationProvider.getPinLocation().getLongitude());
                openStreetMapController.addLocationPinPointMarker(lastTakeOsm);
                zoomToArea(pinLocationProvider.getPinLocation().getLatitude(), pinLocationProvider.getPinLocation().getLongitude());
            }
        }

        redrawAutoCompleteLocations();

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

    public void getMyLocation( Location location )
    {
        if( mapSingleton.getSelectedMap() == Config.GOOGLEMAP )
        {
            gmapController.getMyCurrentLocation(location);
        }
        else
        {
            openStreetMapController.getMyCurrentLocation(location);
        }
    }

    private class CameraIdleListener implements GoogleMap.OnCameraIdleListener, MapListener
    {
        private ClusterManager clusterManager1;
        private ClusterManager clusterManager2;

        private LatLngBounds.Builder builder;

        public CameraIdleListener( ClusterManager clusterManager1, ClusterManager clusterManager2 )
        {
            this.clusterManager1 = clusterManager1;
            this.clusterManager2 = clusterManager2;
            builder = new LatLngBounds.Builder();
        }

        @Override
        public void onCameraIdle()
        {
            clusterManager1.onCameraIdle();
            clusterManager2.onCameraIdle();
            zoomLevelGoogle = getLastZoomLevel();
            zoomLevelOSM = getLastZoomLevel() + 3;
            lastZoomLevel = (int) googleMap.getCameraPosition().zoom - 2;

            if( MapSingleton.getInstance().getSelectedMap() == Config.OPENSTREETMAP )
                lastZoomLevel = zoomLevelOSM;
            else if( MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP )
                lastZoomLevel = zoomLevelGoogle;
            setLastZoomLevel(getLastZoomLevel());

            if( allowShowResume && MainActivity.isScanning )
            {
                mapSingleton.setTouchStatus(true);
                mainActivity.getMapToolsController().showResumeButton();
            }
            else
            {
                allowShowResume = true;
            }
        }

        @Override
        public boolean onScroll( ScrollEvent event )
        {
            if( allowShowResume && MainActivity.isScanning )
            {
                mapSingleton.setTouchStatus(true);
                mainActivity.getMapToolsController().showResumeButton();
            }
            else
            {
                allowShowResume = true;
            }
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

    public void setAllowShowResume( boolean initialized )
    {
        this.allowShowResume = initialized;
    }

    //set pin location provider (Turn On The Function)
    public void setPinLocationProvider( String gpsProvider )
    {
        if( pinLocationProvider == null )
        {
            pinLocationProvider = new PinLocationProvider(gpsProvider, mainActivity);
        }
    }

    //shutdown pin location
    public boolean shutDownPinLocation()
    {
        if( pinLocationProvider != null )
        {
            pinLocationProvider.shutDown();
            pinLocationProvider = null;
            mainActivity.getMapToolsController().setMyLocationActivated(false);
            mainActivity.getMapToolsController().setResumeActivated(false);

            return true;
        }
        return false;
    }

    // get address name show in autocomplete search box when long press
    public String getAddressName( LatLng latLng, GeoPoint geoPoint )
    {
        List<Address> addresses;
        Geocoder geocoder = new Geocoder(mainActivity, Locale.getDefault());
        boolean chooseGoogleMap = MapSingleton.getInstance().getSelectedMap() == Config.GOOGLEMAP;

        try
        {
            if( chooseGoogleMap )
            {
                addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, Global.RETURN_RESULT);
            }
            else
            {
                addresses = geocoder.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), Global.RETURN_RESULT);
            }

            insideException = false;
            return addresses.get(Global.DEFAULT_INTEGER_VALUE).getAddressLine(Global.ADDRESS_LINE_PRESENT);

        }
        catch ( IOException e )
        {
            boolean serviceNotAvailable = e.getMessage().equalsIgnoreCase(mainActivity.getResources().getString(R.string.service_not_avail));
            boolean grpcFailed = e.getMessage().equalsIgnoreCase(mainActivity.getResources().getString(R.string.grpc_failed));

            if( serviceNotAvailable || grpcFailed )
            {
                insideException = true;
            }
            e.printStackTrace();
        }

        return null;
    }

    //google map long click listener
    public GoogleMap.OnMapLongClickListener registerEventGmap = new GoogleMap.OnMapLongClickListener()
    {
        @Override
        public void onMapLongClick( LatLng latLng )
        {
            updateInfoWindow = true;
            latitudeLongClick = latLng.latitude;
            longitudeLongClick = latLng.longitude;

            if( pinLocationProvider != null )
            {
                clearMap();
                isLongPress = true;
                preparePinLocation(latitudeLongClick, longitudeLongClick);
                gmapController.addLocationPinPointMarker(latLng);
                Toast.makeText(mainActivity, mainActivity.getResources().getString(R.string.loc_pinned), Toast.LENGTH_SHORT).show();

                if( Utility.isNetworkConnected(mainActivity) )
                {
                    mainActivity.getGooglePlacesController().getGooglePlacesField().setText(getAddressName(latLng, null));
                }
                else
                {
                    mainActivity.getGooglePlacesController().getGooglePlacesField().setText(Global.EMPTY_STRING);
                }
                mainActivity.getGooglePlacesController().getGooglePlacesField().dismissDropDown();
            }
            else
            {
                Toast.makeText(mainActivity, R.string.long_click_status_off, Toast.LENGTH_SHORT).show();
            }

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
            updateInfoWindow = true;
            latitudeLongClick = geoPoint.getLatitude();
            longitudeLongClick = geoPoint.getLongitude();

            if( pinLocationProvider != null )
            {
                clearMap();
                isLongPress = true;
                openStreetMapController.registerEventOsm(getRegisterEventOsm());
                preparePinLocation(latitudeLongClick, longitudeLongClick);
                openStreetMapController.addLocationPinPointMarker(geoPoint);
                Toast.makeText(mainActivity, mainActivity.getResources().getString(R.string.loc_pinned), Toast.LENGTH_SHORT).show();

                if( Utility.isNetworkConnected(mainActivity) )
                {
                    mainActivity.getGooglePlacesController().getGooglePlacesField().setText(getAddressName(null, geoPoint));
                }
                else
                {
                    mainActivity.getGooglePlacesController().getGooglePlacesField().setText(Global.EMPTY_STRING);
                }

                mainActivity.getGooglePlacesController().getGooglePlacesField().dismissDropDown();

            }
            else
            {
                Toast.makeText(mainActivity, R.string.long_click_status_off, Toast.LENGTH_SHORT).show();
            }

            return true;
        }
    };

    //prepare pin location
    public void preparePinLocation( double latitude, double longitude )
    {
        pinLocationProvider.pushLocation(latitude, longitude);
        LocationManager locManager = (LocationManager) mainActivity.getSystemService(Context.LOCATION_SERVICE);

        String[] multiplePermission = new String[Global.LOCATION_PERMISSION_GROUP.length + Global.PHONE_PERMISSION_GROUP.length];

        System.arraycopy(Global.LOCATION_PERMISSION_GROUP, 0, multiplePermission, 0, Global.LOCATION_PERMISSION_GROUP.length);
        System.arraycopy(Global.PHONE_PERMISSION_GROUP, 0, multiplePermission, Global.LOCATION_PERMISSION_GROUP.length, Global.PHONE_PERMISSION_GROUP.length);

        ArrayList<String> deniedPermissionList = new ArrayList<>();

        for( String permission : multiplePermission )
        {
            if( mainActivity.getApplicationContext().checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED )
            {
                deniedPermissionList.add(permission);
            }
        }

        if( deniedPermissionList.isEmpty() )
        {
            //check gps in background
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Global.MIN_TIME_RESPONSE, Global.MIN_DIST_RESPONSE, mainActivity.getCellInfoController().getLocationListener());
        }
        else
        {
            boolean isPhoneAllow = mainActivity.getPermissionController().getDeniedPermissionList(Global.PHONE_PERMISSION_GROUP).isEmpty();
            boolean isLocationAllow = mainActivity.getPermissionController().getDeniedPermissionList(Global.LOCATION_PERMISSION_GROUP).isEmpty();

            mainActivity.getPermissionController().getPermissionList().put(Global.PHONE_PERMISSION, isPhoneAllow);
            CelltraxDB.getInstance().getPermissionTable().updatePermission(Global.PHONE_PERMISSION, isPhoneAllow);

            mainActivity.getPermissionController().getPermissionList().put(Global.LOCATION_PERMISSION, isLocationAllow);
            CelltraxDB.getInstance().getPermissionTable().updatePermission(Global.LOCATION_PERMISSION, isLocationAllow);
        }
    }

    /**
     * Is Mock Location Enable or not
     * Support API 23
     *
     * @return isMockLocation {@true} mock location has active {@false} otherwise
     */
    public boolean isMockLocation()
    {
        boolean isMockLocation = false;

        try
        {
            AppOpsManager opsManager = (AppOpsManager) mainActivity.getSystemService(Context.APP_OPS_SERVICE);
            isMockLocation = (opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID) == AppOpsManager.MODE_ALLOWED);
        }
        catch ( Exception e )
        {
            return isMockLocation;
        }

        return isMockLocation;
    }

    //get pin location provider
    public PinLocationProvider getPinLocationProvider()
    {
        return this.pinLocationProvider;
    }

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
