package com.example.myapplication8.controllers;


import com.example.myapplication8.activities.MainActivity;
import com.example.myapplication8.databases.AppDatabase;
import com.example.myapplication8.models.Cell;
import com.example.myapplication8.models.MeasuredLocation;
import com.example.myapplication8.models.Session;
import com.example.myapplication8.utilities.Config;
import com.example.myapplication8.utilities.Utility;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;


public class JSONFileController
{

    private MainActivity mainActivity;
    private AppDatabase appDatabase;

    public JSONFileController( MainActivity mainActivity )
    {
        this.mainActivity = mainActivity;
        appDatabase = mainActivity.getAppDatabase();
    }

    public int read( File readFile, LeftSessionController.ImportJSONAsync importJSONAsync ) throws IOException
    {

        String[] fileNameArray = readFile.getPath().split("\\.");
        String ext = fileNameArray[fileNameArray.length - 1];

        if( !ext.equals(Config.KEY_CURRENT_EXT) )
        { // NOT success due to invalid file format
            return Config.ERROR_IMPORT_INVALID_FORMAT_FILE;
        }

        BufferedReader fileReader = new BufferedReader(new FileReader(readFile.getPath()));
        JsonParser parser = new JsonParser();
        JsonObject importedObject = parser.parse(fileReader).getAsJsonObject();
        int partFile = validateAndGetFilePart(importedObject);

        if( partFile > Config.DEFAULT_PART_FILE )
        {

            ArrayList<Cell> cellList = new ArrayList<>();
            ArrayList<MeasuredLocation> locationList = new ArrayList<>();

            Session sessionImported = validateAndGetCurrentSession(importedObject, partFile);

            if( null != sessionImported )
            {

                JsonObject sessionObject = importedObject.get(Config.SESSIONS_KEY).getAsJsonArray().get(0).getAsJsonObject();
                JsonArray sessionArray = sessionObject.get(Config.LIST_KEY).getAsJsonArray();

                for( JsonElement element : sessionArray )
                {

                    JsonObject radio = element.getAsJsonObject();

                    MeasuredLocation importedLocation = getImportedLocation(radio, sessionImported.getId());

                    Cell importedCell = (Cell) buildCellObject(radio, importedLocation.getId(), sessionImported.getId());

                    long cellId = appDatabase.cellDao().insertNewEntry(importedCell);
                    importedCell.setId((int) cellId);

                    cellList.add(importedCell);


                    if( !locationList.contains(importedLocation) )
                    {
                        locationList.add(importedLocation);
                    }

                }

                int cellCount = appDatabase.cellDao().getCellCountBySessionId(sessionImported.getId());

                sessionImported.setCellCount(cellCount);

                importJSONAsync.broadcastResults(sessionImported);

                return Config.ERROR_IMPORT_SUCCESS;
            }

            return Config.ERROR_IMPORT_INSERTED_BEFORE;

        }

        if( partFile < 0 )
        {
            return Config.ERROR_IMPORT_INVALID_FORMAT_FILE;
        }

        return Config.ERROR_IMPORT_INVALID_CHECKSUM;
    }

    private int validateAndGetFilePart( JsonObject importedObject )
    {

        try
        {

            String checksum = importedObject.get(Config.CHECKSUM_KEY).getAsString();
            String dateImported = importedObject.get(Config.DATE_IMPORTED_KEY).getAsString();
            int filePart = importedObject.get(Config.FILE_PART_KEY).getAsInt();

            String validatedChecksum = Utility.generateChecksum(dateImported, filePart);

            if( checksum.equals(validatedChecksum) )
            {
                return filePart;
            }

        }
        catch ( NoSuchAlgorithmException e )
        {
            e.printStackTrace();
            return -1;
        }

        return Config.DEFAULT_PART_FILE;

    }

    /**
     * Validate export id; the export id is a unique key that was generated by {@link Utility#generateExportId(long, long, long)}
     *
     * @param importedObject get from the file;
     * @param partFile       part file;
     * @return {@link Session}
     */
    public Session validateAndGetCurrentSession( JsonObject importedObject, int partFile )
    {


        JsonObject sessionObject = importedObject.get(Config.SESSIONS_KEY).getAsJsonArray().get(0).getAsJsonObject();

        long exportId = sessionObject.get(Config.EXPORT_ID_KEY).getAsLong();
        long endTime = sessionObject.get(Config.END_TIME_KEY).getAsLong();
        String tagId = importedObject.get(Config.TAG_FILE_ID).getAsString();

        Session sessionImport;

        String partFileCollector;


        partFileCollector = partFile + tagId + ";";

        sessionImport = new Session();
        sessionImport.setPartFile(partFileCollector);
        sessionImport.setExportId(exportId);
        sessionImport.setDateTime(endTime);

        long sessionImportId = appDatabase.sessionDao().insertNewEntry(sessionImport);
        sessionImport.setId(sessionImportId);

        return sessionImport;
    }

    /**
     * Check if the part file has been existed or not in database;
     *
     * @param partFileCollection file part collection that has been stored in current object and database;
     * @param tagId              such as fileType, it's used to seperate cell and wifi part file;
     * @param importedPartFile   it was bring by file;
     * @return {@link Boolean} {@true} if the part file has been existed; otherwise process it (continue);
     */
    private boolean isFilePartExisted( String partFileCollection, String tagId, int importedPartFile )
    {

        if( null == partFileCollection )
        {
            return false;
        }

        String[] partFileInArray = partFileCollection.split(";");

        if( partFileInArray.length > 0 )
        {

            for( String existedPartFile : partFileInArray )
            {
                String importedPartFileCheck = importedPartFile + tagId;

                if( existedPartFile.equals(importedPartFileCheck) )
                {
                    return true;
                }

            }

        }

        return false;
    }

    /**
     * Generate {@link MeasuredLocation} object; prepare it to store into database;
     *
     * @param location  json object;
     * @param sessionId session id;
     * @return {@link MeasuredLocation}
     */
    private MeasuredLocation getImportedLocation( JsonObject location, long sessionId )
    {

        double latitude = location.get(Config.LATITUDE_KEY).getAsDouble();
        double longitude = location.get(Config.LONGITUDE_KEY).getAsDouble();
        double accuracy = location.get(Config.ACCURACY_KEY).getAsDouble();
        double elevation = location.get(Config.ELEVATION_KEY).getAsDouble();
        String datetime = location.get(Config.DATE_TIME_KEY).getAsString();

        long time = Utility.strDateToMilis(datetime);

        MeasuredLocation measuredLocation = appDatabase.locationDao().validateMeasuredLocationIfExisted(latitude, longitude, sessionId);

        if( null == measuredLocation )
        {

            measuredLocation = new MeasuredLocation();

            measuredLocation.setSessionId((int) sessionId);
            measuredLocation.setLatitude(latitude);
            measuredLocation.setLongitude(longitude);
            measuredLocation.setAccuracy(accuracy);
            measuredLocation.setElevation(elevation);
            measuredLocation.setTime(time);

            long locationId = appDatabase.locationDao().insertNewEntry(measuredLocation);
            measuredLocation.setId((int) locationId);

        }

        return measuredLocation;

    }

    /**
     * Build to cell object; this method will be generate the data for read/write method based on {@link Object} that was given;
     *
     * @param data            if the {@link Object} instance of {@link JsonObject} it means the result prepare for read method;
     *                        otherwise the result prepare for write method;
     * @param locationId      location measurement id;
     * @param sessionImportId selected session id;
     * @return {@link Object}
     */
    private Object buildCellObject( Object data, long locationId, long sessionImportId )
    {

        Cell cell;
        Object cellBuild;

        if( data instanceof JsonObject )
        {

            JsonObject radioCell = (JsonObject) data;
            cell = new Cell();

            String radioType = radioCell.get((Config.RADIO_TYPE_KEY)).getAsString();
            String cellReference = radioCell.get((Config.CELL_REFERENCE_KEY)).getAsString();
            String[] cellReferenceSplit = cellReference.split("\\.");

            int cellId = radioCell.get(Config.CELL_ID_KEY).getAsInt();

            int mcc = Integer.parseInt(cellReferenceSplit[0]);
            int mnc = Integer.parseInt(cellReferenceSplit[1]);
            int lac = Integer.parseInt(cellReferenceSplit[2]);

            int signalStrength = radioCell.get(Config.SIGNAL_STRENGTH_KEY).getAsInt();
            int psc = radioCell.get(Config.PSC_KEY).getAsInt();

            cell.setSessionId((int) sessionImportId);
            cell.setCellReference(cellReference);
            cell.setMcc(mcc);
            cell.setMnc(mnc);
            cell.setLac(lac);
            cell.setCellId(cellId);
            cell.setRadioType(radioType);
            cell.setPsc(psc);
            cell.setLocationId(locationId);

            cellBuild = cell;

        }
        else
        {

            cell = (Cell) data;

            JsonObject cellJSON = new JsonObject();
            MeasuredLocation location = appDatabase.locationDao().getSingleLocation(cell.getLocationId());

            cellJSON.addProperty(Config.OBJECT_ID_KEY, cell.getId());
            cellJSON.addProperty(Config.CELL_ID_KEY, cell.getCellId());
            cellJSON.addProperty(Config.PSC_KEY, cell.getPsc());
            cellJSON.addProperty(Config.CELL_REFERENCE_KEY, cell.getCellReference());
            cellJSON.addProperty(Config.BEARING_KEY, location.getBearing());
            cellJSON.addProperty(Config.LATITUDE_KEY, cell.getLatitude());
            cellJSON.addProperty(Config.LONGITUDE_KEY, cell.getLongitude());
            cellJSON.addProperty(Config.DATE_TIME_KEY, cell.getDatetime());

            cellBuild = cellJSON;

        }

        return cellBuild;

    }


}
