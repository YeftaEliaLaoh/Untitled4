package com.example.myapplication8.utilities;

import android.Manifest;

public class Config
{
    public static boolean IS_DEMO_VERSION = false;
    public static String DEFAULT_STRING_VALUE = "";
    public static String DEMO_USERNAME = "superadmin";
    public static String DEMO_PASSWORD = "1234Qwe";
    public static int GOOGLEMAP = 0;
    public static int OPENSTREETMAP = 1;

    public static final int SCANNED_TYPE_CELL = 0;
    public static final int SCANNED_TYPE_WIFI = 1;

    public static final String SCAN_FRAGMENT = "scan_fragment";
    public static final int HASHMAP_DEFAULT_VALUE = 1;

    public static final int ERROR_UNKNOWN = -1;
    public static final int ERROR_IMPORT_SUCCESS = 0;
    public static final int ERROR_IMPORT_INVALID_FORMAT_FILE = 1;
    public static final int ERROR_IMPORT_INVALID_CHECKSUM = 2;
    public static final int ERROR_IMPORT_INSERTED_BEFORE = 3;
    public static final int ERROR_IMPORT_INVALID_FORMAT_JSON = 4;

    public static final String[] STORAGE_PERMISSION_GROUP = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    public static final String[] LOCATION_PERMISSION_GROUP = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    public static final String[] PHONE_PERMISSION_GROUP = new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.READ_PHONE_STATE,  Manifest.permission.WAKE_LOCK};
    public static final String[] CAMERA_PERMISSION_GROUP = new String[]{Manifest.permission.CAMERA};

    public static final int STORAGE_REQUEST_ID = 0;
    public static final int CAMERA_REQUEST_ID = 1;
    public static final int MULTIPLE_REQUEST_ID = 2;
}
