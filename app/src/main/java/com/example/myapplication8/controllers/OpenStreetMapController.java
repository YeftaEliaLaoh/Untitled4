/*
package com.example.myapplication8.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;

import com.firstwap.celltrax.BuildConfig;
import com.firstwap.celltrax.R;
import com.firstwap.celltrax.activities.MainActivity;
import com.firstwap.celltrax.models.ClusteredCenterMarker;
import com.firstwap.celltrax.models.MapSingleton;
import com.firstwap.celltrax.utils.Global;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.List;

public class OpenStreetMapController
{
    private MapView mapView;
    private Context context;
    //declare osm claster object
    private OsmClusterController osmClusterMarkers;
    private List<Marker> markerOsm;
    private Marker currentLocationMarker;
    private Marker locationMarker;
    private MainActivity mainActivity;

    public OpenStreetMapController( MapView mapView, MainActivity mainActivity )
    {
        this.mapView = mapView;
        this.context = mapView.getContext();
        this.osmClusterMarkers = new OsmClusterController(context);
        markerOsm = new ArrayList<>();
        //tap to zoom
        mapView.setMultiTouchControls(true);
        this.mainActivity = mainActivity;
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
    }

    public Context getContext()
    {
        return mapView.getContext();
    }

    public void showDetailsOsmMap()
    {
        mapView.setMinZoomLevel((double) 3);
        mapView.setMaxZoomLevel((double) 22);
        MapSingleton.getInstance().setSelectedMap(Global.OPENSTREETMAP);
        mapView.setVisibility(View.VISIBLE);
        IMapController mapController = mapView.getController();
        mapController.setZoom(mainActivity.getMapWrapper().zoomLevelOSM);
        mainActivity.getMapWrapper().redrawLocations();
        if( mainActivity.getMapToolsController() != null && mainActivity.getMapToolsController().isViewingSession() )
        {
            mainActivity.redrawMapComponents();
        }
    }

    public Polygon addCircleWithZoom( double latitude, double longitude, double accuracy )
    {
        Polygon polygon = new Polygon();
        GeoPoint latlng = new GeoPoint(latitude, longitude);
        ArrayList<GeoPoint> arrayList = new ArrayList<>();
        arrayList.add(latlng);
        BoundingBox boundingbox = BoundingBox.fromGeoPoints(arrayList);

        polygon.setPoints(Polygon.pointsAsCircle(latlng, accuracy));
        polygon.getFillPaint().setColor(0x7F45bed8);
        polygon.getOutlinePaint().setColor(Color.TRANSPARENT);
        polygon.getOutlinePaint().setStrokeWidth(1);

        final double newZoomLevel = mapView.getZoomLevelDouble();
        if( MainActivity.isScanning && MainActivity.firstTimeScanning )
        {
            mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            mapView.setMaxZoomLevel((double) 18);
            mapView.zoomToBoundingBox(boundingbox, true);
            MainActivity.firstTimeScanning = false;

        }
        else if( MainActivity.isScanning )
        {
            mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
            mapView.setMinZoomLevel(newZoomLevel);
            mapView.setMaxZoomLevel(newZoomLevel);
            mapView.zoomToBoundingBox(boundingbox, true);

        }
        mapView.getOverlays().add(polygon);
        mapView.invalidate();
        mapView.setMinZoomLevel((double) 3);
        mapView.setMaxZoomLevel((double) 24);
        mapView.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.SHOW_AND_FADEOUT);

        return polygon;
    }

    public MapView getMapView()
    {
        return mapView;
    }

    public void drawOnOsmMap( Overlay overlayObject )
    {
        mapView.getOverlays().add(overlayObject);
        mapView.invalidate();
    }

    private void drawOnTopOsmMap( Overlay overlayObject )
    {
        mapView.getOverlays().add(0, overlayObject);
        mapView.invalidate();
    }


    public void removeFromOsmMap( Overlay overlay )
    {
        mapView.getOverlays().remove(overlay);
        mapView.invalidate();
    }

    public Polygon addColorToArea( double latitude, double longitude, double accuracy, int color, int outlineColor )
    {
        Polygon polygon = new Polygon();
        GeoPoint geoPoint = new GeoPoint(latitude, longitude);
        polygon.setPoints(Polygon.pointsAsCircle(geoPoint, accuracy));
        polygon.getFillPaint().setColor(color);
        polygon.getOutlinePaint().setColor(outlineColor);
        polygon.getOutlinePaint().setStrokeWidth(3f);
        drawOnTopOsmMap(polygon);

        return polygon;
    }

    public void animateCameraToBound( LatLngBounds bound )
    {
        LatLng northEast = bound.northeast;
        LatLng southWest = bound.southwest;

        //BoundingBox(maxLat, maxLon, minLat, minLon);
        BoundingBox boundingbox = new BoundingBox(northEast.latitude, northEast.longitude, southWest.latitude, southWest.longitude);
        mapView.zoomToBoundingBox(boundingbox, true);
    }

    public Polyline addPolyline( Polyline polyline )
    {
        drawOnOsmMap(polyline);
        return polyline;
    }

    public void clear()
    {
        if( null == mapView )
        {
            return;
        }
        mapView.getOverlays().clear();
        mapView.invalidate();
    }

    public void setOSMZoomLevel( final double latitude, final double longitude, final double level )
    {
        if( null == mapView )
        {
            return;
        }
        IMapController mapController = mapView.getController();
        mapController.setZoom(level);
        mapController.animateTo(new GeoPoint(latitude, longitude));
    }

    public void showWorldOsm()
    {
        setOSMZoomLevel(0, 0, 0);
    }

    //adding marker wifi and cell
    public Marker addItemMarker( ClusteredCenterMarker cluster )
    {
        Marker marker = new Marker(mapView);
        GeoPoint point = new GeoPoint(cluster.getLatitude(), cluster.getLongitude());
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker.setTitle(cluster.getName());

        Drawable icon;
        if( cluster.getType() == Global.SCANNED_TYPE_CELL )
        {
            icon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.bts_scan, null);
        }
        else
        {
            icon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.wifi_scan, null);
        }

        marker.setSnippet(Integer.toString(cluster.getType()));
        marker.setIcon(icon);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);

        return marker;
    }

    //clearing cluster marker and list
    public void clearClusterItem()
    {
        markerOsm.clear();
        osmClusterMarkers.getItems().clear();
        mapView.getOverlays().remove(osmClusterMarkers);
        mapView.invalidate();
    }

    //setting cluster max zoom
    public void zoomClusterMarker()
    {
        if( markerOsm.size() != 0 )
        {
            mapView.setMaxZoomLevel((double) 19);
        }
    }

    //add location marker for OSM
    public Marker addLocationMarker( double latitude, double longitude, Drawable icon )
    {
        locationMarker = new Marker(mapView);
        locationMarker.setIcon(icon);
        locationMarker.setPosition(new GeoPoint(latitude, longitude));
        locationMarker.setAnchor(locationMarker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        locationMarker.setOnMarkerClickListener(mainActivity.getGooglePlacesController());
        drawOnOsmMap(locationMarker);

        return locationMarker;
    }

    public void addLocationPinPointMarker( GeoPoint geoPoint )
    {
        resetLocationMarker();
        addLocationMarker(geoPoint.getLatitude(), geoPoint.getLongitude(), ResourcesCompat.getDrawable(getContext().getResources(), R.drawable.arrow, null));
    }

    //adding object to list
    public void addListClusterMarker( Marker marker )
    {
        markerOsm.add(marker);
    }

    //getting list of cluster object
    public List<Marker> getMarkerOsm()
    {
        return markerOsm;
    }

    public void clearClusterOsm()
    {
        if( MapSingleton.getInstance().getSelectedMap() == Global.OPENSTREETMAP && osmClusterMarkers.getItems().size() != 0 )
        {
            osmClusterMarkers.getItems().clear();
        }
    }

    //show cluster osm to map
    public void showClusterOsm()
    {
        Drawable cid = ResourcesCompat.getDrawable(context.getResources(), R.drawable.ic_marker_cluster, null);
        Bitmap clusterIcon = ((BitmapDrawable) cid).getBitmap();
        osmClusterMarkers.setIcon(clusterIcon);

        for( final Marker m : markerOsm )
        {
            osmClusterMarkers.add(m);
        }

        drawOnOsmMap(osmClusterMarkers);
    }

    //remove cluster marker
    public void removeClusterItem( Marker marker )
    {
        osmClusterMarkers.getItems().clear();
        markerOsm.remove(marker);
        drawOnOsmMap(osmClusterMarkers);
    }

    public void getMyCurrentLocation( Location location )
    {
        if( null != currentLocationMarker )
        {
            mapView.getOverlays().remove(currentLocationMarker);
        }
        currentLocationMarker = addLocationMarker(location.getLatitude(), location.getLongitude(), ResourcesCompat.getDrawable(context.getResources(), R.drawable.arrow, null));
        setOSMZoomLevel(location.getLatitude(), location.getLongitude(), mapView.getZoomLevelDouble());
    }

    public void registerEventOsm( MapEventsReceiver receiver )
    {
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(receiver);
        mapView.getOverlays().add(mapEventsOverlay);
        mapView.invalidate();
    }

    public void resetLocationMarker()
    {
        if( locationMarker != null )
        {
            locationMarker.remove(mapView);
        }
    }
}
*/
