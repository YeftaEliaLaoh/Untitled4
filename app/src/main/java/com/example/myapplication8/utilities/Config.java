package com.example.myapplication8.utilities;

import android.Manifest;
import android.graphics.Color;

public class Config
{
    public static boolean IS_DEMO_VERSION = false;
    public static String DEFAULT_STRING_VALUE = "";
    public static String DEMO_USERNAME = "superadmin";
    public static String DEMO_PASSWORD = "1234Qwe";
    public static int GOOGLE_MAP = 0;
    public static int OPEN_STREET_MAP = 1;

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

    public static final String KEY_CURRENT_EXT = "txt";
    public static final int DEFAULT_PART_FILE = 0;

    public static final String SESSIONS_KEY = "sessions";
    public static final String LIST_KEY = "list";
    public static final String CELL_ID_KEY = "cell_id";

    public static final String CHECKSUM_KEY = "checksum";
    public static final String DATE_IMPORTED_KEY = "date_imported";
    public static final String FILE_PART_KEY = "file_part";
    public static final String HASHING_METHOD = "MD5";

    public static final String SESSION_ID_KEY = "id";
    public static final String EXPORT_ID_KEY = "export_id";
    public static final String START_TIME_KEY = "start_time";
    public static final String END_TIME_KEY = "end_time";
    public static final String COUNT_KEY = "count";
    public static final String MEASUREMENT_KEY = "measurement";
    public static final String OBJECT_ID_KEY = "id";
    public static final String PSC_KEY = "psc";
    public static final String CELL_REFERENCE_KEY = "cell_reference";
    public static final String CELL_REFERENCE_RNC_KEY = "cell_reference_rnc";
    public static final String RADIO_TYPE_KEY = "radio_type";
    public static final String SIGNAL_STRENGTH_KEY = "signal_strength";
    public static final String ACCURACY_KEY = "accuracy";
    public static final String BEARING_KEY = "bearing";
    public static final String ELEVATION_KEY = "elevation";
    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";
    public static final String DATE_TIME_KEY = "date_time";
    public static final int RESULTS_LOADER_LIMIT = 5;
    public static final String TAG_FILE_ID = "tag";

    public static final int DEFAULT_COLOR_RADIO_CELL_OUTLINE = Color.BLACK;

    public static final int DEFAULT_COLOR_RADIO_CELL_MEASURED = Color.argb(120, 53, 214, 0); //green
    public static final int DEFAULT_COLOR_RADIO_CELL_CALCULATED_CENTER = Color.argb(120, 67, 144, 233); //blue
    public static final int DEFAULT_COLOR_RADIO_CELL_AREA_POLYGON = Color.argb(120, 233, 67, 76); //red


}
