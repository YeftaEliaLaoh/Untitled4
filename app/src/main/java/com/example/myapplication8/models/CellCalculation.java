package com.example.myapplication8.models;

import android.content.Context;
import android.graphics.Paint;
import android.location.Location;
import android.util.Log;

import com.example.myapplication8.utilities.Config;
import com.example.myapplication8.utilities.ConvexHull;
import com.example.myapplication8.utilities.Point;
import com.example.myapplication8.utilities.Utility;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CellCalculation
{
    private int currentScannedType;

    private Point[] convexHullPoints;

    private ArrayList<Cell> cellList;
    private LatLng center;
    private LatLng northEast;
    private LatLng southWest;
    private LatLng northWest;
    private LatLng southEast;
    private LatLngBounds radioBound;

    //  private Marker centerMarker;
    // private MarkerOptions centerMarkerOptions;

    // Components for Google Map
    private Polygon centerPolygon;
    private Polygon convexHull;

    private ArrayList<Circle> cellListMarker;

    private ArrayList<Circle> locationListMarker;
    private ArrayList<Marker> locationCenterIconListMarker;
    private Polyline loctionPolyline;

    private ClusteredCenterMarker clusteredMarker;
    private org.osmdroid.views.overlay.Marker clusteredMarkerOsm;

    // Components for OSM
    private org.osmdroid.views.overlay.Polygon centerPolygonOsm;
    private org.osmdroid.views.overlay.Polyline convexHullOsm;
    private ArrayList<org.osmdroid.views.overlay.Polygon> cellListMarkerOsm;
    private ArrayList<org.osmdroid.views.overlay.Polygon> locationListMarkerOsm;
    private ArrayList<org.osmdroid.views.overlay.Marker> locationCenterIconListMarkerOsm;
    private org.osmdroid.views.overlay.Polyline locationPolylineOsm;

    private boolean pointIncluded = false;

    public CellCalculation( int scannedType )
    {
        cellList = new ArrayList<>();
        cellListMarker = new ArrayList<>();
        locationListMarker = new ArrayList<>();
        locationCenterIconListMarker = new ArrayList<>();

        cellListMarkerOsm = new ArrayList<>();
        locationListMarkerOsm = new ArrayList<>();
        locationCenterIconListMarkerOsm = new ArrayList<>();

        this.currentScannedType = scannedType;
    }

    public int getCurrentScannedType()
    {
        return this.currentScannedType;
    }

    public int getMeasurement()
    {
        if( currentScannedType == Config.SCANNED_TYPE_CELL )
        {
            return cellList.size();
        }

        return 0;
    }

    public Point[] getConvexHullPoints()
    {
        return this.convexHullPoints;
    }

    public void setConvexHullPoints( Point[] convexHullPoints )
    {
        this.convexHullPoints = convexHullPoints;
    }

    public void setCellList( ArrayList<Cell> cellList )
    {
        this.cellList = cellList;
    }

    public ArrayList<Cell> getCellList()
    {
        return this.cellList;
    }

    // get maximum signal strength based on wifi or cell
    public int getMaxSignalStrength()
    {
        int size;
        int[] items = null;
        if( currentScannedType == Config.SCANNED_TYPE_CELL )
        {
            size = cellList.size();
            items = new int[size];

            for( int i = 0; i < size; i++ )
            {
                items[i] = cellList.get(i).getSignalStrength();
            }
        }

        return Utility.getMax(items);
    }

    // get minimum signal strength based on wifi or cell
    public int getMinSignalStrength()
    {
        int size;
        int[] items = null;
        if( currentScannedType == Config.SCANNED_TYPE_CELL )
        {
            size = cellList.size();
            items = new int[size];

            for( int i = 0; i < size; i++ )
            {
                items[i] = cellList.get(i).getSignalStrength();
            }
        }

        return Utility.getMin(items);
    }

    // get cell reference of the first cell from cell list, if scanned type is cell, the expected cell reference format is mcc.mnc.lac.cid.
    // if scanned type is wifi return empty string
    public String getCellReference()
    {
        if( currentScannedType != Config.SCANNED_TYPE_CELL )
        {
            return "";
        }
        else if( cellList.size() > 0 )
        {
            return (cellList.get(0)).getCellRefWithRNC();
        }
        return "";
    }

    // return the cell radio type of the first cell from the list
    // if scanned type is wifi return empty string
    public String getCellRadioType()
    {
        if( currentScannedType != Config.SCANNED_TYPE_CELL )
        {
            return "";
        }
        else if( cellList.size() > 0 )
        {
            return (cellList.get(0)).getRadioType();
        }
        return "";
    }

    public int getCellPSC()
    {
        if( currentScannedType != Config.SCANNED_TYPE_CELL )
        {
            return 0;
        }
        else if( cellList.size() > 0 )
        {
            return (cellList.get(0)).getPsc();
        }
        return 0;
    }


    // get datetime of the current scanned type, return empty string if the current scanned type
    // list is empty
    public String getDateTime()
    {
        if( currentScannedType == Config.SCANNED_TYPE_CELL && cellList.size() > 0 )
        {
            return cellList.get(0).getDatetime();
        }
        return "";
    }

    public LatLng getCenter()
    {
        return center;
    }

    // calculate the center point of multiple geo points on earth
    public void calculateCenter()
    {
        ArrayList<LatLng> points = new ArrayList<>();

        if( currentScannedType == Config.SCANNED_TYPE_CELL )
        {
            for( Cell cell : cellList )
            {
                LatLng point = new LatLng(cell.getMeasuredLocation().getLatitude(), cell.getMeasuredLocation().getLongitude());
                points.add(point);
            }
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        int size = points.size();
        for( int i = 0; i < size; i++ )
        {
            builder.include(points.get(i));
            pointIncluded = true;
        }

        if( pointIncluded )
        {
            radioBound = builder.build();
            center = radioBound.getCenter();
            northEast = radioBound.northeast;
            southWest = radioBound.southwest;

            // Generate north west and south east point from Center point
            northWest = new LatLng(southWest.latitude, northEast.longitude);
            southEast = new LatLng(northEast.latitude, southWest.longitude);

            setCenterMarkerOptions(center);
        }

    }

    public LatLngBounds getCenterBound()
    {
        return radioBound;
    }

    private void setCenterMarkerOptions( LatLng center )
    {
        if( currentScannedType == Config.SCANNED_TYPE_CELL )
        {
            clusteredMarker = new ClusteredCenterMarker(center, getCellReference(), Config.SCANNED_TYPE_CELL);
        }
    }

    public ClusteredCenterMarker getClusteredMarker()
    {
        return this.clusteredMarker;
    }

    public void setCenterMarkerOptionsOsm( org.osmdroid.views.overlay.Marker osmMarker )
    {
        this.clusteredMarkerOsm = osmMarker;

    }

    public org.osmdroid.views.overlay.Marker getClusteredMarkerOsm()
    {
        return this.clusteredMarkerOsm;
    }

    // Start Google Components

    public void setLocationPolyline( Polyline polyline )
    {
        this.loctionPolyline = polyline;
    }

    public void setLocationPolylineOsm( org.osmdroid.views.overlay.Polyline pathOverlay )
    {
        this.locationPolylineOsm = pathOverlay;
    }

    public org.osmdroid.views.overlay.Polyline getLocationPolylineOsm()
    {
        return locationPolylineOsm;
    }

    public void removeLocationPolyline()
    {
        if( loctionPolyline != null )
        {
            loctionPolyline.remove();
        }
    }

    public void addLocationCenterIconListMarker( Marker marker )
    {
        locationCenterIconListMarker.add(marker);
    }

    public void removeLocationCenterIconListMarkers()
    {
        Iterator<Marker> locationCenterIconListMarkerIterator = locationCenterIconListMarker.iterator();
        Marker currentMarker;
        while ( locationCenterIconListMarkerIterator.hasNext() )
        {
            currentMarker = locationCenterIconListMarkerIterator.next();
            currentMarker.remove();
        }
    }

    public List<org.osmdroid.views.overlay.Marker> getLocationCenterIconListMarkerOsm()
    {
        return locationCenterIconListMarkerOsm;
    }

    public void addLocationCenterIconListMarkerOsm( org.osmdroid.views.overlay.Marker marker )
    {
        locationCenterIconListMarkerOsm.add(marker);
    }

    public void addLocationListMarker( Circle circle )
    {
        locationListMarker.add(circle);
    }

    public void removeLocationListMarkers()
    {
        Iterator<Circle> locationListMarkerIterator = locationListMarker.iterator();
        Circle currentCircle;
        while ( locationListMarkerIterator.hasNext() )
        {
            currentCircle = locationListMarkerIterator.next();
            currentCircle.remove();
        }
    }

    public void addLocationListMarkerOsm( org.osmdroid.views.overlay.Polygon circle )
    {
        locationListMarkerOsm.add(circle);
    }

    public List<org.osmdroid.views.overlay.Polygon> getLocationListOsm()
    {
        return locationListMarkerOsm;
    }

    public void addCircle( Circle circle )
    {
        cellListMarker.add(circle);
    }

    public List<org.osmdroid.views.overlay.Polygon> getCellListMarkerOsm()
    {
        return this.cellListMarkerOsm;
    }

    public void addCircleOsm( org.osmdroid.views.overlay.Polygon circle )
    {
        cellListMarkerOsm.add(circle);
    }

    public void removeCellListMarkers()
    {
        Iterator<Circle> cellListMarkerIterator = cellListMarker.iterator();
        Circle currentCircle;

        while ( cellListMarkerIterator.hasNext() )
        {
            currentCircle = cellListMarkerIterator.next();
            currentCircle.remove();
        }
    }

    public PolygonOptions getCenterPolygonOptions( int color, int outline )
    {
        if( null == northEast || null == southWest )
        {
            return null;
        }
        double rotation = 0;


        Location locNorthEast = new Location("north east");
        locNorthEast.setLatitude(northEast.latitude);
        locNorthEast.setLongitude(northEast.longitude);

        Location locNorthWest = new Location("north west");
        locNorthWest.setLatitude(northWest.latitude);
        locNorthWest.setLongitude(northWest.longitude);

        Location locSouthEast = new Location("south east");
        locSouthEast.setLatitude(southEast.latitude);
        locSouthEast.setLongitude(southEast.longitude);

        /*
         * Referenced to http://stackoverflow.com/questions/433371/ellipse-bounding-a-rectangle
         * */
        double rectWidth = locNorthEast.distanceTo(locNorthWest) / Math.sqrt(2);
        double rectHeight = locNorthEast.distanceTo(locSouthEast) / Math.sqrt(2);

        // Get Polygon options
        return Utility.getEllipseForGoogleMap(center, rectWidth / 1000, rectHeight / 1000, rotation, 200, color, outline);
    }

    public org.osmdroid.views.overlay.Polygon getCenterPolygonOptionsOsm( Context context, int color, int outline )
    {
        if( null == northEast || null == southWest )
        {
            return null;
        }
        double rotation = 0;

        // Generate north west and south east point from Center point
        LatLng northWest = new LatLng(southWest.latitude, northEast.longitude);
        LatLng southEast = new LatLng(northEast.latitude, southWest.longitude);

        Location locNorthEast = new Location("north east");
        locNorthEast.setLatitude(northEast.latitude);
        locNorthEast.setLongitude(northEast.longitude);

        Location locNorthWest = new Location("north west");
        locNorthWest.setLatitude(northWest.latitude);
        locNorthWest.setLongitude(northWest.longitude);

        Location locSouthEast = new Location("south east");
        locSouthEast.setLatitude(southEast.latitude);
        locSouthEast.setLongitude(southEast.longitude);

        /*
         * Referenced to http://stackoverflow.com/questions/433371/ellipse-bounding-a-rectangle
         * */
        double rectWidth = locNorthEast.distanceTo(locNorthWest) / Math.sqrt(2);
        double rectHeight = locNorthEast.distanceTo(locSouthEast) / Math.sqrt(2);

        // Get Polygon options
        return Utility.getEllipseForOSMMap(center, rectWidth / 1000, rectHeight / 1000, rotation, 200, context, color, outline);
    }

    public void setCenterPolygon( Polygon polygon )
    {
        this.centerPolygon = polygon;
    }

    public org.osmdroid.views.overlay.Polygon getCenterPolygonOsm()
    {
        return this.centerPolygonOsm;
    }

    public void setCenterPolygonOsm( org.osmdroid.views.overlay.Polygon polygon )
    {
        this.centerPolygonOsm = polygon;
    }

    public void removeCenterPolygon()
    {
        this.centerPolygon.remove();
    }

    public PolygonOptions setHullPolylineOptions( int color, int outlinecolor )
    {
        PolygonOptions convexHullOptions = new PolygonOptions();
        convexHullOptions.fillColor(color);
        convexHullOptions.strokeWidth(3f);
        convexHullOptions.strokeColor(outlinecolor);


        Point[] hull = ConvexHull.convexHull(convexHullPoints).clone();

        for( Point point : hull )
        {
            if( point != null )
            {
                Log.d("info cell", "X: " + point.x + " Y : " + point.y);
                convexHullOptions.add(new LatLng(point.x, point.y));
            }

        }
        return convexHullOptions;
    }

    public org.osmdroid.views.overlay.Polyline getHullPolylineOptionsOsm( Context context, int color )
    {
        org.osmdroid.views.overlay.Polyline polyline = new org.osmdroid.views.overlay.Polyline();
        polyline.getOutlinePaint().setStyle(Paint.Style.FILL);
        polyline.getOutlinePaint().setColor(color);

        Point[] hull = ConvexHull.convexHull(convexHullPoints).clone();
        for( Point point : hull )
        {
            if( point != null )
            {
                Log.d("info cell", "X: " + point.x + " Y : " + point.y);
                polyline.addPoint(new GeoPoint(point.x, point.y));
            }
        }

        return polyline;

    }

    public void setPolyline( Polygon polygon )
    {
        this.convexHull = polygon;
    }

    public org.osmdroid.views.overlay.Polyline getConvexHullOsm()
    {
        return this.convexHullOsm;
    }

    public void setPolylineOsm( org.osmdroid.views.overlay.Polyline convexHullOsm )
    {
        this.convexHullOsm = convexHullOsm;
    }

    public void removeConvexHull()
    {
        convexHull.remove();
    }

    // End of Google Components

}
