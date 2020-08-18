package com.example.myapplication8.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.PathOverlay;

import java.util.ArrayList;

public class ScannedListFragment extends Fragment
{
    static
    {
        ListviewItem.addTypeCount(ItemCellCalculation.class);
    }

    private ListviewAdapter mAdapter;
    private SwipeRefreshLayoutBottom mSwipe;
    private ArrayList<ListviewItem> mData = new ArrayList<>();
    private ArrayList<CellCalculation> selectedRadio;
    private MainActivity mActivity;

    private int selectedCell;
    private int selectedWifi;
    private Session session;

    private boolean isPointIncluded = false;
    private boolean addWifiToRadioList = false;
    private boolean addCellToRadioList = false;

    private ProfileSettingSingleton profileSingleton;
    private int selectedItem = 0;

    private SparseArray<MeasuredLocation> locationHashMap;
    private boolean selectionOnClear = false;

    private LatLngBounds.Builder builder;
    private LatLngBounds bounds;

    public ScannedListFragment()
    {
        locationHashMap = new SparseArray<>();
        builder = new LatLngBounds.Builder();
    }

    public ArrayList<CellCalculation> getSelectedRadio()
    {
        return this.selectedRadio;
    }

    public void clearListviewData()
    {
        mData.clear();
    }

    @Override
    public View onCreateView( LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState )
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.layout_listview, container, false);
        mAdapter = new ListviewAdapter(this.getActivity(), 0, mData);

        //use to refresh list
        mSwipe = new SwipeRefreshLayoutBottom(getActivity());
        mSwipe.setOnRefreshListener(new SwipeRefreshLayoutBottom.IOnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                onRefreshScannedResults();
            }
        });

        ListView mListView = new ListView(getActivity());
        mSwipe.addView(mListView);
        mListView.setCacheColorHint(Color.WHITE);
        mListView.requestFocus(0);
        FrameLayout mRootLayout = view.findViewById(R.id.root_layout);
        mRootLayout.setForeground(new ColorDrawable(Color.BLACK));
        mRootLayout.getForeground().setAlpha(0);

        mRootLayout.addView(mSwipe);

        mListView.setAdapter(mAdapter);
        //end refresh list

        selectedRadio = new ArrayList<>();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id )
            {
                ListviewItem item = (ListviewItem) parent.getItemAtPosition(position);
                selectUnselectRow(item);
            }
        });

        selectedCell = 0;
        selectedWifi = 0;
        //To do : set from profile
        int defaultLocation = 0;

        Bundle args = getArguments();
        if( null != args.getParcelable("session") )
        {
            session = args.getParcelable("session");
        }

        populateScannedList(session);

        return view;
    }

    public void populateScannedList( Session session )
    {
        if( mActivity instanceof MainActivity )
        {
            populateCellInfoAsync(mActivity, session);
            if( mData.size() < 1 )
            {
                mActivity.getLeftPaneController().getTextEmptyList().setVisibility(View.VISIBLE);
                mActivity.getLeftPaneController().getTextEmptyList().setText("There is no scan result");
            }
        }

    }

    public ArrayList<ListviewItem> getData()
    {
        return mData;
    }

    @Override
    public void onAttach( Context context )
    {
        super.onAttach(context);
        if( context instanceof MainActivity )
        {
            mActivity = (MainActivity) context;
        }
        this.profileSingleton = ProfileSettingSingleton.getInstance();
    }

    private synchronized void populateCellInfoAsync( final MainActivity mActivity, final Session session )
    {
        CellTable cellTable = mActivity.getCellTable();
        ArrayList<Cell> list;

        if( session.getCellList().size() > 0 )
        {
            list = session.getCellList();
        }
        else
        {
            list = cellTable.getUniqueCellBySessionIdLimited(session.getId(), 0);
        }

        if( list.size() < 1 || session.getWifiList().size() > 0 )
        {
            populateWifiInfoAsync(mActivity, session);
        }
        else
        {
            session.setCellList(list);
            addCellToRadioList = selectedCell != session.getCellList().size();
        }

        selectedCell = 0;
        for( Cell cell : list )
        {
            new CellAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cell);
        }
    }

    private synchronized void populateWifiInfoAsync( final MainActivity mActivity, final Session session )
    {

        WifiTable wifiTable = mActivity.getWifiTable();
        ArrayList<Wifi> list;

        if( session.getWifiList().size() > 0 )
        {
            list = session.getWifiList();
        }
        else
        {
            list = wifiTable.getUniqueWifiBySessionIdLimited(session.getId(), 0);
        }

        session.setWifiList(list);

        if( list.size() < 1 )
        {
            return;
        }

        if( selectedWifi == session.getWifiList().size() )
        {
            addWifiToRadioList = false;
        }
        else
        {
            addWifiToRadioList = true;
        }

        selectedWifi = 0;
        for( Wifi wifi : list )
        {
            new WifiAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, wifi);
        }
    }

    private void selectUnselectRow( ListviewItem item )
    {
        item.onSelected = !item.onSelected;

        ItemCellCalculation itemCellCalculation = (ItemCellCalculation) item;
        CellCalculation cellCalculation = itemCellCalculation.getCellCalculation();

        if( !itemCellCalculation.onSelected )
        {
            selectedItem--;
            unselectRadioItem(cellCalculation);
            selectedRadio.remove(cellCalculation);

        }
        else
        {
            selectedItem++;
            selectRadioItem(cellCalculation);
            selectedRadio.add(cellCalculation);
        }

        mActivity.getMapWrapper().drawClusterOsmOnMap();
        mAdapter.notifyDataSetChanged();

    }

    /**
     * Step 1: If only 1 RadioItem is selected, clear the maps first because there were elements from Overview drawing
     * Step 2: Zoom to selected Radio's center position
     * Step 3: Draw the Radio details on the map based on the current 'Selected Cell or Wifi' sub profile
     * Step 4: Draw the Radio details on the map based on the current 'Selected Location' sub profile
     */
    public void selectRadioItem( CellCalculation cellCalculation )
    {
        //        mActivity.getMapWrapper().setLastZoomLevel(-1);
        // Step 1
        if( selectedItem == 1 )
        {
            //its not clear marker yet in osm
            mActivity.getMapWrapper().clearMap();
        }

        // Step 2
        //clear cluster item to handle select radio item in osm
        if( cellCalculation.getCenterBound() != null )
        {
            mActivity.getMapWrapper().zoomToBound(cellCalculation.getCenterBound());
        }

        mActivity.getMapWrapper().clearClusterOsmOnMap();

        // Step 3
        if( cellCalculation.getCurrentScannedType() == Global.SCANNED_TYPE_CELL )
        {
            mActivity.getMapWrapper().drawRadioBasedOnProfile(cellCalculation, profileSingleton.getSelectedCell(), Global.SCANNED_TYPE_CELL);
        }
        else if( cellCalculation.getCurrentScannedType() == Global.SCANNED_TYPE_WIFI )
        {
            mActivity.getMapWrapper().drawRadioBasedOnProfile(cellCalculation, profileSingleton.getSelectedWifi(), Global.SCANNED_TYPE_WIFI);
        }

        // Step 4
        mActivity.getMapWrapper().drawLocationBasedOnSelectedProfile(cellCalculation, profileSingleton.getSelectedLocation());
    }

    /**
     * Step 1: Remove the drawing generated from Step 4 of selectRadioItem function
     * Step 2: Remove the drawing generated from Step 3 of selectRadioItem function
     * Step 3: If no Radio Item is selected, draw the overview of this session based on 'Overview Location' sub profile
     * Step 4: Draw the radio details of all the radio captured in this session based on 'Overview Cell or Wifi' sub profile
     */
    public void unselectRadioItem( CellCalculation cellCalculation )
    {
        // Step 1
        mActivity.getMapWrapper().removeLocationBasedOnSelectedProfile(cellCalculation);

        // Step 2
        if( cellCalculation.getCurrentScannedType() == Global.SCANNED_TYPE_CELL )
        {
            mActivity.getMapWrapper().removeRadioBasedOnProfile(cellCalculation, profileSingleton.getSelectedCell());

        }
        else if( cellCalculation.getCurrentScannedType() == Global.SCANNED_TYPE_WIFI )
        {
            mActivity.getMapWrapper().removeRadioBasedOnProfile(cellCalculation, profileSingleton.getSelectedWifi());
        }


        if( selectedItem == 0 )
        {
            selectedCell = session.getCellList().size();
            selectedWifi = session.getWifiList().size();

            // Step 3
            redrawOnClearSelected();

            // Step 4
            for( ListviewItem listviewItem : mData )
            {
                ItemCellCalculation itemCellCalc = (ItemCellCalculation) listviewItem;
                CellCalculation cellCalc = itemCellCalc.getCellCalculation();
                if( cellCalc.getCurrentScannedType() == Global.SCANNED_TYPE_CELL && selectedCell < profileSingleton.getOverviewCell().getRadioNumber().getAmountRadio() )
                {
                    mActivity.getMapWrapper().drawRadioBasedOnProfile(cellCalc, profileSingleton.getOverviewCell(), Global.SCANNED_TYPE_CELL);
                    selectedCell++;
                }
                else if( cellCalc.getCurrentScannedType() == Global.SCANNED_TYPE_WIFI && selectedWifi < profileSingleton.getOverviewWifi().getRadioNumber().getAmountRadio() )
                {
                    mActivity.getMapWrapper().drawRadioBasedOnProfile(cellCalc, profileSingleton.getOverviewWifi(), Global.SCANNED_TYPE_WIFI);
                    selectedWifi++;
                }

            }
        }

    }

    @Override
    public void onStop()
    {
        // There was a 'googlemap.clear() command here
        // was removed in respond to fix issue-8370-CellTrax_task_scan_result_missing
        super.onStop();
    }

    public int getSelectedItem()
    {
        return selectedItem;
    }


    private class WifiAsyncTask extends AsyncTask<Wifi, CellCalculation, Void>
    {
        @Override
        protected Void doInBackground( Wifi... wifi )
        {
            CellCalculation cellCalculation = new CellCalculation(Global.SCANNED_TYPE_WIFI);

            // Set convex hull points and wifi list
            cellCalculation = mActivity.getWifiTable().getByMacAddressAndSessionId(cellCalculation, session.getId(), wifi[0].getBssid());

            cellCalculation.calculateCenter();
            publishProgress(cellCalculation);

            return null;
        }

        @Override
        protected synchronized void onProgressUpdate( CellCalculation... calculations )
        {
            ItemCellCalculation item = new ItemCellCalculation(calculations[0]);
            item.onSelected = false;

            new WifiLocationAsyncTask().executeOnExecutor(THREAD_POOL_EXECUTOR, calculations[0].getWifiList());

            if( !profileSingleton.getOverviewWifi().getRadioNumber().isShowing() || selectedWifi < profileSingleton.getOverviewWifi().getRadioNumber().getAmountRadio() )
            {
                mActivity.getMapWrapper().drawRadioBasedOnProfile(calculations[0], profileSingleton.getOverviewWifi(), Global.SCANNED_TYPE_WIFI);
            }

            if( !selectionOnClear )
            {
                selectedWifi++;
            }

            if( addWifiToRadioList )
            {
                mData.add(item);
                mAdapter.notifyDataSetChanged();

                if( selectedWifi == session.getWifiList().size() )
                {
                    addWifiToRadioList = false;
                    if( mSwipe.isRefreshing() )
                    {
                        mSwipe.setRefreshing(false);
                    }
                }
                else
                {
                    addWifiToRadioList = true;
                }

                if( mActivity.getLeftPaneController().getTextEmptyList().getVisibility() == View.VISIBLE )
                {
                    mActivity.getLeftPaneController().getTextEmptyList().setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        synchronized protected void onPostExecute( Void params )
        {
            if( mData.size() < 1 )
            {
                mActivity.getLeftPaneController().getTextEmptyList().setVisibility(View.VISIBLE);
                mActivity.getLeftPaneController().getTextEmptyList().setText("There is no scan result");
            }
            else
            {
                if( MapSingleton.getInstance().getSelectedMap() == Global.OPENSTREETMAP && selectedWifi == session.getWifiList().size() )
                {
                    mActivity.getMapWrapper().drawClusterOsmOnMap();
                }
            }
        }
    }

    private class CellAsyncTask extends AsyncTask<Cell, CellCalculation, Void>
    {
        @Override
        protected Void doInBackground( Cell... cell )
        {
            CellCalculation cellCalculation = new CellCalculation(Global.SCANNED_TYPE_CELL);
            cellCalculation = mActivity.getCellTable().getByCellRefAndSessionId(cellCalculation, session.getId(), cell[0].getCellReference());

            cellCalculation.calculateCenter();
            publishProgress(cellCalculation);
            return null;
        }

        @Override
        protected synchronized void onProgressUpdate( CellCalculation... cellCalculations )
        {
            ItemCellCalculation item = new ItemCellCalculation(cellCalculations[0]);
            item.onSelected = false;

            new CellLocationAsyncTask().executeOnExecutor(THREAD_POOL_EXECUTOR, cellCalculations[0].getCellList());

            // Draw the cells
            if( !profileSingleton.getOverviewCell().getRadioNumber().isShowing() || selectedCell < profileSingleton.getOverviewCell().getRadioNumber().getAmountRadio() )
            {
                mActivity.getMapWrapper().drawRadioBasedOnProfile(cellCalculations[0], profileSingleton.getOverviewCell(), Global.SCANNED_TYPE_CELL);
            }


            if( !selectionOnClear )
            {
                selectedCell++;
            }

            if( addCellToRadioList )
            {
                mData.add(item);
                mAdapter.notifyDataSetChanged();

                if( selectedCell == session.getCellList().size() )
                {
                    addCellToRadioList = false;
                    if( mSwipe.isRefreshing() )
                    {
                        mSwipe.setRefreshing(false);
                    }
                }
                else
                {
                    addCellToRadioList = true;
                }

                if( mActivity.getLeftPaneController().getTextEmptyList().getVisibility() == View.VISIBLE )
                {
                    mActivity.getLeftPaneController().getTextEmptyList().setVisibility(View.INVISIBLE);
                }
            }
        }

        @Override
        protected void onPostExecute( Void params )
        {
            if( mData.size() < 1 )
            {
                mActivity.getLeftPaneController().getTextEmptyList().setVisibility(View.VISIBLE);
                mActivity.getLeftPaneController().getTextEmptyList().setText("There is no scan result");
            }
            else
            {
                if( MapSingleton.getInstance().getSelectedMap() == Global.OPENSTREETMAP && selectedCell == session.getCellList().size() )
                {
                    mActivity.getMapWrapper().drawClusterOsmOnMap();
                }
            }
        }
    }

    private class CellLocationAsyncTask extends AsyncTask<ArrayList<Cell>, Cell, Void>
    {
        @Override
        protected Void doInBackground( ArrayList<Cell>... cells )
        {
            for( Cell cell : cells[0] )
            {
                publishProgress(cell);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate( Cell... cell )
        {
            drawLocationOnMapAsync(cell[0].getMeasuredLocation());

        }

        @Override
        protected void onPostExecute( Void params )
        {
            if( selectionOnClear )
            {
                selectionOnClear = false;
            }
        }
    }

    private class WifiLocationAsyncTask extends AsyncTask<ArrayList<Wifi>, Wifi, Void>
    {
        @Override
        protected Void doInBackground( ArrayList<Wifi>... wifis )
        {
            for( Wifi wifi : wifis[0] )
            {
                publishProgress(wifi);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate( Wifi... wifi )
        {
            drawLocationOnMapAsync(wifi[0].getMeasuredLocation());
        }

        @Override
        protected void onPostExecute( Void params )
        {
            if( selectionOnClear )
            {
                selectionOnClear = false;
            }
        }
    }

    public void onRefreshScannedResults()
    {
        ArrayList<Cell> cellListRefresh = mActivity.getCellTable().getUniqueCellBySessionIdLimited(session.getId(), session.getCellList().size());
        ArrayList<Wifi> wifiListRefresh = mActivity.getWifiTable().getUniqueWifiBySessionIdLimited(session.getId(), session.getWifiList().size());

        if( cellListRefresh.size() != 0 )
        {
            addCellToRadioList = true;
            for( Cell cellAdded : cellListRefresh )
            {
                session.getCellList().add(cellAdded);
                new CellAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cellAdded);
            }
        }
        else
        {
            if( wifiListRefresh.size() != 0 )
            {
                addWifiToRadioList = true;
                for( Wifi wifiAdded : wifiListRefresh )
                {
                    session.getWifiList().add(wifiAdded);
                    new WifiAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, wifiAdded);
                }
            }
            else
            {
                mSwipe.setRefreshing(false);
                mSwipe.setEnabled(false);
            }
        }
    }

    public void redrawOnClearSelected()
    {
        selectionOnClear = true;

        if( session.getCellList().size() != 0 )
        {
            for( Cell cell : session.getCellList() )
            {
                new CellAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cell);
            }
        }

        if( session.getWifiList().size() != 0 )
        {
            for( Wifi wifi : session.getWifiList() )
            {
                new WifiAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, wifi);
            }
        }
    }

    public void drawLocationOnMapAsync( MeasuredLocation measuredLocation )
    {
        PolylineOptions polylineOptions = new PolylineOptions();
        PathOverlay pathOverlay = new PathOverlay(Color.BLACK, mActivity);
        LatLng point;

        point = new LatLng(measuredLocation.getLatitude(), measuredLocation.getLongitude());
        builder.include(point);
        polylineOptions.add(point);
        pathOverlay.addPoint(new GeoPoint(measuredLocation.getLatitude(), measuredLocation.getLongitude()));

        if( !isPointIncluded )
        {
            isPointIncluded = true;
        }

        if( profileSingleton.getOverviewLocation().getLineOfRoute().isShowing() )
        {
            pathOverlay.setColor(profileSingleton.getOverviewLocation().getLineOfRoute().getColor());
            polylineOptions.color(profileSingleton.getOverviewLocation().getLineOfRoute().getColor());
            mActivity.getMapWrapper().addLocationPolyline(polylineOptions, pathOverlay);
        }

        if( isPointIncluded )
        {
            bounds = builder.build();
            mActivity.getMapWrapper().zoomToBound(bounds);
        }

        locationHashMap.put(measuredLocation.getId(), measuredLocation);
        mActivity.getMapWrapper().drawLocationBasedOnProfile(mActivity, measuredLocation, profileSingleton.getOverviewLocation());


    }

    public SparseArray<MeasuredLocation> getLocationHashMap()
    {
        return locationHashMap;
    }

    public void resetLocationSelected()
    {
        this.selectionOnClear = false;
    }

    public LatLngBounds.Builder getBuilder()
    {
        return builder;
    }

    public void setBuilder( LatLngBounds.Builder builder )
    {
        this.builder = builder;
    }

}
