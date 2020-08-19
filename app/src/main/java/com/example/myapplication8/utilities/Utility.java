package com.example.myapplication8.utilities;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication8.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolygonOptions;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Polygon;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class Utility
{
    private static final double EARTH_RADIUS = 6367;

    public static String milisToDateFormat( long milliSeconds, String format )
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static long strDateToMilis( String date )
    {
        if( date.isEmpty() )
        {
            return 0;
        }

        Calendar calendar = Calendar.getInstance(Locale.getDefault());

        String[] masterSplit = date.split(" ");

        String[] dateSpliter = masterSplit[0].split("-");
        String[] hourSpliter = masterSplit[1].split(":");

        int year = Integer.parseInt(dateSpliter[0]);
        int month = Integer.parseInt(dateSpliter[1]) - 1;
        int day = Integer.parseInt(dateSpliter[2]);

        int hour = Integer.parseInt(hourSpliter[0]);
        int minutes = Integer.parseInt(hourSpliter[1]);
        int second = Integer.parseInt(hourSpliter[2]);

        calendar.set(Calendar.DATE, day);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, second);

        return calendar.getTimeInMillis();
    }

    public static String generateChecksum( String dateOfImport, int filePart ) throws NoSuchAlgorithmException
    {
        String plainText = filePart + "|" + dateOfImport;

        MessageDigest md = MessageDigest.getInstance(Config.HASHING_METHOD);
        md.update(plainText.getBytes());

        byte[] byteData = md.digest();

        StringBuffer sb = new StringBuffer();
        for( byte datum : byteData )
        {
            sb.append(Integer.toString((datum & 0xff) + 0x100, 16).substring(1));
        }

        StringBuffer hexString = new StringBuffer();
        for( byte byteDatum : byteData )
        {
            String hex = Integer.toHexString(0xff & byteDatum);
            if( hex.length() == 1 )
                hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();

    }

    public static byte[] convertByteArrayP( int p_int )
    {
        byte[] lByteArray = new byte[4];
        int maskC = 0xFF;
        for( short i = 0; i <= 3; i++ )
        {
            lByteArray[i] = (byte) ((p_int >> (8 * i)) & maskC);
        }
        return lByteArray;
    }

    public static void updateTextview( TextView textview, String textContent )
    {
        textview.setTypeface(Typeface.DEFAULT);
        textview.setTextColor(Color.BLACK);
        textview.setText(textContent);
        textview.setTextSize(10);
    }

    public static void updateTextviewNotAvailable( TextView textview, String textContent )
    {
        textview.setTextColor(Color.parseColor("#CDCDCD"));
        textview.setTypeface(textview.getTypeface(), Typeface.ITALIC);
        textview.setText(textContent);
        textview.setTextSize(10);
    }

    // For Map Calculation

    public static double deg2Rad( double degree )
    {
        return degree * Math.PI / 180;
    }

    public static double calcHaversineDistance( double lat1, double lon1, double lat2, double lon2 )
    {
        double latitude1 = deg2Rad(lat1);
        double longitude1 = deg2Rad(lon1);
        double latitude2 = deg2Rad(lat2);
        double longitude2 = deg2Rad(lon2);
        double dLat = latitude2 - latitude1;
        double dLon = longitude2 - longitude1;

        double coordLength = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(latitude1) * Math.cos(latitude2) * Math.pow(Math.sin(dLon / 2), 2);
        double centralAngle = 2 * Math.atan2(Math.sqrt(coordLength), Math.sqrt(1 - coordLength));

        return EARTH_RADIUS * centralAngle;
    }

    public static PolygonOptions getEllipseForGoogleMap( LatLng point, double width, double height, double rotation, int vertexCount, int color, int outline )
    {
        double lat = point.latitude;
        double lng = point.longitude;
        double rot = -rotation * Math.PI / 180;

        double latConv = calcHaversineDistance(lat, lng, (lat + 0.1), lng) * 10;
        double lngConv = calcHaversineDistance(lat, lng, lat, (lng + 0.1)) * 10;
        double step = (360 / vertexCount);
        double flop = -1;
        double i1 = 180 / vertexCount;
        PolygonOptions polygonOptions = new PolygonOptions();
        // polygonOptions.fillColor(0x5500ff00);
        polygonOptions.fillColor(color);
        polygonOptions.strokeWidth(3f);
        polygonOptions.strokeColor(outline);

        for( double i = i1; i <= 360.001 + i1; i += step )
        {
            flop = -1 - flop;
            double y = width * Math.cos(i * Math.PI / 180);
            double x = height * Math.sin(i * Math.PI / 180);
            double lngRes = (x * Math.cos(rot) - y * Math.sin(rot)) / lngConv;
            double latRes = (y * Math.cos(rot) + x * Math.sin(rot)) / latConv;

            double pathx = lat + latRes;
            double pathy = lng + lngRes;
            pathx = Math.round(pathx * 10000) / 10000;
            pathy = Math.round(pathy * 10000) / 10000;
            polygonOptions.add(new LatLng(lat + latRes, lng + lngRes));
        }
        return polygonOptions;

    }

    public static Polygon getEllipseForOSMMap( LatLng point, double width, double height, double rotation, int vertexCount, Context context, int color, int outline )
    {
        double lat = point.latitude;
        double lng = point.longitude;
        double rot = -rotation * Math.PI / 180;

        double latConv = calcHaversineDistance(lat, lng, (lat + 0.1), lng) * 10;
        double lngConv = calcHaversineDistance(lat, lng, lat, (lng + 0.1)) * 10;
        double step = (360 / vertexCount);
        double flop = -1;
        double i1 = 180 / vertexCount;
        final ArrayList<GeoPoint> list = new ArrayList<>();

        Polygon polygon = new Polygon();
        polygon.setFillColor(color);
        polygon.setStrokeWidth(2f);
        polygon.setStrokeColor(outline);

        for( double i = i1; i <= 360.001 + i1; i += step )
        {
            flop = -1 - flop;
            double y = width * Math.cos(i * Math.PI / 180);
            double x = height * Math.sin(i * Math.PI / 180);
            double lngRes = (x * Math.cos(rot) - y * Math.sin(rot)) / lngConv;
            double latRes = (y * Math.cos(rot) + x * Math.sin(rot)) / latConv;

            double pathx = lat + latRes;
            double pathy = lng + lngRes;
            pathx = Math.round(pathx * 10000) / 10000;
            pathy = Math.round(pathy * 10000) / 10000;
            list.add(new GeoPoint(lat + latRes, lng + lngRes));
        }
        polygon.setPoints(list);

        return polygon;
    }

    public static int getRNC( byte[] p_bytes )
    {
        int maskC = 0xFF;
        int lResult = 0;
        lResult = p_bytes[2] & maskC;
        lResult = lResult + ((p_bytes[3] & maskC) << 8);
        return lResult;
    }

    public static int getCid( byte[] p_bytes )
    {
        int maskC = 0xFF;
        int lResult = 0;
        lResult = p_bytes[0] & maskC;
        lResult = lResult + ((p_bytes[1] & maskC) << 8);
        return lResult;
    }

    public static String generateCellReference( String networkOperator, int lac, int cid, boolean isRncIncluded )
    {
        int int16Bit = 65536;
        String mcc = "-1";
        String mnc = "-1";
        int rnc = -1;
        String cellref = "";

        try
        {
            mcc = networkOperator.substring(0, 3);
            mnc = networkOperator.substring(3);
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        if( isRncIncluded && cid != -1 )
        {
            rnc = getRNC(Utility.convertByteArrayP(cid));
        }

        if( rnc != -1 && rnc != 0 )
        {
            cid = cid % int16Bit;
            cellref = mcc + "." + mnc + "." + lac + "." + rnc + "." + cid;
        }
        else
        {
            cellref = mcc + "." + mnc + "." + lac + "." + cid;
        }

        return cellref;
    }

    public static int asu2dBm( int signalStrengthAsu, String radioType )
    {
        int dBm = 0;
        if( radioType.equals("gsm") || radioType.equals("wcdma") )
        {

            int asu = (signalStrengthAsu == 99 ? Integer.MAX_VALUE : signalStrengthAsu);
            if( asu != Integer.MAX_VALUE )
            {
                dBm = -113 + (2 * asu);
            }
            else
            {
                dBm = Integer.MAX_VALUE;
            }


        }
        else if( radioType.equals("lte") )
        {
            //formula asu to dbm
            dBm = signalStrengthAsu - 140;
            //                int lteAsuLevel = 99;
            //                int lteDbm = getDbm();
            //                if (lteDbm <= -140) lteAsuLevel = 0;
            //                else if (lteDbm >= -43) lteAsuLevel = 97;
            //                else lteAsuLevel = lteDbm + 140;
            //                if (DBG) log("Lte Asu level: "+lteAsuLevel);
            //                return lteAsuLevel;

            //formula asu to dbm: asu-140

        }
        return dBm;
    }

    public static boolean canScanQRCode( Activity activity )
    {
        // check if device has a camera
        PackageManager pm = activity.getPackageManager();

        Log.d("CameraTest", "Check if device could support QRCode reading");
        if( pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) )
        {
            // detect if the device in the exclude list.
            Log.d("CameraTest", Build.MANUFACTURER);
            Log.d("CameraTest", Build.MODEL);
            if( Build.MANUFACTURER.equalsIgnoreCase("Foxconn International Holdings Limited") )
            {
                if( Build.MODEL.equalsIgnoreCase("FieldBook_E1") )
                {
                    Log.d("CameraTest", "Disable QRCode scanning based on device type");
                    return false;
                }
                else if( Build.MODEL.equalsIgnoreCase("FieldBook E1") )
                {
                    Log.d("CameraTest", "Disable QRCode scanning based on device type");
                    return false;
                }
            }
            else if( Build.MANUFACTURER.toLowerCase(Locale.getDefault()).startsWith("foxconn") )
            {
                if( Build.MODEL.toLowerCase(Locale.getDefault()).startsWith("fieldbook_e1") )
                {
                    Log.d("CameraTest", "Disable QRCode scanning based on device type");
                    return false;
                }
                else if( Build.MODEL.toLowerCase(Locale.getDefault()).startsWith("fieldbook e1") )
                {
                    Log.d("CameraTest", "Disable QRCode scanning based on device type");
                    return false;
                }
            }
            return true;
        }
        else
        {
            Log.d("CameraTest", "Disable QRCode scanning based on camera availability");
            return false;
        }
    }

    public static int dpToPixel( int dp, Context context )
    {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static String getNetworkClass( int networkType )
    {
        switch ( networkType )
        {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "gsm";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "wcdma";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "lte";
            default:
                return "Unknown";
        }
    }

    public static long generateExportId( long startTime, long endTime, long sessionId )
    {
        return (endTime - startTime) + sessionId;
    }

    /**
     * Get current date; format {yyyy-MM-dd};
     *
     * @return {@link String} of date
     */
    public static String getCurrentDate()
    {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        return dateFormat.format(Calendar.getInstance(Locale.getDefault()).getTime());

    }

    public static long parseTimeServerToLocaleMs( String time )
    {
        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'KK:mm:ss'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(time).getTime();
        }
        catch ( ParseException e )
        {
            e.printStackTrace();
        }
        return 0;
    }

    public static void showToast( Context context, String text )
    {
        Toast toastResponse = Toast.makeText(context, text, Toast.LENGTH_LONG);
        TextView textResponse = toastResponse.getView().findViewById(android.R.id.message);

        if( null != textResponse )
        {
            textResponse.setGravity(Gravity.CENTER);
        }

        toastResponse.show();
    }

    public static boolean inTypeEditTextValidator( Context context, EditText editText, String regex, int errRequired, int errType )
    {


        boolean isRequired = (null != editText.getText()) ? editText.getText().toString().isEmpty() : true;

        if( isRequired )
        {
            editText.setError(context.getString(errRequired));
        }
        else
        {

            if( null == regex )
            {
                return true;
            }

            Pattern pattern = Pattern.compile(regex);
            boolean isMatchPattern = pattern.matcher(

                    (null != editText.getText() ? editText.getText().toString() : "")

            ).matches();

            if( isMatchPattern )
            {
                return true;
            }

            editText.setError(context.getString(errType));
        }

        return false;
    }

    public static boolean isNetworkConnected( Context context )
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
